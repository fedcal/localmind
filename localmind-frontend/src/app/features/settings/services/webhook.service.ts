import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Webhook, WebhookRequest } from '../models/webhook.model';

@Injectable({ providedIn: 'root' })
export class WebhookService {
  private api = inject(ApiService);

  listAll(): Observable<Webhook[]> {
    return this.api.get<Webhook[]>('/webhooks');
  }

  getById(id: string): Observable<Webhook> {
    return this.api.get<Webhook>(`/webhooks/${id}`);
  }

  create(request: WebhookRequest): Observable<Webhook> {
    return this.api.post<Webhook>('/webhooks', request);
  }

  update(id: string, request: WebhookRequest): Observable<Webhook> {
    return this.api.put<Webhook>(`/webhooks/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/webhooks/${id}`);
  }

  test(id: string): Observable<void> {
    return this.api.post<void>(`/webhooks/${id}/test`, {});
  }
}
