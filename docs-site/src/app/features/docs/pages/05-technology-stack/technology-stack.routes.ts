import { Routes } from '@angular/router';

export const TECHNOLOGYSTACK_ROUTES: Routes = [
  { path: '', redirectTo: 'backend-stack', pathMatch: 'full' },
    { path: 'backend-stack', loadComponent: () => import('./backend-stack.component').then(m => m.BackendStackComponent) },
    { path: 'frontend-stack', loadComponent: () => import('./frontend-stack.component').then(m => m.FrontendStackComponent) },
    { path: 'docker-infrastructure', loadComponent: () => import('./docker-infrastructure.component').then(m => m.DockerInfrastructureComponent) },
    { path: 'backend-testing', loadComponent: () => import('./backend-testing.component').then(m => m.BackendTestingComponent) },
    { path: 'functional-testing', loadComponent: () => import('./functional-testing.component').then(m => m.FunctionalTestingComponent) }
];
