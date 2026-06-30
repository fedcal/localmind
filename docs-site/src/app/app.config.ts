import { ApplicationConfig, APP_INITIALIZER, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withHashLocation, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideMarkdown } from 'ngx-markdown';
import { routes } from './app.routes';
import { TranslationService } from './core/i18n/translation.service';

function initTranslations(i18n: TranslationService) {
  return () => i18n.init();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withHashLocation(),
      withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: 'top' })
    ),
    provideHttpClient(),
    provideAnimationsAsync(),
    provideMarkdown(),
    {
      provide: APP_INITIALIZER,
      useFactory: initTranslations,
      deps: [TranslationService],
      multi: true
    }
  ]
};
