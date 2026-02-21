import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PluginInfo } from '../models/plugin.model';

@Injectable({ providedIn: 'root' })
export class PluginService {
  private readonly baseUrl = `${environment.apiUrl}/plugins`;

  constructor(private http: HttpClient) {}

  getPlugins(): Observable<PluginInfo[]> {
    return this.http.get<PluginInfo[]>(this.baseUrl);
  }

  getPlugin(id: string): Observable<PluginInfo> {
    return this.http.get<PluginInfo>(`${this.baseUrl}/${id}`);
  }

  enablePlugin(id: string): Observable<PluginInfo> {
    return this.http.post<PluginInfo>(`${this.baseUrl}/${id}/enable`, {});
  }

  disablePlugin(id: string): Observable<PluginInfo> {
    return this.http.post<PluginInfo>(`${this.baseUrl}/${id}/disable`, {});
  }

  deletePlugin(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  uploadPlugin(file: File): Observable<PluginInfo> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<PluginInfo>(`${this.baseUrl}/upload`, formData);
  }
}
