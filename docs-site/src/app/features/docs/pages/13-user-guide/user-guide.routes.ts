import { Routes } from '@angular/router';

export const USERGUIDE_ROUTES: Routes = [
  { path: '', redirectTo: 'introduction', pathMatch: 'full' },
    { path: 'introduction', loadComponent: () => import('./introduction.component').then(m => m.IntroductionComponent) },
    { path: 'dashboard', loadComponent: () => import('./dashboard.component').then(m => m.DashboardComponent) },
    { path: 'chat', loadComponent: () => import('./chat.component').then(m => m.ChatComponent) },
    { path: 'documents', loadComponent: () => import('./documents.component').then(m => m.DocumentsComponent) },
    { path: 'semantic-search', loadComponent: () => import('./semantic-search.component').then(m => m.SemanticSearchComponent) },
    { path: 'monitored-folders', loadComponent: () => import('./monitored-folders.component').then(m => m.MonitoredFoldersComponent) },
    { path: 'provider-settings', loadComponent: () => import('./provider-settings.component').then(m => m.ProviderSettingsComponent) },
    { path: 'mcp-integration', loadComponent: () => import('./mcp-integration.component').then(m => m.McpIntegrationComponent) }
];
