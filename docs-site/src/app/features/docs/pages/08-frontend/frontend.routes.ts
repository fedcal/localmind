import { Routes } from '@angular/router';

export const FRONTEND_ROUTES: Routes = [
  { path: '', redirectTo: 'project-structure', pathMatch: 'full' },
    { path: 'project-structure', loadComponent: () => import('./project-structure.component').then(m => m.ProjectStructureComponent) },
    { path: 'routing-lazy-loading', loadComponent: () => import('./routing-lazy-loading.component').then(m => m.RoutingLazyLoadingComponent) },
    { path: 'state-management-signals', loadComponent: () => import('./state-management-signals.component').then(m => m.StateManagementSignalsComponent) },
    { path: 'feature-modules', loadComponent: () => import('./feature-modules.component').then(m => m.FeatureModulesComponent) },
    { path: 'e2e-testing-playwright', loadComponent: () => import('./e2e-testing-playwright.component').then(m => m.E2eTestingPlaywrightComponent) }
];
