import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '../core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from '../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, LanguageSwitcherComponent],
  template: `
    <div class="layout" [class.collapsed]="collapsed()">
      <nav class="sidebar">
        <div class="sidebar-brand">
          <div class="brand-logo">LM</div>
          @if (!collapsed()) {
            <span class="brand-text">LocalMind</span>
          }
        </div>

        <button class="collapse-btn" (click)="collapsed.set(!collapsed())">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            @if (collapsed()) {
              <path d="M9 18l6-6-6-6"/>
            } @else {
              <path d="M15 18l-6-6 6-6"/>
            }
          </svg>
        </button>

        <ul class="nav-links">
          <li>
            <a routerLink="/dashboard" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
                <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.DASHBOARD' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/chat" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.CHAT' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/documents" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.DOCUMENTS' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/search" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.SEARCH' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/folders" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.FOLDERS' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/mcp" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.MCP' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/settings" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.SETTINGS' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/guide" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.GUIDE' | translate }}</span> }
            </a>
          </li>
        </ul>

        <div class="sidebar-footer">
          <app-language-switcher [collapsed]="collapsed()" />
          @if (!collapsed()) {
            <span class="version">v0.1.0</span>
          }
        </div>
      </nav>
      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .layout {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }
    .sidebar {
      width: var(--sidebar-width);
      background: var(--color-sidebar);
      color: white;
      display: flex;
      flex-direction: column;
      transition: width var(--transition-slow);
      overflow: hidden;
      flex-shrink: 0;
    }
    .collapsed .sidebar {
      width: var(--sidebar-collapsed-width);
    }
    .sidebar-brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1.25rem 1rem;
      border-bottom: 1px solid rgba(255,255,255,0.08);
    }
    .brand-logo {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, #0f3460, #3498db);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 0.85rem;
      letter-spacing: 0.5px;
      flex-shrink: 0;
    }
    .brand-text {
      font-size: 1.15rem;
      font-weight: 700;
      letter-spacing: -0.3px;
      white-space: nowrap;
    }
    .collapse-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0.5rem 0.75rem;
      padding: 0.4rem;
      background: rgba(255,255,255,0.05);
      border: none;
      border-radius: 6px;
      color: rgba(255,255,255,0.5);
      cursor: pointer;
      transition: all var(--transition-fast);
    }
    .collapse-btn:hover {
      background: rgba(255,255,255,0.1);
      color: white;
    }
    .nav-links {
      list-style: none;
      padding: 0.5rem 0.5rem;
      margin: 0;
      flex: 1;
      overflow-y: auto;
    }
    .nav-links li {
      margin-bottom: 2px;
    }
    .nav-links a {
      color: rgba(255,255,255,0.55);
      text-decoration: none;
      padding: 0.6rem 0.75rem;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      border-radius: 8px;
      transition: all var(--transition-fast);
      font-size: 0.875rem;
      font-weight: 500;
      white-space: nowrap;
    }
    .nav-links a svg {
      flex-shrink: 0;
    }
    .nav-links a:hover {
      background: rgba(255,255,255,0.08);
      color: rgba(255,255,255,0.9);
    }
    .nav-links a.active {
      background: var(--color-sidebar-active);
      color: white;
    }
    .sidebar-footer {
      padding: 0.75rem 1rem;
      border-top: 1px solid rgba(255,255,255,0.08);
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      align-items: center;
    }
    .version {
      font-size: 0.7rem;
      color: rgba(255,255,255,0.3);
    }
    .content {
      flex: 1;
      overflow-y: auto;
      padding: var(--space-xl);
      background: var(--color-bg);
      min-width: 0;
    }
  `]
})
export class LayoutComponent {
  collapsed = signal(false);
}
