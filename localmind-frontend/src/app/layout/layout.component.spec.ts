import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { LayoutComponent } from './layout.component';

describe('LayoutComponent', () => {
  let component: LayoutComponent;
  let fixture: ComponentFixture<LayoutComponent>;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LayoutComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(component).toBeTruthy();
  });

  it('sidebar ha le voci di navigazione attese / sidebar has expected navigation items', () => {
    const links = fixture.nativeElement.querySelectorAll('.nav-links a');
    const hrefs = Array.from(links).map((a: any) => a.getAttribute('href'));

    expect(hrefs).toContain('/dashboard');
    expect(hrefs).toContain('/chat');
    expect(hrefs).toContain('/documents');
    expect(hrefs).toContain('/search');
    expect(hrefs).toContain('/folders');
    expect(hrefs).toContain('/settings');
  });

  it('contiene router-outlet / contains router-outlet', () => {
    const outlet = fixture.nativeElement.querySelector('router-outlet');
    expect(outlet).toBeTruthy();
  });

  it('toggle sidebar cambia stato collapsed / toggle sidebar changes collapsed state', () => {
    expect(component.collapsed()).toBe(false);

    const collapseBtn = fixture.nativeElement.querySelector('.collapse-btn');
    collapseBtn.click();
    fixture.detectChanges();

    expect(component.collapsed()).toBe(true);

    collapseBtn.click();
    fixture.detectChanges();

    expect(component.collapsed()).toBe(false);
  });

  it('mostra brand LocalMind / shows brand LocalMind', () => {
    const brandLogo = fixture.nativeElement.querySelector('.brand-logo');
    expect(brandLogo).toBeTruthy();
    expect(brandLogo.textContent.trim()).toBe('LM');

    const brandText = fixture.nativeElement.querySelector('.brand-text');
    expect(brandText).toBeTruthy();
    expect(brandText.textContent.trim()).toBe('LocalMind');
  });
});
