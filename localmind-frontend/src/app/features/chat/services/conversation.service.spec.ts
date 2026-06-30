import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ConversationService } from './conversation.service';
import { ConversationSummary, ConversationDetail, PageResponse } from '../models/chat.model';
import { environment } from '../../../../environments/environment';

describe('ConversationService', () => {
  let service: ConversationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ConversationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ConversationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('listAll() ritorna lista conversazioni / listAll() returns conversation list', () => {
    const mockList: ConversationSummary[] = [
      { id: '1', title: 'Conv 1', messageCount: 5, createdAt: '2025-01-01', updatedAt: '2025-01-02' },
      { id: '2', title: 'Conv 2', messageCount: 3, createdAt: '2025-01-01', updatedAt: '2025-01-02' }
    ];

    service.listAll().subscribe(data => {
      expect(data.length).toBe(2);
      expect(data).toEqual(mockList);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/conversations`);
    expect(req.request.method).toBe('GET');
    req.flush(mockList);
  });

  it('getById() ritorna dettaglio conversazione / getById() returns conversation detail', () => {
    const mockDetail: ConversationDetail = {
      id: '1',
      title: 'Test',
      messages: [{ role: 'USER', content: 'Ciao' }],
      createdAt: '2025-01-01',
      updatedAt: '2025-01-02'
    };

    service.getById('1').subscribe(data => {
      expect(data).toEqual(mockDetail);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/conversations/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDetail);
  });

  it('delete() chiama DELETE / delete() calls DELETE', () => {
    service.delete('conv-abc').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/conversations/conv-abc`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('rename() chiama PATCH con il nuovo titolo / rename() calls PATCH with new title', () => {
    const mockDetail: ConversationDetail = {
      id: '1', title: 'Nuovo titolo', messages: [], createdAt: '2025-01-01', updatedAt: '2025-01-02'
    };

    service.rename('1', 'Nuovo titolo').subscribe(data => {
      expect(data.title).toBe('Nuovo titolo');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/conversations/1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ title: 'Nuovo titolo' });
    req.flush(mockDetail);
  });

  it('listPaginated() chiama GET con parametri di paginazione / listPaginated() calls GET with pagination params', () => {
    const mockPage: PageResponse<ConversationSummary> = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      page: 0,
      size: 20,
      hasMore: false
    };

    service.listPaginated(0, 20).subscribe(data => {
      expect(data).toEqual(mockPage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/conversations/paginated?page=0&size=20`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });
});
