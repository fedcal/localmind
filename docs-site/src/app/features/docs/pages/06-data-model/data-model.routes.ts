import { Routes } from '@angular/router';

export const DATAMODEL_ROUTES: Routes = [
  { path: '', redirectTo: 'database-schema', pathMatch: 'full' },
    { path: 'database-schema', loadComponent: () => import('./database-schema.component').then(m => m.DatabaseSchemaComponent) },
    { path: 'jpa-entities', loadComponent: () => import('./jpa-entities.component').then(m => m.JpaEntitiesComponent) },
    { path: 'flyway-migrations', loadComponent: () => import('./flyway-migrations.component').then(m => m.FlywayMigrationsComponent) }
];
