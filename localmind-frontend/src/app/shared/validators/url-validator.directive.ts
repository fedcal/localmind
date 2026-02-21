import { Directive } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl, ValidationErrors } from '@angular/forms';

@Directive({
  selector: '[appUrlValidator]',
  standalone: true,
  providers: [{ provide: NG_VALIDATORS, useExisting: UrlValidatorDirective, multi: true }]
})
export class UrlValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    return /^https?:\/\/.+/i.test(control.value) ? null : { appUrlValidator: true };
  }
}
