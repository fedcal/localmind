import { Routes } from '@angular/router';

export const GUIDE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/guide-page/guide-page.component').then(m => m.GuidePageComponent)
  }
];
