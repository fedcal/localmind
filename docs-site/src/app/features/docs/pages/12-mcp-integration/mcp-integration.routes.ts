import { Routes } from '@angular/router';

export const MCPINTEGRATION_ROUTES: Routes = [
  { path: '', redirectTo: 'mcp-protocol-overview', pathMatch: 'full' },
    { path: 'mcp-protocol-overview', loadComponent: () => import('./mcp-protocol-overview.component').then(m => m.McpProtocolOverviewComponent) },
    { path: 'server-implementation', loadComponent: () => import('./server-implementation.component').then(m => m.ServerImplementationComponent) },
    { path: 'client-implementation', loadComponent: () => import('./client-implementation.component').then(m => m.ClientImplementationComponent) },
    { path: 'configuration', loadComponent: () => import('./configuration.component').then(m => m.ConfigurationComponent) },
    { path: 'usage-examples', loadComponent: () => import('./usage-examples.component').then(m => m.UsageExamplesComponent) },
    { path: 'agent-integration', loadComponent: () => import('./agent-integration.component').then(m => m.AgentIntegrationComponent) },
    { path: 'troubleshooting', loadComponent: () => import('./troubleshooting.component').then(m => m.TroubleshootingComponent) }
];
