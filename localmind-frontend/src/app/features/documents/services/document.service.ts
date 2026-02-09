import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Document } from '../models/document.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private api = inject(ApiService);
  private http = inject(HttpClient);

  listDocuments(): Observable<Document[]> {
    return this.api.get<Document[]>('/documents');
  }

  deleteDocument(id: string): Observable<void> {
    return this.api.delete<void>(`/documents/${id}`);
  }

  uploadDocument(file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Document>(`${environment.apiUrl}/documents/upload`, formData);
  }
}
