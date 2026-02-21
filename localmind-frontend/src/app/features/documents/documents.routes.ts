import { Routes } from '@angular/router';

export const DOCUMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/document-list-page/document-list-page.component').then(m => m.DocumentListPageComponent)
  }
];
