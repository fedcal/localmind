import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-brand">
          <div class="brand-logo">LM</div>
          <h1>LocalMind</h1>
        </div>

        @if (authService.needsSetup()) {
          <h2>{{ 'AUTH.SETUP_TITLE' | translate }}</h2>
          <p class="setup-desc">{{ 'AUTH.SETUP_DESC' | translate }}</p>
          <form (ngSubmit)="onSetup()">
            <div class="form-group">
              <label>{{ 'AUTH.PASSWORD' | translate }}</label>
              <input type="password" [(ngModel)]="password" name="password"
                     [placeholder]="'AUTH.PASSWORD_PLACEHOLDER' | translate" required minlength="4">
            </div>
            <div class="form-group">
              <label>{{ 'AUTH.CONFIRM_PASSWORD' | translate }}</label>
              <input type="password" [(ngModel)]="confirmPassword" name="confirmPassword"
                     [placeholder]="'AUTH.CONFIRM_PLACEHOLDER' | translate" required>
            </div>
            @if (error()) {
              <div class="error-msg">{{ error() }}</div>
            }
            <button type="submit" [disabled]="loading()" class="btn-primary">
              @if (loading()) { <span class="spinner"></span> }
              {{ 'AUTH.SETUP_BTN' | translate }}
            </button>
          </form>
        } @else {
          <h2>{{ 'AUTH.LOGIN_TITLE' | translate }}</h2>
          <form (ngSubmit)="onLogin()">
            <div class="form-group">
              <label>{{ 'AUTH.PASSWORD' | translate }}</label>
              <input type="password" [(ngModel)]="password" name="password"
                     [placeholder]="'AUTH.PASSWORD_PLACEHOLDER' | translate" required
                     (keydown.enter)="onLogin()">
            </div>
            @if (error()) {
              <div class="error-msg">{{ error() }}</div>
            }
            <button type="submit" [disabled]="loading()" class="btn-primary">
              @if (loading()) { <span class="spinner"></span> }
              {{ 'AUTH.LOGIN_BTN' | translate }}
            </button>
          </form>
        }
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #0a1628 0%, #1a2744 50%, #0f3460 100%);
    }
    .login-card {
      background: white;
      border-radius: 16px;
      padding: 2.5rem;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    }
    .login-brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 2rem;
      justify-content: center;
    }
    .brand-logo {
      width: 44px;
      height: 44px;
      background: linear-gradient(135deg, #0f3460, #3498db);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 800;
      font-size: 1rem;
    }
    .login-brand h1 {
      margin: 0;
      font-size: 1.5rem;
      color: #1a2744;
    }
    h2 {
      text-align: center;
      color: #333;
      margin-bottom: 0.5rem;
      font-size: 1.1rem;
    }
    .setup-desc {
      text-align: center;
      color: #666;
      font-size: 0.85rem;
      margin-bottom: 1.5rem;
    }
    .form-group {
      margin-bottom: 1rem;
    }
    .form-group label {
      display: block;
      margin-bottom: 0.4rem;
      font-size: 0.85rem;
      font-weight: 500;
      color: #555;
    }
    .form-group input {
      width: 100%;
      padding: 0.7rem 0.9rem;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 0.9rem;
      transition: border-color 0.2s;
      box-sizing: border-box;
    }
    .form-group input:focus {
      outline: none;
      border-color: #3498db;
      box-shadow: 0 0 0 3px rgba(52,152,219,0.1);
    }
    .error-msg {
      background: #fef2f2;
      color: #c62828;
      padding: 0.6rem 0.8rem;
      border-radius: 6px;
      font-size: 0.8rem;
      margin-bottom: 1rem;
    }
    .btn-primary {
      width: 100%;
      padding: 0.75rem;
      background: linear-gradient(135deg, #0f3460, #3498db);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 0.95rem;
      font-weight: 600;
      cursor: pointer;
      transition: opacity 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }
    .btn-primary:hover:not(:disabled) { opacity: 0.9; }
    .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
    .spinner {
      display: inline-block;
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255,255,255,0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class LoginPageComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  password = '';
  confirmPassword = '';
  loading = signal(false);
  error = signal('');

  ngOnInit() {
    this.authService.checkStatus();
  }

  onLogin() {
    if (!this.password) return;
    this.loading.set(true);
    this.error.set('');
    this.authService.login(this.password).subscribe({
      next: (res) => {
        this.authService.handleAuthResponse(res);
        this.router.navigate(['/']);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Authentication failed');
        this.loading.set(false);
      }
    });
  }

  onSetup() {
    if (!this.password || !this.confirmPassword) return;
    if (this.password !== this.confirmPassword) {
      this.error.set('Passwords do not match');
      return;
    }
    if (this.password.length < 4) {
      this.error.set('Password must be at least 4 characters');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.authService.setup(this.password, this.confirmPassword).subscribe({
      next: (res) => {
        this.authService.handleAuthResponse(res);
        this.router.navigate(['/']);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Setup failed');
        this.loading.set(false);
      }
    });
  }
}
