import { Routes } from '@angular/router';

export const FOLDER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/folder-config-page/folder-config-page.component').then(m => m.FolderConfigPageComponent)
  }
];
