import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslationService, SupportedLang } from './translation.service';

describe('TranslationService', () => {
  let service: TranslationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        TranslationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TranslationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('lingua default e\' it / default language is it', () => {
    expect(service.currentLang()).toBe('it');
  });

  it('use() cambia la lingua corrente / use() changes current language', () => {
    service.use('en');

    expect(service.currentLang()).toBe('en');

    // Il servizio carica le traduzioni per la nuova lingua
    const req = httpMock.expectOne('./assets/i18n/en.json');
    req.flush({ 'TEST_KEY': 'Test Value' });
  });

  it('instant() ritorna la chiave se la traduzione non esiste / instant() returns key if translation missing', () => {
    const result = service.instant('CHIAVE.INESISTENTE');
    expect(result).toBe('CHIAVE.INESISTENTE');
  });

  it('instant() ritorna la traduzione con interpolazione / instant() returns translation with interpolation', () => {
    // Prima carichiamo le traduzioni
    service.init();
    const req = httpMock.expectOne('./assets/i18n/it.json');
    req.flush({ 'GREETING': 'Ciao {{name}}!' });

    const result = service.instant('GREETING', { name: 'Mario' });
    expect(result).toBe('Ciao Mario!');
  });

  it('salva la lingua scelta in localStorage / saves chosen language in localStorage', () => {
    service.use('fr');

    expect(localStorage.getItem('localmind-lang')).toBe('fr');

    const req = httpMock.expectOne('./assets/i18n/fr.json');
    req.flush({});
  });

  it('recupera la lingua salvata dal localStorage / retrieves saved language from localStorage', () => {
    localStorage.setItem('localmind-lang', 'de');

    // Ricreiamo il servizio per leggere dal localStorage
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        TranslationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    const newService = TestBed.inject(TranslationService);

    expect(newService.currentLang()).toBe('de');
  });
});
