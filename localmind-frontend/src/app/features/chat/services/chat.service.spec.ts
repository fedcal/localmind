import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ChatService } from './chat.service';
import { ChatRequest, ChatResponse } from '../models/chat.model';
import { environment } from '../../../../environments/environment';

describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChatService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('chat() chiama POST su /chat con il messaggio / chat() calls POST on /chat with message', () => {
    const request: ChatRequest = { message: 'Ciao, come stai?' };
    const mockResponse: ChatResponse = {
      content: 'Sto bene, grazie!',
      model: 'llama3',
      provider: 'OLLAMA',
      latencyMs: 150
    };

    service.chat(request).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/chat`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('chat() invia parametri opzionali / chat() sends optional parameters', () => {
    const request: ChatRequest = {
      message: 'Test',
      provider: 'OPENAI',
      model: 'gpt-4',
      conversationId: 'conv-123',
      temperature: 0.7,
      maxTokens: 500,
      enableRag: true
    };

    service.chat(request).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat`);
    expect(req.request.body.provider).toBe('OPENAI');
    expect(req.request.body.model).toBe('gpt-4');
    expect(req.request.body.conversationId).toBe('conv-123');
    expect(req.request.body.temperature).toBe(0.7);
    expect(req.request.body.enableRag).toBe(true);
    req.flush({ content: 'ok', model: 'gpt-4', provider: 'OPENAI', latencyMs: 100 });
  });

  it('chat() gestisce errori / chat() handles errors', () => {
    const request: ChatRequest = { message: 'Test' };
    let errorReceived = false;

    service.chat(request).subscribe({
      error: () => { errorReceived = true; }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/chat`);
    req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });

    expect(errorReceived).toBe(true);
  });
});
