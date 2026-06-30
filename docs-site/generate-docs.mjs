#!/usr/bin/env node

/**
 * generate-docs.mjs
 *
 * Legge i file markdown da documentation/ (EN) e documentazione/ (IT),
 * li converte in file .ts di contenuto e componenti Angular standalone.
 *
 * Uso: node generate-docs.mjs (dalla cartella docs-site/)
 */

import { readdir, readFile, writeFile, mkdir } from 'fs/promises';
import { join, basename, dirname, extname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// ---------------------------------------------------------------------------
// Configurazione
// ---------------------------------------------------------------------------

const EN_SOURCE = join(__dirname, '..', 'documentation');
const IT_SOURCE = join(__dirname, '..', 'documentazione');
const CONTENT_DEST = join(__dirname, 'src', 'app', 'features', 'docs', 'content');
const PAGES_DEST = join(__dirname, 'src', 'app', 'features', 'docs', 'pages');

const EXCLUDED_FOLDERS = new Set(['14-reference-img']);

const FOLDER_MAP_IT_TO_EN = {
  '01-overview-progetto': '01-project-overview',
  '02-analisi-competitor': '02-competitor-analysis',
  '03-analisi-funzionale': '03-functional-analysis',
  '04-architettura': '04-architecture',
  '05-stack-tecnologico': '05-technology-stack',
  '06-modello-dati': '06-data-model',
  '06-sicurezza': '06-security',
  '07-api-reference': '07-api-reference',
  '08-frontend': '08-frontend',
  '09-sicurezza-privacy': '09-security-privacy',
  '10-deployment': '10-deployment',
  '11-roadmap': '11-roadmap',
  '12-mcp-integration': '12-mcp-integration',
  '13-guida-utente': '13-user-guide',
  '15-ambiti-estensione': '15-extension-domains',
};

const POSITION_OVERRIDES = {
  '06-data-model': 6,
  '06-security': 7,
};

function getFolderPosition(folderName) {
  if (POSITION_OVERRIDES[folderName] !== undefined) return POSITION_OVERRIDES[folderName];
  const match = folderName.match(/^(\d+)-/);
  if (!match) return 0;
  const num = parseInt(match[1], 10);
  if (num > 6) return num + 1;
  return num;
}

// ---------------------------------------------------------------------------
// Utilita
// ---------------------------------------------------------------------------

function humanize(name) {
  let clean = name;
  if (clean.endsWith('.md')) clean = clean.slice(0, -3);
  clean = clean.replace(/^\d+-/, '');
  clean = clean.replace(/-/g, ' ');
  clean = clean.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
  return clean;
}

function slugify(name) {
  let clean = name;
  if (clean.endsWith('.md')) clean = clean.slice(0, -3);
  clean = clean.replace(/^\d+-/, '');
  return clean;
}

function camelCase(str) {
  return str.replace(/-([a-z])/g, (_, c) => c.toUpperCase());
}

function pascalCase(str) {
  const cc = camelCase(str);
  return cc.charAt(0).toUpperCase() + cc.slice(1);
}

function stripFrontmatter(content) {
  const trimmed = content.trimStart();
  if (!trimmed.startsWith('---')) return content;
  const endIdx = trimmed.indexOf('---', 3);
  if (endIdx === -1) return content;
  return trimmed.substring(endIdx + 3).trimStart();
}

function escapeTemplateString(str) {
  return str.replace(/\\/g, '\\\\').replace(/`/g, '\\`').replace(/\$\{/g, '\\${');
}

async function ensureDir(dirPath) {
  await mkdir(dirPath, { recursive: true });
}

// ---------------------------------------------------------------------------
// Link mapping: filename.md -> route Angular
// ---------------------------------------------------------------------------

// Mappa globale: "filename.md" -> "/docs/category/slug" (per EN e IT)
const linkMap = new Map();
// Mappa cartelle IT -> EN per risolvere link cross-category
const folderNameToEnCategory = new Map();

async function buildLinkMap(enFolders, itFolderMap) {
  for (const folder of enFolders) {
    const enFolderName = folder.name;
    const enSourceDir = join(EN_SOURCE, enFolderName);

    // EN files
    try {
      const entries = await readdir(enSourceDir, { withFileTypes: true });
      const enFiles = entries.filter(e => e.isFile() && extname(e.name).toLowerCase() === '.md' && !/^readme/i.test(e.name))
        .map(e => e.name).sort();

      for (const fileName of enFiles) {
        const slug = slugify(fileName);
        const route = `/docs/${enFolderName}/${slug}`;
        linkMap.set(fileName, route);
        // Anche senza numero prefix
        linkMap.set(fileName.replace(/^\d+-/, ''), route);
      }
    } catch (err) { /* skip */ }

    // IT files (mappa a stesse route EN per posizione)
    const itDir = itFolderMap[enFolderName];
    if (itDir) {
      try {
        const enEntries = await readdir(enSourceDir, { withFileTypes: true });
        const enFiles = enEntries.filter(e => e.isFile() && extname(e.name).toLowerCase() === '.md' && !/^readme/i.test(e.name))
          .map(e => e.name).sort();

        const itEntries = await readdir(itDir, { withFileTypes: true });
        const itFiles = itEntries.filter(e => e.isFile() && extname(e.name).toLowerCase() === '.md' && !/^readme/i.test(e.name))
          .map(e => e.name).sort();

        // Mappa nome cartella IT -> categoria EN
        const itFolderBasename = basename(itDir);
        folderNameToEnCategory.set(itFolderBasename, enFolderName);

        for (let i = 0; i < itFiles.length && i < enFiles.length; i++) {
          const enSlug = slugify(enFiles[i]);
          const route = `/docs/${enFolderName}/${enSlug}`;
          linkMap.set(itFiles[i], route);
          // Anche senza numero prefix
          linkMap.set(itFiles[i].replace(/^\d+-/, ''), route);
        }
      } catch (err) { /* skip */ }
    }
  }
}

/**
 * Riscrive i link .md nel contenuto markdown per puntare alle route Angular.
 * Gestisce:
 * - Link stessa categoria: [testo](02-file.md) -> [testo](/docs/category/slug)
 * - Link cross-categoria: [testo](../folder/02-file.md) -> [testo](/docs/en-category/slug)
 * - Link con anchor: [testo](file.md#section) -> [testo](/docs/category/slug#section)
 * - Rimuove link a README.md (non esiste nelle route)
 */
function rewriteMdLinks(content, currentCategory) {
  return content.replace(
    /\[([^\]]*)\]\(([^)]*\.md(?:#[^)]*)?)\)/g,
    (match, text, href) => {
      // Separa path e anchor
      const [filePart, anchor] = href.split('#');

      // Ignora link a README.md
      if (filePart.toLowerCase() === 'readme.md' || filePart.endsWith('/readme.md') || filePart.endsWith('/README.md')) {
        return text; // Mostra solo il testo, senza link
      }

      let route = null;

      if (filePart.startsWith('../') || filePart.startsWith('./')) {
        // Cross-category link: ../folder-name/file.md
        const parts = filePart.replace(/^\.\.\//, '').replace(/^\.\//, '').split('/');
        if (parts.length === 2) {
          const folderName = parts[0];
          const fileName = parts[1];
          // Prova direttamente
          route = linkMap.get(fileName);
          if (!route) {
            // Prova con il folder mappato
            const enCategory = FOLDER_MAP_IT_TO_EN[folderName] || folderNameToEnCategory.get(folderName) || folderName;
            const fileSlug = slugify(fileName);
            route = `/docs/${enCategory}/${fileSlug}`;
          }
        } else {
          // File singolo con ./
          route = linkMap.get(parts[parts.length - 1]);
        }
      } else {
        // Same-category link: file.md
        route = linkMap.get(filePart);
        if (!route) {
          // Fallback: costruisci da slug nella categoria corrente
          const fileSlug = slugify(filePart);
          route = `/docs/${currentCategory}/${fileSlug}`;
        }
      }

      if (route) {
        const fullHref = anchor ? `${route}#${anchor}` : route;
        return `[${text}](${fullHref})`;
      }

      return match; // Lascia invariato se non riesce a mappare
    }
  );
}

// ---------------------------------------------------------------------------
// Struttura dati
// ---------------------------------------------------------------------------

const categories = [];

// ---------------------------------------------------------------------------
// Generazione
// ---------------------------------------------------------------------------

const stats = { contentFiles: 0, components: 0, routes: 0, linksRewritten: 0, errors: [] };

async function processCategory(enFolderName, enSourceDir, itSourceDir, itFolderBasename) {
  const categorySlug = enFolderName;
  const position = getFolderPosition(enFolderName);
  const label = humanize(enFolderName);
  const labelIt = itFolderBasename ? humanize(itFolderBasename) : label;

  // Leggi file EN
  let enFiles = [];
  try {
    const entries = await readdir(enSourceDir, { withFileTypes: true });
    enFiles = entries.filter(e => e.isFile() && extname(e.name).toLowerCase() === '.md' && !/^readme/i.test(e.name))
      .map(e => e.name)
      .sort();
  } catch (err) {
    stats.errors.push(`Errore lettura ${enSourceDir}: ${err.message}`);
    return null;
  }

  // Leggi file IT
  let itFiles = [];
  if (itSourceDir) {
    try {
      const entries = await readdir(itSourceDir, { withFileTypes: true });
      itFiles = entries.filter(e => e.isFile() && extname(e.name).toLowerCase() === '.md' && !/^readme/i.test(e.name))
        .map(e => e.name)
        .sort();
    } catch (err) {
      // IT potrebbe non esistere per tutte le categorie
    }
  }

  const docs = [];

  for (let i = 0; i < enFiles.length; i++) {
    const enFileName = enFiles[i];
    const docSlug = slugify(enFileName);
    const docLabel = humanize(enFileName);
    const componentName = pascalCase(docSlug) + 'Component';

    // Genera file contenuto EN
    const enContentDir = join(CONTENT_DEST, 'en', categorySlug);
    await ensureDir(enContentDir);
    const enContent = await readFile(join(enSourceDir, enFileName), 'utf-8');
    let enStripped = stripFrontmatter(enContent);
    enStripped = rewriteMdLinks(enStripped, categorySlug);
    const enContentFile = join(enContentDir, `${docSlug}.ts`);
    await writeFile(enContentFile, `export const content = \`${escapeTemplateString(enStripped)}\`;\n`, 'utf-8');
    stats.contentFiles++;

    // Genera file contenuto IT (cerca il file corrispondente per posizione)
    const itFileName = itFiles[i]; // stesso indice (ordine alfabetico)
    if (itFileName) {
      const itContentDir = join(CONTENT_DEST, 'it', categorySlug);
      await ensureDir(itContentDir);
      const itContent = await readFile(join(itSourceDir, itFileName), 'utf-8');
      let itStripped = stripFrontmatter(itContent);
      itStripped = rewriteMdLinks(itStripped, categorySlug);
      const itContentFile = join(itContentDir, `${docSlug}.ts`);
      await writeFile(itContentFile, `export const content = \`${escapeTemplateString(itStripped)}\`;\n`, 'utf-8');
      stats.contentFiles++;
    }

    // Genera componente Angular
    const pageDir = join(PAGES_DEST, categorySlug);
    await ensureDir(pageDir);

    const hasIt = !!itFileName;
    const enImport = `import { content as contentEN } from '../../content/en/${categorySlug}/${docSlug}';`;
    const itImport = hasIt ? `import { content as contentIT } from '../../content/it/${categorySlug}/${docSlug}';` : '';

    const contentMap = hasIt
      ? `{ en: contentEN, it: contentIT }`
      : `{ en: contentEN }`;

    const componentCode = `import { Component, computed, inject } from '@angular/core';
import { MarkdownComponent } from 'ngx-markdown';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { DocNavComponent } from '../../../../shared/components/doc-nav/doc-nav.component';
${enImport}
${itImport}

@Component({
  selector: 'app-doc-${docSlug}',
  standalone: true,
  imports: [MarkdownComponent, DocNavComponent],
  template: \`
    <article class="doc-content">
      <markdown [data]="content()"></markdown>
    </article>
    <app-doc-nav [prev]="prev" [next]="next"></app-doc-nav>
  \`
})
export class ${componentName} {
  private i18n = inject(TranslationService);
  private contentMap: Record<string, string> = ${contentMap};

  content = computed(() => {
    const lang = this.i18n.currentLang();
    return this.contentMap[lang] ?? this.contentMap['en'] ?? '';
  });

  prev: { path: string; label: string } | null = null;
  next: { path: string; label: string } | null = null;
}
`;

    await writeFile(join(pageDir, `${docSlug}.component.ts`), componentCode, 'utf-8');
    stats.components++;

    const docLabelIt = itFileName ? humanize(itFileName) : docLabel;

    docs.push({
      slug: docSlug,
      label: docLabel,
      labelIt: docLabelIt,
      componentName,
      fileName: `${docSlug}.component`,
      hasIt,
    });
  }

  // Genera file routes per categoria
  const routeImports = docs.map(d =>
    `    { path: '${d.slug}', loadComponent: () => import('./${d.fileName}').then(m => m.${d.componentName}) }`
  ).join(',\n');

  const routesCode = `import { Routes } from '@angular/router';

export const ${camelCase(enFolderName.replace(/^\d+-/, '')).toUpperCase()}_ROUTES: Routes = [
  { path: '', redirectTo: '${docs[0]?.slug || ''}', pathMatch: 'full' },
${routeImports}
];
`;

  const routeFileName = `${enFolderName.replace(/^\d+-/, '')}.routes.ts`;
  await writeFile(join(PAGES_DEST, categorySlug, routeFileName), routesCode, 'utf-8');
  stats.routes++;

  // Imposta prev/next per i componenti
  await setPrevNext(categorySlug, docs);

  return {
    id: categorySlug,
    label,
    labelIt,
    position,
    routeFileName,
    routeExportName: `${camelCase(enFolderName.replace(/^\d+-/, '')).toUpperCase()}_ROUTES`,
    docs,
  };
}

async function setPrevNext(categorySlug, docs) {
  // Rileggi e aggiorna ogni componente con prev/next corretti
  for (let i = 0; i < docs.length; i++) {
    const doc = docs[i];
    const filePath = join(PAGES_DEST, categorySlug, `${doc.slug}.component.ts`);
    let code = await readFile(filePath, 'utf-8');

    const prevDoc = i > 0 ? docs[i - 1] : null;
    const nextDoc = i < docs.length - 1 ? docs[i + 1] : null;

    const prevValue = prevDoc
      ? `{ path: '/docs/${categorySlug}/${prevDoc.slug}', label: '${prevDoc.label.replace(/'/g, "\\'")}' }`
      : 'null';
    const nextValue = nextDoc
      ? `{ path: '/docs/${categorySlug}/${nextDoc.slug}', label: '${nextDoc.label.replace(/'/g, "\\'")}' }`
      : 'null';

    code = code.replace(
      /prev: \{ path: string; label: string \} \| null = null;/,
      `prev: { path: string; label: string } | null = ${prevValue};`
    );
    code = code.replace(
      /next: \{ path: string; label: string \} \| null = null;/,
      `next: { path: string; label: string } | null = ${nextValue};`
    );

    await writeFile(filePath, code, 'utf-8');
  }
}

async function generateDocsRoutes(categories) {
  const imports = categories.map(c =>
    `    {\n      path: '${c.id}',\n      loadChildren: () => import('./pages/${c.id}/${c.routeFileName.replace('.ts', '')}').then(m => m.${c.routeExportName})\n    }`
  ).join(',\n');

  const defaultDoc = categories[0]?.docs[0];
  const defaultRedirect = defaultDoc ? `${categories[0].id}/${defaultDoc.slug}` : '';

  const code = `import { Routes } from '@angular/router';

export const DOCS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./docs-layout.component').then(m => m.DocsLayoutComponent),
    children: [
      { path: '', redirectTo: '${defaultRedirect}', pathMatch: 'full' },
${imports}
    ]
  }
];
`;

  await writeFile(join(__dirname, 'src', 'app', 'features', 'docs', 'docs.routes.ts'), code, 'utf-8');
}

async function generateSidebarData(categories) {
  const data = categories.map(c => ({
    id: c.id,
    labels: { en: c.label, it: c.labelIt },
    position: c.position,
    docs: c.docs.map(d => ({
      slug: d.slug,
      labels: { en: d.label, it: d.labelIt },
      path: `/docs/${c.id}/${d.slug}`,
    })),
  }));

  const code = `export interface SidebarDoc {
  slug: string;
  labels: Record<string, string>;
  path: string;
}

export interface SidebarCategory {
  id: string;
  labels: Record<string, string>;
  position: number;
  docs: SidebarDoc[];
}

export const SIDEBAR_DATA: SidebarCategory[] = ${JSON.stringify(data, null, 2)};
`;

  await writeFile(join(__dirname, 'src', 'app', 'features', 'docs', 'sidebar-data.ts'), code, 'utf-8');
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

async function main() {
  console.log('==============================================');
  console.log('  LocalMind - Angular Docs Generator');
  console.log('==============================================\n');

  // Leggi cartelle EN
  const enFolders = (await readdir(EN_SOURCE, { withFileTypes: true }))
    .filter(e => e.isDirectory() && !EXCLUDED_FOLDERS.has(e.name))
    .sort((a, b) => a.name.localeCompare(b.name));

  // Leggi cartelle IT
  let itFolders = [];
  try {
    itFolders = (await readdir(IT_SOURCE, { withFileTypes: true }))
      .filter(e => e.isDirectory() && !EXCLUDED_FOLDERS.has(e.name));
  } catch (err) {
    console.warn('Cartella IT non trovata, continuo solo con EN');
  }

  const itFolderMap = {};
  const itFolderNameMap = {}; // EN folder name -> IT folder basename
  for (const f of itFolders) {
    const enName = FOLDER_MAP_IT_TO_EN[f.name] || f.name;
    itFolderMap[enName] = join(IT_SOURCE, f.name);
    itFolderNameMap[enName] = f.name;
  }

  // FASE 1: Costruire la mappa dei link PRIMA di generare i contenuti
  console.log('Fase 1: Costruzione mappa link...');
  await buildLinkMap(enFolders, itFolderMap);
  console.log(`  Mappati ${linkMap.size} link .md -> route Angular\n`);

  // FASE 2: Generazione contenuti
  console.log('Fase 2: Generazione contenuti...');
  for (const folder of enFolders) {
    console.log(`  [${folder.name}]`);
    const itDir = itFolderMap[folder.name] || null;
    const cat = await processCategory(
      folder.name,
      join(EN_SOURCE, folder.name),
      itDir,
      itFolderNameMap[folder.name] || null
    );
    if (cat) categories.push(cat);
  }

  // Ordina per posizione
  categories.sort((a, b) => a.position - b.position);

  // Genera docs.routes.ts
  console.log('\nGenerazione docs.routes.ts...');
  await generateDocsRoutes(categories);

  // Genera sidebar-data.ts
  console.log('Generazione sidebar-data.ts...');
  await generateSidebarData(categories);

  // Report
  console.log('\n==============================================');
  console.log('  Report');
  console.log('==============================================');
  console.log(`File contenuto generati:  ${stats.contentFiles}`);
  console.log(`Componenti generati:      ${stats.components}`);
  console.log(`File routes generati:     ${stats.routes}`);
  console.log(`Categorie:                ${categories.length}`);

  if (stats.errors.length > 0) {
    console.log(`\nErrori (${stats.errors.length}):`);
    for (const err of stats.errors) console.error(`  - ${err}`);
    process.exit(1);
  } else {
    console.log('\nGenerazione completata senza errori.');
  }
}

main().catch(err => {
  console.error('Errore fatale:', err);
  process.exit(1);
});
