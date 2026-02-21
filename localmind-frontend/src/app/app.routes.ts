import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login-page/login-page.component').then(m => m.LoginPageComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layout/layout.component').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'chat', pathMatch: 'full' },
      {
        path: 'chat',
        loadChildren: () => import('./features/chat/chat.routes').then(m => m.CHAT_ROUTES)
      },
      {
        path: 'documents',
        loadChildren: () => import('./features/documents/documents.routes').then(m => m.DOCUMENT_ROUTES)
      },
      {
        path: 'search',
        loadChildren: () => import('./features/search/search.routes').then(m => m.SEARCH_ROUTES)
      },
      {
        path: 'folders',
        loadChildren: () => import('./features/folders/folders.routes').then(m => m.FOLDER_ROUTES)
      },
      {
        path: 'settings',
        loadChildren: () => import('./features/settings/settings.routes').then(m => m.SETTINGS_ROUTES)
      },
      {
        path: 'mcp',
        loadChildren: () => import('./features/mcp/mcp.routes').then(m => m.MCP_ROUTES)
      },
      {
        path: 'plugins',
        loadChildren: () => import('./features/plugins/plugins.routes').then(m => m.PLUGINS_ROUTES)
      },
      {
        path: 'backup',
        loadChildren: () => import('./features/backup/backup.routes').then(m => m.BACKUP_ROUTES)
      },
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
      },
      {
        path: 'calendar',
        loadChildren: () => import('./features/calendar/calendar.routes').then(m => m.CALENDAR_ROUTES)
      },
      {
        path: 'email',
        loadChildren: () => import('./features/email/email.routes').then(m => m.EMAIL_ROUTES)
      },
      {
        path: 'knowledge',
        loadChildren: () => import('./features/knowledge/knowledge.routes').then(m => m.KNOWLEDGE_ROUTES)
      },
      {
        path: 'finetuning',
        loadChildren: () => import('./features/finetuning/finetuning.routes').then(m => m.FINETUNING_ROUTES)
      },
      {
        path: 'marketplace',
        loadChildren: () => import('./features/marketplace/marketplace.routes').then(m => m.MARKETPLACE_ROUTES)
      },
      {
        path: 'guide',
        loadChildren: () => import('./features/guide/guide.routes').then(m => m.GUIDE_ROUTES)
      }
    ]
  },
  {
    path: '**',
    loadComponent: () => import('./features/not-found/not-found-page.component').then(m => m.NotFoundPageComponent)
  }
];
