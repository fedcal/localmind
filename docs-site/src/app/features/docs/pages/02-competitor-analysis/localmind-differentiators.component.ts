import { Component, computed, inject } from '@angular/core';
import { MarkdownComponent } from 'ngx-markdown';
import { TranslationService } from '../../../../core/i18n/translation.service';
import { DocNavComponent } from '../../../../shared/components/doc-nav/doc-nav.component';
import { content as contentEN } from '../../content/en/02-competitor-analysis/localmind-differentiators';
import { content as contentIT } from '../../content/it/02-competitor-analysis/localmind-differentiators';

@Component({
  selector: 'app-doc-localmind-differentiators',
  standalone: true,
  imports: [MarkdownComponent, DocNavComponent],
  template: `
    <article class="doc-content">
      <markdown [data]="content()"></markdown>
    </article>
    <app-doc-nav [prev]="prev" [next]="next"></app-doc-nav>
  `
})
export class LocalmindDifferentiatorsComponent {
  private i18n = inject(TranslationService);
  private contentMap: Record<string, string> = { en: contentEN, it: contentIT };

  content = computed(() => {
    const lang = this.i18n.currentLang();
    return this.contentMap[lang] ?? this.contentMap['en'] ?? '';
  });

  prev: { path: string; label: string } | null = { path: '/docs/02-competitor-analysis/detailed-comparison', label: 'Detailed Comparison' };
  next: { path: string; label: string } | null = { path: '/docs/02-competitor-analysis/openclaw-comparison', label: 'Openclaw Comparison' };
}
