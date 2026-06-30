import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { LlmProviderConfig, CreateProviderRequest, OllamaStatus } from '../models/settings.model';

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

  pullOllamaModel(baseUrl: string, modelName: string): Observable<void> {
    return this.api.post<void>('/settings/providers/ollama/models/pull', { baseUrl, modelName });
  }

  deleteOllamaModel(baseUrl: string, modelName: string): Observable<void> {
    return this.api.delete<void>(`/settings/providers/ollama/models/${encodeURIComponent(modelName)}?baseUrl=${encodeURIComponent(baseUrl)}`);
  }

  updateDefaultModel(providerId: string, model: string): Observable<LlmProviderConfig> {
    return this.api.put<LlmProviderConfig>(`/settings/providers/${providerId}/default-model`, { model });
  }

  getOllamaStatus(baseUrl: string): Observable<OllamaStatus> {
    return this.api.get<OllamaStatus>(`/settings/providers/ollama/status?baseUrl=${encodeURIComponent(baseUrl)}`);
  }

  startOllama(): Observable<void> {
    return this.api.post<void>('/settings/providers/ollama/start', {});
  }
}
