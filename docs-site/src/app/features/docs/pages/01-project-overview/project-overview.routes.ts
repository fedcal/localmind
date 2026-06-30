import { Routes } from '@angular/router';

export const PROJECTOVERVIEW_ROUTES: Routes = [
  { path: '', redirectTo: 'vision-and-objectives', pathMatch: 'full' },
    { path: 'vision-and-objectives', loadComponent: () => import('./vision-and-objectives.component').then(m => m.VisionAndObjectivesComponent) },
    { path: 'target-users', loadComponent: () => import('./target-users.component').then(m => m.TargetUsersComponent) },
    { path: 'value-proposition', loadComponent: () => import('./value-proposition.component').then(m => m.ValuePropositionComponent) }
];
