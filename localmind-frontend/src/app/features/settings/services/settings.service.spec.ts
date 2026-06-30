import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SettingsService } from './settings.service';
import { LlmProviderConfig, CreateProviderRequest } from '../models/settings.model';
import { environment } from '../../../../environments/environment';

describe('SettingsService', () => {
  let service: SettingsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SettingsService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(SettingsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('listProviders() ritorna lista provider / listProviders() returns provider list', () => {
    const mockProviders: LlmProviderConfig[] = [
      { id: '1', name: 'Ollama', type: 'OLLAMA', baseUrl: 'http://localhost:11434', enabled: true, models: ['llama3'] },
      { id: '2', name: 'OpenAI', type: 'OPENAI', baseUrl: 'https://api.openai.com', enabled: true, models: ['gpt-4'] }
    ];

    service.listProviders().subscribe(data => {
      expect(data.length).toBe(2);
      expect(data).toEqual(mockProviders);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/providers`);
    expect(req.request.method).toBe('GET');
    req.flush(mockProviders);
  });

  it('saveProvider() chiama POST per creare provider / saveProvider() calls POST to create provider', () => {
    const request: CreateProviderRequest = {
      name: 'Nuovo Provider',
      type: 'ANTHROPIC',
      baseUrl: 'https://api.anthropic.com'
    };
    const mockResponse: LlmProviderConfig = {
      id: '3', name: 'Nuovo Provider', type: 'ANTHROPIC',
      baseUrl: 'https://api.anthropic.com', enabled: true, models: []
    };

    service.saveProvider(request).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/providers`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('deleteProvider() chiama DELETE / deleteProvider() calls DELETE', () => {
    service.deleteProvider('prov-123').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/providers/prov-123`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('testProvider() chiama POST per testare connessione / testProvider() calls POST to test connection', () => {
    const mockResponse = { status: 'OK', message: 'Connessione riuscita' };

    service.testProvider('prov-1').subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/providers/prov-1/test`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('listOllamaModels() chiama GET con baseUrl / listOllamaModels() calls GET with baseUrl', () => {
    const mockModels = ['llama3', 'mistral', 'codellama'];
    const baseUrl = 'http://localhost:11434';

    service.listOllamaModels(baseUrl).subscribe(data => {
      expect(data).toEqual(mockModels);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/settings/providers/ollama/models?baseUrl=${encodeURIComponent(baseUrl)}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockModels);
  });
});
