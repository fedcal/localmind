import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';

@Component({
  selector: 'app-site-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  template: `
    <div class="site-layout">
      <app-header />
      <main class="main-content">
        <router-outlet />
      </main>
      <app-footer />
    </div>
  `,
  styles: [`
    .site-layout {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .main-content {
      flex: 1;
    }
  `]
})
export class SiteLayoutComponent {}
