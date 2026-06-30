import { Routes } from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/settings-page/settings-page.component').then(m => m.SettingsPageComponent)
  },
  {
    path: 'webhooks',
    loadComponent: () => import('./pages/webhooks-page/webhooks-page.component').then(m => m.WebhooksPageComponent)
  }
];
