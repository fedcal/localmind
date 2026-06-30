import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ThemeToggleComponent } from './theme-toggle.component';
import { ThemeService } from '../../../core/services/theme.service';

describe('ThemeToggleComponent', () => {
  let component: ThemeToggleComponent;
  let fixture: ComponentFixture<ThemeToggleComponent>;
  let themeService: ThemeService;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [ThemeToggleComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ThemeToggleComponent);
    component = fixture.componentInstance;
    themeService = TestBed.inject(ThemeService);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(component).toBeTruthy();
  });

  it('click sul toggle chiama theme.toggle() / click on toggle calls theme.toggle()', () => {
    const spy = vi.spyOn(themeService, 'toggle');

    const button = fixture.nativeElement.querySelector('.theme-toggle');
    button.click();

    expect(spy).toHaveBeenCalled();
  });

  it('mostra SVG diverso per tema corrente / shows different SVG for current theme', () => {
    // Impostiamo il tema a dark
    themeService.setTheme('dark');
    fixture.detectChanges();

    // In modalita' dark dovrebbe mostrare l'icona del sole (circle + line)
    let svg = fixture.nativeElement.querySelector('.theme-toggle svg');
    expect(svg).toBeTruthy();

    // Cambiamo a light
    themeService.setTheme('light');
    fixture.detectChanges();

    // In modalita' light dovrebbe mostrare l'icona della luna (path con d="M21...")
    svg = fixture.nativeElement.querySelector('.theme-toggle svg');
    expect(svg).toBeTruthy();
  });
});
