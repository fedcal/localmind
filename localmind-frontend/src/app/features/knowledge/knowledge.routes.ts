import { Routes } from '@angular/router';

export const KNOWLEDGE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/knowledge-page/knowledge-page.component').then(m => m.KnowledgePageComponent)
  }
];
