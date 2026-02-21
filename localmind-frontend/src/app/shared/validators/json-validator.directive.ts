import { Directive } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl, ValidationErrors } from '@angular/forms';

@Directive({
  selector: '[appJsonValidator]',
  standalone: true,
  providers: [{ provide: NG_VALIDATORS, useExisting: JsonValidatorDirective, multi: true }]
})
export class JsonValidatorDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    try { JSON.parse(control.value); return null; } catch { return { appJsonValidator: true }; }
  }
}
