import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FolderService } from './folder.service';
import { MonitoredFolder, CreateFolderRequest } from '../models/folder.model';
import { environment } from '../../../../environments/environment';

describe('FolderService', () => {
  let service: FolderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FolderService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(FolderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('listFolders() ritorna lista cartelle / listFolders() returns folder list', () => {
    const mockFolders: MonitoredFolder[] = [
      { id: '1', path: '/home/docs', recursive: true, status: 'ACTIVE', documentCount: 10, createdAt: '2025-01-01' },
      { id: '2', path: '/home/notes', recursive: false, status: 'PAUSED', documentCount: 5, createdAt: '2025-01-02' }
    ];

    service.listFolders().subscribe(data => {
      expect(data.length).toBe(2);
      expect(data).toEqual(mockFolders);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/folders`);
    expect(req.request.method).toBe('GET');
    req.flush(mockFolders);
  });

  it('addFolder() chiama POST per creare cartella / addFolder() calls POST to create folder', () => {
    const request: CreateFolderRequest = { path: '/home/new-docs', recursive: true };
    const mockResponse: MonitoredFolder = {
      id: '3', path: '/home/new-docs', recursive: true, status: 'ACTIVE', documentCount: 0, createdAt: '2025-01-03'
    };

    service.addFolder(request).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/folders`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('removeFolder() chiama DELETE / removeFolder() calls DELETE', () => {
    service.removeFolder('folder-abc').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/folders/folder-abc`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('syncFolder() chiama POST per sincronizzare / syncFolder() calls POST to sync', () => {
    service.syncFolder('folder-1').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/folders/folder-1/sync`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(null);
  });
});
