import { Routes } from '@angular/router';

export const DEPLOYMENT_ROUTES: Routes = [
  { path: '', redirectTo: 'development-environment', pathMatch: 'full' },
    { path: 'development-environment', loadComponent: () => import('./development-environment.component').then(m => m.DevelopmentEnvironmentComponent) },
    { path: 'production-environment', loadComponent: () => import('./production-environment.component').then(m => m.ProductionEnvironmentComponent) },
    { path: 'docker-compose', loadComponent: () => import('./docker-compose.component').then(m => m.DockerComposeComponent) },
    { path: 'installation-guide', loadComponent: () => import('./installation-guide.component').then(m => m.InstallationGuideComponent) },
    { path: 'startup-scripts', loadComponent: () => import('./startup-scripts.component').then(m => m.StartupScriptsComponent) }
];
