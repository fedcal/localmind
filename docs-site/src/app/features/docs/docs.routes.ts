import { Routes } from '@angular/router';

export const DOCS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./docs-layout.component').then(m => m.DocsLayoutComponent),
    children: [
      { path: '', redirectTo: '01-project-overview/vision-and-objectives', pathMatch: 'full' },
    {
      path: '01-project-overview',
      loadChildren: () => import('./pages/01-project-overview/project-overview.routes').then(m => m.PROJECTOVERVIEW_ROUTES)
    },
    {
      path: '02-competitor-analysis',
      loadChildren: () => import('./pages/02-competitor-analysis/competitor-analysis.routes').then(m => m.COMPETITORANALYSIS_ROUTES)
    },
    {
      path: '03-functional-analysis',
      loadChildren: () => import('./pages/03-functional-analysis/functional-analysis.routes').then(m => m.FUNCTIONALANALYSIS_ROUTES)
    },
    {
      path: '04-architecture',
      loadChildren: () => import('./pages/04-architecture/architecture.routes').then(m => m.ARCHITECTURE_ROUTES)
    },
    {
      path: '05-technology-stack',
      loadChildren: () => import('./pages/05-technology-stack/technology-stack.routes').then(m => m.TECHNOLOGYSTACK_ROUTES)
    },
    {
      path: '06-data-model',
      loadChildren: () => import('./pages/06-data-model/data-model.routes').then(m => m.DATAMODEL_ROUTES)
    },
    {
      path: '06-security',
      loadChildren: () => import('./pages/06-security/security.routes').then(m => m.SECURITY_ROUTES)
    },
    {
      path: '07-api-reference',
      loadChildren: () => import('./pages/07-api-reference/api-reference.routes').then(m => m.APIREFERENCE_ROUTES)
    },
    {
      path: '08-frontend',
      loadChildren: () => import('./pages/08-frontend/frontend.routes').then(m => m.FRONTEND_ROUTES)
    },
    {
      path: '09-security-privacy',
      loadChildren: () => import('./pages/09-security-privacy/security-privacy.routes').then(m => m.SECURITYPRIVACY_ROUTES)
    },
    {
      path: '10-deployment',
      loadChildren: () => import('./pages/10-deployment/deployment.routes').then(m => m.DEPLOYMENT_ROUTES)
    },
    {
      path: '11-roadmap',
      loadChildren: () => import('./pages/11-roadmap/roadmap.routes').then(m => m.ROADMAP_ROUTES)
    },
    {
      path: '12-mcp-integration',
      loadChildren: () => import('./pages/12-mcp-integration/mcp-integration.routes').then(m => m.MCPINTEGRATION_ROUTES)
    },
    {
      path: '13-user-guide',
      loadChildren: () => import('./pages/13-user-guide/user-guide.routes').then(m => m.USERGUIDE_ROUTES)
    }
    ]
  }
];
