import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface BackupInfo {
  id: string;
  filename: string;
  createdAt: string;
  sizeBytes: number;
  backupType: string;
  components: string[];
}

@Injectable({ providedIn: 'root' })
export class BackupService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/backups`;

  list(): Observable<BackupInfo[]> {
    return this.http.get<BackupInfo[]>(this.baseUrl);
  }

  create(components: string[]): Observable<BackupInfo> {
    return this.http.post<BackupInfo>(this.baseUrl, { components });
  }

  download(filename: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${filename}/download`, { responseType: 'blob' });
  }

  restore(file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<void>(`${this.baseUrl}/restore`, formData);
  }

  delete(filename: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${filename}`);
  }
}
