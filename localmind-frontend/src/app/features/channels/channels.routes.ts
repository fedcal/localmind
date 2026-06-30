import { Routes } from '@angular/router';

export const CHANNELS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/channels-page/channels-page.component')
      .then(m => m.ChannelsPageComponent)
  }
];
