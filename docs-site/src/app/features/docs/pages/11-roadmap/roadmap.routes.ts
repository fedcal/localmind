import { Routes } from '@angular/router';

export const ROADMAP_ROUTES: Routes = [
  { path: '', redirectTo: 'development-phases', pathMatch: 'full' },
    { path: 'development-phases', loadComponent: () => import('./development-phases.component').then(m => m.DevelopmentPhasesComponent) },
    { path: 'future-evolution', loadComponent: () => import('./future-evolution.component').then(m => m.FutureEvolutionComponent) }
];
