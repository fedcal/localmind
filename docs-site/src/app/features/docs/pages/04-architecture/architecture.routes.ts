import { Routes } from '@angular/router';

export const ARCHITECTURE_ROUTES: Routes = [
  { path: '', redirectTo: 'general-architecture', pathMatch: 'full' },
    { path: 'general-architecture', loadComponent: () => import('./general-architecture.component').then(m => m.GeneralArchitectureComponent) },
    { path: 'hexagonal-architecture', loadComponent: () => import('./hexagonal-architecture.component').then(m => m.HexagonalArchitectureComponent) },
    { path: 'maven-modules', loadComponent: () => import('./maven-modules.component').then(m => m.MavenModulesComponent) },
    { path: 'flow-diagrams', loadComponent: () => import('./flow-diagrams.component').then(m => m.FlowDiagramsComponent) },
    { path: 'rag-pipeline', loadComponent: () => import('./rag-pipeline.component').then(m => m.RagPipelineComponent) }
];
