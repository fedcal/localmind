import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('get() chiama GET con URL corretto / get() calls GET with correct URL', () => {
    const mockData = { id: '1', name: 'test' };

    service.get<typeof mockData>('/documents').subscribe(data => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('post() chiama POST con body / post() calls POST with body', () => {
    const body = { message: 'ciao' };
    const mockResponse = { content: 'risposta' };

    service.post<typeof mockResponse>('/chat', body).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/chat`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(mockResponse);
  });

  it('put() chiama PUT con body / put() calls PUT with body', () => {
    const body = { name: 'aggiornato' };
    const mockResponse = { id: '1', name: 'aggiornato' };

    service.put<typeof mockResponse>('/settings/providers/1', body).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/providers/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(body);
    req.flush(mockResponse);
  });

  it('patch() chiama PATCH con body / patch() calls PATCH with body', () => {
    const body = { title: 'nuovo titolo' };
    const mockResponse = { id: '1', title: 'nuovo titolo' };

    service.patch<typeof mockResponse>('/conversations/1', body).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/conversations/1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(mockResponse);
  });

  it('delete() chiama DELETE / delete() calls DELETE', () => {
    service.delete<void>('/documents/abc').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/documents/abc`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('gestisce errori HTTP senza crash / handles HTTP errors without crashing', () => {
    let errorReceived = false;

    service.get('/non-esiste').subscribe({
      error: () => { errorReceived = true; }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/non-esiste`);
    req.flush('Not Found', { status: 404, statusText: 'Not Found' });

    expect(errorReceived).toBe(true);
  });

  it('usa il baseUrl corretto da environment / uses correct baseUrl from environment', () => {
    service.get('/test').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/test`);
    expect(req.request.url).toBe(`${environment.apiUrl}/test`);
    req.flush({});
  });
});
