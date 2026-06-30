import { Component, computed, inject } from '@angular/core';
import { MarkdownComponent } from 'ngx-markdown';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { DocNavComponent } from '../../../../shared/components/doc-nav/doc-nav.component';
import { content as contentEN } from '../../content/en/15-extension-domains/local-services-health';
import { content as contentIT } from '../../content/it/15-extension-domains/local-services-health';

@Component({
  selector: 'app-doc-local-services-health',
  standalone: true,
  imports: [MarkdownComponent, DocNavComponent],
  template: `
    <article class="doc-content">
      <markdown [data]="content()"></markdown>
    </article>
    <app-doc-nav [prev]="prev" [next]="next"></app-doc-nav>
  `
})
export class LocalServicesHealthComponent {
  private i18n = inject(TranslationService);
  private contentMap: Record<string, string> = { en: contentEN, it: contentIT };

  content = computed(() => {
    const lang = this.i18n.currentLang();
    return this.contentMap[lang] ?? this.contentMap['en'] ?? '';
  });

  prev: { path: string; label: string } | null = { path: '/docs/15-extension-domains/real-estate', label: 'Real Estate' };
  next: { path: string; label: string } | null = { path: '/docs/15-extension-domains/enterprise-knowledge-base', label: 'Enterprise Knowledge Base' };
}
