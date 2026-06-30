import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/site-layout.component').then(m => m.SiteLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
      },
      {
        path: 'docs',
        loadChildren: () => import('./features/docs/docs.routes').then(m => m.DOCS_ROUTES)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
