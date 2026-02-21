import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SearchService } from './search.service';
import { SearchRequest, SearchResult } from '../models/search.model';
import { environment } from '../../../../environments/environment';

describe('SearchService', () => {
  let service: SearchService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SearchService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(SearchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('search() chiama POST su /search con la query / search() calls POST on /search with query', () => {
    const request: SearchRequest = { query: 'intelligenza artificiale' };
    const mockResults: SearchResult[] = [
      { content: 'Testo trovato', score: 0.95, documentId: 'd1', documentName: 'doc.pdf' }
    ];

    service.search(request).subscribe(data => {
      expect(data).toEqual(mockResults);
      expect(data.length).toBe(1);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResults);
  });

  it('search() supporta parametro topK opzionale / search() supports optional topK parameter', () => {
    const request: SearchRequest = { query: 'test query', topK: 5 };

    service.search(request).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/search`);
    expect(req.request.body.topK).toBe(5);
    req.flush([]);
  });

  it('search() gestisce risultati vuoti / search() handles empty results', () => {
    const request: SearchRequest = { query: 'nessun risultato' };

    service.search(request).subscribe(data => {
      expect(data).toEqual([]);
      expect(data.length).toBe(0);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/search`);
    req.flush([]);
  });

  it('search() gestisce errori / search() handles errors', () => {
    const request: SearchRequest = { query: 'test' };
    let errorReceived = false;

    service.search(request).subscribe({
      error: () => { errorReceived = true; }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/search`);
    req.flush('Error', { status: 500, statusText: 'Internal Server Error' });

    expect(errorReceived).toBe(true);
  });
});
