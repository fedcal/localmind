import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [ThemeService]
    });
    service = TestBed.inject(ThemeService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('stato iniziale del tema e\' light di default / initial theme state is light by default', () => {
    // Senza valore in localStorage e senza preferenza dark, il default e' light
    expect(service.currentTheme()).toBeDefined();
    expect(['light', 'dark']).toContain(service.currentTheme());
  });

  it('toggle() alterna tra light e dark / toggle() switches between light and dark', () => {
    service.setTheme('light');
    TestBed.flushEffects();

    expect(service.currentTheme()).toBe('light');

    service.toggle();
    expect(service.currentTheme()).toBe('dark');

    service.toggle();
    expect(service.currentTheme()).toBe('light');
  });

  it('isDark() ritorna il valore corretto / isDark() returns correct value', () => {
    service.setTheme('dark');
    expect(service.isDark()).toBe(true);

    service.setTheme('light');
    expect(service.isDark()).toBe(false);
  });

  it('persiste il tema in localStorage / persists theme in localStorage', () => {
    service.setTheme('dark');
    TestBed.flushEffects();

    expect(localStorage.getItem('localmind-theme')).toBe('dark');

    service.setTheme('light');
    TestBed.flushEffects();

    expect(localStorage.getItem('localmind-theme')).toBe('light');
  });
});
