import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyStateComponent } from './empty-state.component';

describe('EmptyStateComponent', () => {
  let component: EmptyStateComponent;
  let fixture: ComponentFixture<EmptyStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyStateComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyStateComponent);
    component = fixture.componentInstance;
    // title e' required, lo impostiamo tramite componentRef
    fixture.componentRef.setInput('title', 'Nessun risultato');
    fixture.detectChanges();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(component).toBeTruthy();
  });

  it('mostra il titolo passato come input / shows title passed as input', () => {
    const titleEl = fixture.nativeElement.querySelector('.empty-title');
    expect(titleEl.textContent).toContain('Nessun risultato');
  });

  it('mostra la descrizione se presente / shows description if provided', () => {
    fixture.componentRef.setInput('description', 'Prova a cercare qualcos\'altro');
    fixture.detectChanges();

    const descEl = fixture.nativeElement.querySelector('.empty-desc');
    expect(descEl).toBeTruthy();
    expect(descEl.textContent).toContain('Prova a cercare qualcos\'altro');
  });

  it('non mostra la descrizione se vuota / does not show description if empty', () => {
    fixture.componentRef.setInput('description', '');
    fixture.detectChanges();

    const descEl = fixture.nativeElement.querySelector('.empty-desc');
    expect(descEl).toBeNull();
  });

  it('mostra icona SVG / shows SVG icon', () => {
    const svgEl = fixture.nativeElement.querySelector('.empty-icon svg');
    expect(svgEl).toBeTruthy();
  });
});
