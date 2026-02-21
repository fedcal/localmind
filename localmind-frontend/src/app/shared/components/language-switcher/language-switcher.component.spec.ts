import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LanguageSwitcherComponent } from './language-switcher.component';
import { TranslationService } from '../../../core/i18n/translation.service';

describe('LanguageSwitcherComponent', () => {
  let component: LanguageSwitcherComponent;
  let fixture: ComponentFixture<LanguageSwitcherComponent>;
  let translationService: TranslationService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LanguageSwitcherComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LanguageSwitcherComponent);
    component = fixture.componentInstance;
    translationService = TestBed.inject(TranslationService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(component).toBeTruthy();
  });

  it('mostra 5 bottoni per le lingue / shows 5 language buttons', () => {
    const buttons = fixture.nativeElement.querySelectorAll('.lang-btn');
    expect(buttons.length).toBe(5);

    const labels = Array.from(buttons).map((b: any) => b.textContent.trim());
    expect(labels).toContain('IT');
    expect(labels).toContain('EN');
    expect(labels).toContain('FR');
    expect(labels).toContain('DE');
    expect(labels).toContain('ES');
  });

  it('click su lingua chiama switchLang / click on language calls switchLang', () => {
    const spy = vi.spyOn(component, 'switchLang');

    const enButton = fixture.nativeElement.querySelectorAll('.lang-btn')[1]; // EN
    enButton.click();

    expect(spy).toHaveBeenCalledWith('en');

    // Scartiamo la richiesta HTTP per la traduzione
    const reqs = httpMock.match(() => true);
    reqs.forEach(r => r.flush({}));
  });

  it('lingua corrente ha classe active / current language has active class', () => {
    // La lingua default e' 'it'
    fixture.detectChanges();

    const itButton = fixture.nativeElement.querySelector('.lang-btn.active');
    expect(itButton).toBeTruthy();
    expect(itButton.textContent.trim()).toBe('IT');
  });
});
