import { Pipe, PipeTransform, inject, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { TranslationService } from './translation.service';
import { Subscription } from 'rxjs';

@Pipe({
  name: 'translate',
  standalone: true,
  pure: false
})
export class TranslatePipe implements PipeTransform, OnDestroy {
  private i18n = inject(TranslationService);
  private cdr = inject(ChangeDetectorRef);
  private sub: Subscription;

  constructor() {
    this.sub = this.i18n.langChange.subscribe(() => {
      this.cdr.markForCheck();
    });
  }

  transform(key: string, params?: Record<string, any>): string {
    return this.i18n.instant(key, params);
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
