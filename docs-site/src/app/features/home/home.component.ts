import { Component } from '@angular/core';
import { HeroComponent } from './sections/hero.component';
import { FeaturesComponent } from './sections/features.component';
import { StatsComponent } from './sections/stats.component';
import { ScreenshotsComponent } from './sections/screenshots.component';
import { TechStackComponent } from './sections/tech-stack.component';
import { QuickStartComponent } from './sections/quick-start.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    HeroComponent,
    FeaturesComponent,
    StatsComponent,
    ScreenshotsComponent,
    TechStackComponent,
    QuickStartComponent
  ],
  template: `
    <main class="home-page">
      <app-hero />
      <app-features />
      <app-stats />
      <app-screenshots />
      <app-tech-stack />
      <app-quick-start />
    </main>
  `,
  styles: [`
    .home-page {
      width: 100%;
      overflow-x: hidden;
    }
  `]
})
export class HomeComponent {}
