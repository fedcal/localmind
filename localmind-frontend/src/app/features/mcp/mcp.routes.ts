import { Routes } from '@angular/router';

export const MCP_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/mcp-dashboard.component').then(m => m.McpDashboardComponent),
    children: [
      { path: '', redirectTo: 'servers', pathMatch: 'full' },
      {
        path: 'servers',
        loadComponent: () => import('./pages/mcp-servers.component').then(m => m.McpServersComponent)
      },
      {
        path: 'tools',
        loadComponent: () => import('./pages/mcp-tools.component').then(m => m.McpToolsComponent)
      }
    ]
  }
];
