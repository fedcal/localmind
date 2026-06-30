import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { MonitoredFolder, CreateFolderRequest } from '../models/folder.model';

@Injectable({ providedIn: 'root' })
export class FolderService {
  private api = inject(ApiService);

  listFolders(): Observable<MonitoredFolder[]> {
    return this.api.get<MonitoredFolder[]>('/folders');
  }

  addFolder(request: CreateFolderRequest): Observable<MonitoredFolder> {
    return this.api.post<MonitoredFolder>('/folders', request);
  }

  removeFolder(id: string): Observable<void> {
    return this.api.delete<void>(`/folders/${id}`);
  }

  syncFolder(id: string): Observable<void> {
    return this.api.post<void>(`/folders/${id}/sync`, {});
  }
}
