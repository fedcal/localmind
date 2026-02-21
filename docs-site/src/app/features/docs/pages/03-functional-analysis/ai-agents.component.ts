import { Component, computed, inject } from '@angular/core';
import { MarkdownComponent } from 'ngx-markdown';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { DocNavComponent } from '../../../../shared/components/doc-nav/doc-nav.component';
import { content as contentEN } from '../../content/en/03-functional-analysis/ai-agents';
import { content as contentIT } from '../../content/it/03-functional-analysis/ai-agents';

@Component({
  selector: 'app-doc-ai-agents',
  standalone: true,
  imports: [MarkdownComponent, DocNavComponent],
  template: `
    <article class="doc-content">
      <markdown [data]="content()"></markdown>
    </article>
    <app-doc-nav [prev]="prev" [next]="next"></app-doc-nav>
  `
})
export class AiAgentsComponent {
  private i18n = inject(TranslationService);
  private contentMap: Record<string, string> = { en: contentEN, it: contentIT };

  content = computed(() => {
    const lang = this.i18n.currentLang();
    return this.contentMap[lang] ?? this.contentMap['en'] ?? '';
  });

  prev: { path: string; label: string } | null = { path: '/docs/03-functional-analysis/document-intelligence-rag', label: 'Document Intelligence Rag' };
  next: { path: string; label: string } | null = { path: '/docs/03-functional-analysis/n8n-automations', label: 'N8n Automations' };
}
