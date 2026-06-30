import { Routes } from '@angular/router';

export const SECURITY_ROUTES: Routes = [
  { path: '', redirectTo: 'local-authentication', pathMatch: 'full' },
    { path: 'local-authentication', loadComponent: () => import('./local-authentication.component').then(m => m.LocalAuthenticationComponent) }
];
