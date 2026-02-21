import { Routes } from '@angular/router';

export const EMAIL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/email-page/email-page.component').then(m => m.EmailPageComponent)
  }
];
