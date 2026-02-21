import { Component, Input } from '@angular/core';
import { NgModel } from '@angular/forms';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-field-error',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    @if (control && control.invalid && control.touched) {
      <span class="form-error">
        @if (control.errors?.['required']) {
          {{ 'VALIDATION.REQUIRED' | translate }}
        } @else if (control.errors?.['minlength']) {
          {{ 'VALIDATION.MIN_LENGTH' | translate:{ min: control.errors?.['minlength']?.requiredLength } }}
        } @else if (control.errors?.['maxlength']) {
          {{ 'VALIDATION.MAX_LENGTH' | translate:{ max: control.errors?.['maxlength']?.requiredLength } }}
        } @else if (control.errors?.['appUrlValidator']) {
          {{ 'VALIDATION.INVALID_URL' | translate }}
        } @else if (control.errors?.['min']) {
          {{ 'VALIDATION.MIN_VALUE' | translate:{ min: control.errors?.['min']?.min } }}
        } @else if (control.errors?.['max']) {
          {{ 'VALIDATION.MAX_VALUE' | translate:{ max: control.errors?.['max']?.max } }}
        } @else if (control.errors?.['appJsonValidator']) {
          {{ 'VALIDATION.INVALID_JSON' | translate }}
        } @else if (control.errors?.['appDateAfter']) {
          {{ 'VALIDATION.DATE_AFTER' | translate }}
        }
      </span>
    }
  `,
  styles: [`.form-error { font-size: 0.75rem; color: var(--color-error, #e53935); margin-top: 0.15rem; display: block; }`]
})
export class FieldErrorComponent {
  @Input() control: NgModel | null = null;
}
