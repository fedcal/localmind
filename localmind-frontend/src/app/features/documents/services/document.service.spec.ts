import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DocumentService } from './document.service';
import { Document } from '../models/document.model';
import { environment } from '../../../../environments/environment';

describe('DocumentService', () => {
  let service: DocumentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DocumentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(DocumentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('listDocuments() ritorna lista documenti / listDocuments() returns document list', () => {
    const mockDocs: Document[] = [
      { id: '1', filename: 'doc1.pdf', filePath: '/tmp/doc1.pdf', mimeType: 'application/pdf', fileSize: 1024, status: 'INDEXED', createdAt: '2025-01-01' },
      { id: '2', filename: 'doc2.txt', filePath: '/tmp/doc2.txt', mimeType: 'text/plain', fileSize: 512, status: 'PENDING', createdAt: '2025-01-02' }
    ];

    service.listDocuments().subscribe(data => {
      expect(data.length).toBe(2);
      expect(data).toEqual(mockDocs);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents`);
    expect(req.request.method).toBe('GET');
    req.flush(mockDocs);
  });

  it('deleteDocument() chiama DELETE / deleteDocument() calls DELETE', () => {
    service.deleteDocument('doc-123').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/documents/doc-123`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('uploadDocument() invia FormData con POST / uploadDocument() sends FormData with POST', () => {
    const mockFile = new File(['contenuto test'], 'test.pdf', { type: 'application/pdf' });
    const mockResponse: Document = {
      id: 'new-1', filename: 'test.pdf', filePath: '/tmp/test.pdf',
      mimeType: 'application/pdf', fileSize: 14, status: 'PENDING', createdAt: '2025-01-01'
    };

    service.uploadDocument(mockFile).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents/upload`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush(mockResponse);
  });

  it('gestisce lista documenti vuota / handles empty document list', () => {
    service.listDocuments().subscribe(data => {
      expect(data).toEqual([]);
      expect(data.length).toBe(0);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents`);
    req.flush([]);
  });

  it('gestisce errore di upload / handles upload error', () => {
    const mockFile = new File(['test'], 'fail.pdf', { type: 'application/pdf' });
    let errorReceived = false;

    service.uploadDocument(mockFile).subscribe({
      error: () => { errorReceived = true; }
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/documents/upload`);
    req.flush('Payload Too Large', { status: 413, statusText: 'Payload Too Large' });

    expect(errorReceived).toBe(true);
  });
});
