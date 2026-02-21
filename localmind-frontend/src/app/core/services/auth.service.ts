import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';

export interface AuthStatus {
  configured: boolean;
  authenticated: boolean;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'localmind_token';

  private _isAuthenticated = signal(false);
  private _isConfigured = signal<boolean | null>(null);

  isAuthenticated = this._isAuthenticated.asReadonly();
  isConfigured = this._isConfigured.asReadonly();
  needsSetup = computed(() => this._isConfigured() === false);

  constructor(private http: HttpClient, private router: Router) {
    this._isAuthenticated.set(!!this.getToken());
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  checkStatus() {
    return this.http.get<AuthStatus>(`${this.apiUrl}/status`).subscribe({
      next: (status) => {
        this._isConfigured.set(status.configured);
        if (!status.configured) {
          this._isAuthenticated.set(true); // No auth needed
        }
      },
      error: () => {
        this._isConfigured.set(false);
        this._isAuthenticated.set(true);
      }
    });
  }

  login(password: string) {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { password });
  }

  setup(password: string, confirmPassword: string) {
    return this.http.post<AuthResponse>(`${this.apiUrl}/setup`, { password, confirmPassword });
  }

  changePassword(currentPassword: string, newPassword: string) {
    return this.http.post<void>(`${this.apiUrl}/change-password`, { currentPassword, newPassword });
  }

  handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    this._isAuthenticated.set(true);
    this._isConfigured.set(true);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this._isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }
}
