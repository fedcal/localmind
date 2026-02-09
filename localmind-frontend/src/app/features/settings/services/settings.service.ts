import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { LlmProviderConfig, CreateProviderRequest } from '../models/settings.model';

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private api = inject(ApiService);

  listProviders(): Observable<LlmProviderConfig[]> {
    return this.api.get<LlmProviderConfig[]>('/settings/providers');
  }

  saveProvider(request: CreateProviderRequest): Observable<LlmProviderConfig> {
    return this.api.post<LlmProviderConfig>('/settings/providers', request);
  }

  deleteProvider(id: string): Observable<void> {
    return this.api.delete<void>(`/settings/providers/${id}`);
  }

  testProvider(id: string): Observable<{ status: string; message: string }> {
    return this.api.post<{ status: string; message: string }>(`/settings/providers/${id}/test`, {});
  }

  listOllamaModels(baseUrl: string): Observable<string[]> {
    return this.api.get<string[]>(`/settings/providers/ollama/models?baseUrl=${encodeURIComponent(baseUrl)}`);
  }
}
