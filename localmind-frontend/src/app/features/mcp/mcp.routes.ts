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
      },
      {
        path: 'scrum',
        loadComponent: () => import('./pages/mcp-scrum.component').then(m => m.McpScrumComponent)
      },
      {
        path: 'incidents',
        loadComponent: () => import('./pages/mcp-incidents.component').then(m => m.McpIncidentsComponent)
      },
      {
        path: 'time',
        loadComponent: () => import('./pages/mcp-time.component').then(m => m.McpTimeComponent)
      }
    ]
  }
];
