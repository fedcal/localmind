import { Routes } from '@angular/router';

export const COMPETITORANALYSIS_ROUTES: Routes = [
  { path: '', redirectTo: 'market-overview', pathMatch: 'full' },
    { path: 'market-overview', loadComponent: () => import('./market-overview.component').then(m => m.MarketOverviewComponent) },
    { path: 'detailed-comparison', loadComponent: () => import('./detailed-comparison.component').then(m => m.DetailedComparisonComponent) },
    { path: 'localmind-differentiators', loadComponent: () => import('./localmind-differentiators.component').then(m => m.LocalmindDifferentiatorsComponent) },
    { path: 'openclaw-comparison', loadComponent: () => import('./openclaw-comparison.component').then(m => m.OpenclawComparisonComponent) }
];
