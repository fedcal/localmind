import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '../core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from '../shared/components/language-switcher/language-switcher.component';
import { ThemeToggleComponent } from '../shared/components/theme-toggle/theme-toggle.component';
import { OllamaDownloadService } from '../core/services/ollama-download.service';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe, LanguageSwitcherComponent, ThemeToggleComponent],
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
            <a routerLink="/plugins" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.5 7.27783L12 12.0001M12 12.0001L3.49997 7.27783M12 12.0001L12 21.5001M14 20.6699L12.7 21.3699C12.5 21.4899 12.3 21.5001 12 21.5001C11.7 21.5001 11.5 21.4899 11.3 21.3699L4 17.2199C3.6 16.9899 3.5 16.6501 3.5 16.2801V7.72006C3.5 7.35006 3.7 7.01005 4 6.78005L11.3 2.63C11.5 2.51 11.7 2.5 12 2.5C12.3 2.5 12.5 2.51 12.7 2.63L20 6.78005C20.3 7.01005 20.5 7.35006 20.5 7.72006V12.5001"/>
                <path d="M18.5 14.5V17.5M18.5 17.5V20.5M18.5 17.5H15.5M18.5 17.5H21.5"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.PLUGINS' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/calendar" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.CALENDAR' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/email" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.EMAIL' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/knowledge" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="6" cy="6" r="3"/><circle cx="18" cy="6" r="3"/><circle cx="12" cy="18" r="3"/><line x1="8.5" y1="7.5" x2="15.5" y2="7.5"/><line x1="7.5" y1="8.5" x2="10.5" y2="15.5"/><line x1="16.5" y1="8.5" x2="13.5" y2="15.5"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.KNOWLEDGE' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/finetuning" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2a4 4 0 014 4c0 1.1-.9 2-2 2h-4c-1.1 0-2-.9-2-2a4 4 0 014-4z"/><path d="M8 8v2a4 4 0 004 4h0a4 4 0 004-4V8"/><path d="M6 14l-2 4h16l-2-4"/><line x1="8" y1="18" x2="8" y2="22"/><line x1="16" y1="18" x2="16" y2="22"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.FINETUNING' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/marketplace" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 2L3 7v11a2 2 0 002 2h14a2 2 0 002-2V7l-3-5z"/><line x1="3" y1="7" x2="21" y2="7"/><path d="M16 11a4 4 0 01-8 0"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.MARKETPLACE' | translate }}</span> }
            </a>
          </li>
          <li>
            <a routerLink="/backup" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 8v13H3V8"/><path d="M1 3h22v5H1z"/><path d="M10 12h4"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.BACKUP' | translate }}</span> }
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
            <a routerLink="/settings/webhooks" routerLinkActive="active">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10 13a5 5 0 007.54.54l3-3a5 5 0 00-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 00-7.54-.54l-3 3a5 5 0 007.07 7.07l1.71-1.71"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'NAV.WEBHOOKS' | translate }}</span> }
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
          @if (auth.isConfigured()) {
            <button class="logout-btn" (click)="auth.logout()" [title]="'AUTH.LOGOUT' | translate">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              @if (!collapsed()) { <span>{{ 'AUTH.LOGOUT' | translate }}</span> }
            </button>
          }
          <app-theme-toggle [collapsed]="collapsed()" />
          <app-language-switcher [collapsed]="collapsed()" />
          @if (!collapsed()) {
            <span class="version">v0.1.0</span>
          }
        </div>
      </nav>
      <main class="content">
        @if (dl.showBar()) {
          <div class="download-bar"
               [class.download-error]="dl.downloadProgress()?.error"
               [class.download-done]="dl.downloadProgress()?.done && !dl.downloadProgress()?.error">
            <div class="download-info">
              <span class="download-icon">
                @if (dl.downloadProgress()?.done && !dl.downloadProgress()?.error) {
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M20 6L9 17l-5-5"/></svg>
                } @else if (dl.downloadProgress()?.error) {
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M18 6L6 18M6 6l12 12"/></svg>
                } @else {
                  <span class="dl-spinner"></span>
                }
              </span>
              <span class="download-text">
                @if (dl.downloadProgress()?.error) {
                  {{ 'DOWNLOAD.ERROR' | translate }}: {{ dl.downloadProgress()?.errorMessage }}
                } @else if (dl.downloadProgress()?.done) {
                  {{ 'DOWNLOAD.COMPLETE' | translate }}: {{ dl.downloadModelName() }}
                } @else {
                  {{ 'DOWNLOAD.PULLING' | translate }}: {{ dl.downloadModelName() }}
                  @if (dl.downloadProgress()?.status; as status) {
                    <span class="download-status">- {{ status }}</span>
                  }
                }
              </span>
            </div>
            <div class="download-actions">
              @if (dl.downloadPercentage() >= 0) {
                <span class="download-pct">{{ dl.downloadPercentage() }}%</span>
              }
              @if (!dl.isDownloading()) {
                <button class="download-dismiss" (click)="dl.dismiss()" [title]="'DOWNLOAD.DISMISS' | translate">&times;</button>
              }
            </div>
            @if (dl.downloadPercentage() >= 0) {
              <div class="download-track">
                <div class="download-fill" [style.width.%]="dl.downloadPercentage()"></div>
              </div>
            }
          </div>
        }
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
    .download-bar {
      background: var(--color-info-bg);
      border-radius: 8px;
      padding: 0.5rem 1rem;
      margin-bottom: 1rem;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.8rem;
      animation: slideDown 0.3s ease;
    }
    .download-bar.download-error { background: var(--color-error-bg); }
    .download-bar.download-done { background: var(--color-success-bg); }
    .download-info { display: flex; align-items: center; gap: 0.5rem; flex: 1; min-width: 0; }
    .download-icon { display: flex; align-items: center; flex-shrink: 0; }
    .download-bar.download-done .download-icon { color: var(--color-success-text); }
    .download-bar.download-error .download-icon { color: var(--color-error-text); }
    .download-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--color-text); }
    .download-status { color: var(--color-text-muted); font-size: 0.75rem; }
    .download-actions { display: flex; align-items: center; gap: 0.5rem; }
    .download-pct { font-weight: 600; font-size: 0.85rem; color: var(--color-primary); min-width: 36px; text-align: right; }
    .download-dismiss { background: none; border: none; cursor: pointer; font-size: 1rem; color: var(--color-text-muted); padding: 0.1rem 0.3rem; line-height: 1; border-radius: 4px; }
    .download-dismiss:hover { color: var(--color-text); background: rgba(0,0,0,0.05); }
    .download-track { width: 100%; height: 4px; background: var(--color-border); border-radius: 2px; overflow: hidden; }
    .download-fill { height: 100%; background: linear-gradient(90deg, var(--color-primary), var(--color-info)); border-radius: 2px; transition: width 0.3s ease; }
    .download-bar.download-error .download-fill { background: var(--color-error); }
    .download-bar.download-done .download-fill { background: var(--color-success); }
    .dl-spinner { display: inline-block; width: 12px; height: 12px; border: 2px solid var(--color-border); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.6s linear infinite; }
    .logout-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      width: 100%;
      padding: 0.5rem 0.75rem;
      background: rgba(255,255,255,0.05);
      border: none;
      border-radius: 6px;
      color: rgba(255,255,255,0.5);
      cursor: pointer;
      font-size: 0.8rem;
      transition: all 0.2s;
    }
    .logout-btn:hover {
      background: rgba(231,76,60,0.2);
      color: #e74c3c;
    }
    @keyframes slideDown { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 768px) {
      .sidebar {
        position: fixed;
        z-index: 100;
        height: 100vh;
      }
      .collapsed .sidebar {
        width: 0;
        padding: 0;
        overflow: hidden;
      }
      .content {
        padding: var(--space-md);
      }
    }
  `]
})
export class LayoutComponent {
  collapsed = signal(false);
  dl = inject(OllamaDownloadService);
  auth = inject(AuthService);
}
