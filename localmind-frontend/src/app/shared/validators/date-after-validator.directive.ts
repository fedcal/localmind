import { Directive, Input } from '@angular/core';
import { NG_VALIDATORS, Validator, AbstractControl, ValidationErrors } from '@angular/forms';

@Directive({
  selector: '[appDateAfter]',
  standalone: true,
  providers: [{ provide: NG_VALIDATORS, useExisting: DateAfterValidatorDirective, multi: true }]
})
export class DateAfterValidatorDirective implements Validator {
  @Input() appDateAfter: string = '';
  validate(control: AbstractControl): ValidationErrors | null {
    if (!control.value || !this.appDateAfter) return null;
    return control.value > this.appDateAfter ? null : { appDateAfter: true };
  }
}
