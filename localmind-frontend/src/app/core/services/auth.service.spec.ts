import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('isAuthenticated() ritorna false inizialmente senza token / isAuthenticated() returns false initially without token', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('login() chiama API corretta / login() calls correct API', () => {
    const mockResponse = { token: 'abc123', expiresIn: 3600 };

    service.login('password123').subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ password: 'password123' });
    req.flush(mockResponse);
  });

  it('setup() chiama API di setup / setup() calls setup API', () => {
    const mockResponse = { token: 'token123', expiresIn: 7200 };

    service.setup('pass', 'pass').subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/setup`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ password: 'pass', confirmPassword: 'pass' });
    req.flush(mockResponse);
  });

  it('logout() pulisce lo stato e rimuove il token / logout() clears state and removes token', () => {
    // Simuliamo un utente autenticato
    localStorage.setItem('localmind_token', 'mytoken');
    service.handleAuthResponse({ token: 'mytoken', expiresIn: 3600 });

    expect(service.isAuthenticated()).toBe(true);

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('localmind_token')).toBeNull();
  });

  it('handleAuthResponse() salva il token e aggiorna lo stato / handleAuthResponse() saves token and updates state', () => {
    service.handleAuthResponse({ token: 'new-token', expiresIn: 3600 });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.getToken()).toBe('new-token');
  });
});
