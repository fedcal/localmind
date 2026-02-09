# Internazionalizzazione (i18n) Frontend

**Data**: 2026-02-09
**Stato**: Completato

## Problema

Il frontend Angular 21 di LocalMind aveva ~500+ stringhe italiane hardcoded in 11 componenti. Non esisteva alcuna infrastruttura per il supporto multilingua. L'obiettivo era aggiungere supporto bilingue italiano/inglese con switch runtime senza reload della pagina.

## Soluzione

Implementazione di un sistema i18n custom (senza dipendenze esterne) per massima compatibilita' con Angular 21:

### Infrastruttura creata

| File | Descrizione |
|------|-------------|
| `src/app/core/i18n/translation.service.ts` | Servizio singleton: carica JSON via HttpClient, gestisce cambio lingua, persiste in localStorage |
| `src/app/core/i18n/translate.pipe.ts` | Pipe impura standalone per i template: `{{ 'KEY' \| translate }}` |
| `src/app/shared/components/language-switcher/language-switcher.component.ts` | Toggle IT/EN nella sidebar |
| `public/assets/i18n/it.json` | 508 chiavi di traduzione italiane |
| `public/assets/i18n/en.json` | 508 chiavi di traduzione inglesi |

### Componenti modificati (11 totali)

1. `app.config.ts` - APP_INITIALIZER per caricare traduzioni all'avvio
2. `layout/layout.component.ts` - Sidebar labels + LanguageSwitcher
3. `features/chat/pages/chat-page/chat-page.component.ts` - ~15 stringhe
4. `features/search/pages/search-page/search-page.component.ts` - ~15 stringhe
5. `features/dashboard/pages/dashboard-page/dashboard-page.component.ts` - ~20 stringhe
6. `features/documents/pages/document-list-page/document-list-page.component.ts` - ~30 stringhe + enum
7. `features/folders/pages/folder-config-page/folder-config-page.component.ts` - ~25 stringhe
8. `features/settings/pages/settings-page/settings-page.component.ts` - ~30 stringhe
9. `features/mcp/pages/mcp-dashboard.component.ts` - ~5 stringhe
10. `features/mcp/pages/mcp-servers.component.ts` - ~30 stringhe + enum
11. `features/mcp/pages/mcp-tools.component.ts` - ~15 stringhe
12. `features/guide/pages/guide-page/guide-page.component.ts` - ~150+ stringhe

### Pattern utilizzati

- **Chiavi flat dot-separated**: `NAV.DASHBOARD`, `CHAT.WELCOME_TITLE`, `ENUM.DOCUMENT_STATUS.INDEXED`
- **Interpolazione**: `{{ 'DOCUMENTS.COUNT' | translate:{ count: 5 } }}` -> "5 documenti" / "5 documents"
- **HTML inline**: `[innerHTML]="'GUIDE.START.STEP1_DESC' | translate"` per contenuti con `<strong>`, `<kbd>`, `<code>`
- **Enum frontend-only**: `{{ 'ENUM.DOCUMENT_STATUS.' + doc.status | translate }}`
- **Date locale-aware**: `this.i18n.currentLang() === 'it' ? 'it-IT' : 'en-US'`
- **Notifiche tradotte**: `this.i18n.instant('DOCUMENTS.UPLOAD_SUCCESS', { filename })`
- **Persistenza**: `localStorage.getItem('localmind-lang')`, default `'it'`

### Funzionamento TranslationService

```typescript
// Caratteristiche principali:
- currentLang: WritableSignal<'it' | 'en'>
- langChange: Subject<string>  // notifica cambio lingua
- use(lang): void              // cambia lingua, salva localStorage, ricarica JSON
- instant(key, params?): string // lookup sincrono con interpolazione
- stream(key, params?): Observable<string> // si aggiorna al cambio lingua
```

La pipe `TranslatePipe` e' impura (`pure: false`) e sottoscrive `langChange` per aggiornare automaticamente tutti i binding quando la lingua cambia.

## Checkpoint

- [x] Fase 1: Infrastruttura i18n (TranslationService, TranslatePipe, JSON, app.config)
- [x] Fase 2: Layout + Navigazione sidebar (~15 stringhe)
- [x] Fase 3: Chat, Search, Dashboard, MCP dashboard (~60 stringhe)
- [x] Fase 4: Documents, Folders, Settings (~85 stringhe)
- [x] Fase 5: MCP Servers + MCP Tools (~45 stringhe)
- [x] Fase 6: Guida utente (~150+ stringhe)
- [x] Fase 7: Build verification - compilazione senza errori

## Verifica

- `ng build` completato senza errori
- Tutti i 12 componenti modificati compilano correttamente
- 508 chiavi di traduzione in ciascun JSON (it.json, en.json)
- Nessuna dipendenza esterna aggiunta
