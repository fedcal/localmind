import { Component, computed, inject } from '@angular/core';
import { MarkdownComponent } from 'ngx-markdown';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { DocNavComponent } from '../../../../shared/components/doc-nav/doc-nav.component';
import { content as contentEN } from '../../content/en/15-extension-domains/sports-outdoor';
import { content as contentIT } from '../../content/it/15-extension-domains/sports-outdoor';

@Component({
  selector: 'app-doc-sports-outdoor',
  standalone: true,
  imports: [MarkdownComponent, DocNavComponent],
  template: `
    <article class="doc-content">
      <markdown [data]="content()"></markdown>
    </article>
    <app-doc-nav [prev]="prev" [next]="next"></app-doc-nav>
  `
})
export class SportsOutdoorComponent {
  private i18n = inject(TranslationService);
  private contentMap: Record<string, string> = { en: contentEN, it: contentIT };

  content = computed(() => {
    const lang = this.i18n.currentLang();
    return this.contentMap[lang] ?? this.contentMap['en'] ?? '';
  });

  prev: { path: string; label: string } | null = { path: '/docs/15-extension-domains/culture-art-museums', label: 'Culture Art Museums' };
  next: { path: string; label: string } | null = { path: '/docs/15-extension-domains/local-commerce-shopping', label: 'Local Commerce Shopping' };
}
