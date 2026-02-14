import { Component, signal, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '../core/i18n/translate.pipe';
import { LanguageSwitcherComponent } from '../shared/components/language-switcher/language-switcher.component';
import { OllamaDownloadService } from '../core/services/ollama-download.service';
import { AuthService } from '../core/services/auth.service';

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
      background: #e3f2fd;
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
    .download-bar.download-error { background: #fef2f2; }
    .download-bar.download-done { background: #d4edda; }
    .download-info { display: flex; align-items: center; gap: 0.5rem; flex: 1; min-width: 0; }
    .download-icon { display: flex; align-items: center; flex-shrink: 0; }
    .download-bar.download-done .download-icon { color: #2e7d32; }
    .download-bar.download-error .download-icon { color: #c62828; }
    .download-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #333; }
    .download-status { color: #888; font-size: 0.75rem; }
    .download-actions { display: flex; align-items: center; gap: 0.5rem; }
    .download-pct { font-weight: 600; font-size: 0.85rem; color: #0f3460; min-width: 36px; text-align: right; }
    .download-dismiss { background: none; border: none; cursor: pointer; font-size: 1rem; color: #888; padding: 0.1rem 0.3rem; line-height: 1; border-radius: 4px; }
    .download-dismiss:hover { color: #333; background: rgba(0,0,0,0.05); }
    .download-track { width: 100%; height: 4px; background: rgba(0,0,0,0.08); border-radius: 2px; overflow: hidden; }
    .download-fill { height: 100%; background: linear-gradient(90deg, #0f3460, #3498db); border-radius: 2px; transition: width 0.3s ease; }
    .download-bar.download-error .download-fill { background: #e74c3c; }
    .download-bar.download-done .download-fill { background: #2ecc71; }
    .dl-spinner { display: inline-block; width: 12px; height: 12px; border: 2px solid #ddd; border-top-color: #0f3460; border-radius: 50%; animation: spin 0.6s linear infinite; }
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
