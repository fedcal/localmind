import { Routes } from '@angular/router';

export const FUNCTIONALANALYSIS_ROUTES: Routes = [
  { path: '', redirectTo: 'llm-gateway', pathMatch: 'full' },
    { path: 'llm-gateway', loadComponent: () => import('./llm-gateway.component').then(m => m.LlmGatewayComponent) },
    { path: 'document-intelligence-rag', loadComponent: () => import('./document-intelligence-rag.component').then(m => m.DocumentIntelligenceRagComponent) },
    { path: 'ai-agents', loadComponent: () => import('./ai-agents.component').then(m => m.AiAgentsComponent) },
    { path: 'n8n-automations', loadComponent: () => import('./n8n-automations.component').then(m => m.N8nAutomationsComponent) },
    { path: 'user-interface', loadComponent: () => import('./user-interface.component').then(m => m.UserInterfaceComponent) },
    { path: 'monitoring-dashboard', loadComponent: () => import('./monitoring-dashboard.component').then(m => m.MonitoringDashboardComponent) }
];
