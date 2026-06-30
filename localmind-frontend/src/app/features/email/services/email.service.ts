import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { EmailMessage, SendEmailRequest, DraftReplyResponse } from '../models/email.model';

@Injectable({ providedIn: 'root' })
export class EmailService {
  private api = inject(ApiService);

  listInbox(limit: number = 20): Observable<EmailMessage[]> {
    return this.api.get<EmailMessage[]>(`/email/inbox?limit=${limit}`);
  }

  sendEmail(request: SendEmailRequest): Observable<void> {
    return this.api.post<void>('/email/send', request);
  }

  markAsRead(id: string): Observable<void> {
    return this.api.patch<void>(`/email/${id}/read`, {});
  }

  generateDraftReply(id: string): Observable<DraftReplyResponse> {
    return this.api.post<DraftReplyResponse>(`/email/${id}/draft-reply`, {});
  }
}
