import { Routes } from '@angular/router';

export const APIREFERENCE_ROUTES: Routes = [
  { path: '', redirectTo: 'api-overview', pathMatch: 'full' },
    { path: 'api-overview', loadComponent: () => import('./api-overview.component').then(m => m.ApiOverviewComponent) },
    { path: 'chat-api', loadComponent: () => import('./chat-api.component').then(m => m.ChatApiComponent) },
    { path: 'documents-api', loadComponent: () => import('./documents-api.component').then(m => m.DocumentsApiComponent) },
    { path: 'models-api', loadComponent: () => import('./models-api.component').then(m => m.ModelsApiComponent) },
    { path: 'dashboard-api', loadComponent: () => import('./dashboard-api.component').then(m => m.DashboardApiComponent) },
    { path: 'webhooks-api', loadComponent: () => import('./webhooks-api.component').then(m => m.WebhooksApiComponent) }
];
