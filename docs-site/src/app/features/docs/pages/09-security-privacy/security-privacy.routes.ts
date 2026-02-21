import { Routes } from '@angular/router';

export const SECURITYPRIVACY_ROUTES: Routes = [
  { path: '', redirectTo: 'security-principles', pathMatch: 'full' },
    { path: 'security-principles', loadComponent: () => import('./security-principles.component').then(m => m.SecurityPrinciplesComponent) },
    { path: 'spring-security-config', loadComponent: () => import('./spring-security-config.component').then(m => m.SpringSecurityConfigComponent) },
    { path: 'credential-management', loadComponent: () => import('./credential-management.component').then(m => m.CredentialManagementComponent) }
];
