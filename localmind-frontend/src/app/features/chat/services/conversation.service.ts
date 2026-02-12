import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ConversationSummary, ConversationDetail, RenameRequest, UpdateSystemPromptRequest, AddToolResultRequest, AddTagRequest, PageResponse } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ConversationService {
  constructor(private api: ApiService) {}

  listAll(): Observable<ConversationSummary[]> {
    return this.api.get<ConversationSummary[]>('/conversations');
  }

  listPaginated(page: number = 0, size: number = 20): Observable<PageResponse<ConversationSummary>> {
    return this.api.get<PageResponse<ConversationSummary>>(`/conversations/paginated?page=${page}&size=${size}`);
  }

  search(query: string, page: number = 0, size: number = 20): Observable<PageResponse<ConversationSummary>> {
    return this.api.get<PageResponse<ConversationSummary>>(`/conversations/search?query=${query}&page=${page}&size=${size}`);
  }

  getById(id: string): Observable<ConversationDetail> {
    return this.api.get<ConversationDetail>(`/conversations/${id}`);
  }

  rename(id: string, title: string): Observable<ConversationDetail> {
    return this.api.patch<ConversationDetail>(`/conversations/${id}`, { title } as RenameRequest);
  }

  updateSystemPrompt(id: string, systemPrompt: string | null): Observable<ConversationDetail> {
    return this.api.patch<ConversationDetail>(`/conversations/${id}/system-prompt`, { systemPrompt } as UpdateSystemPromptRequest);
  }

  addToolResult(id: string, request: AddToolResultRequest): Observable<ConversationDetail> {
    return this.api.post<ConversationDetail>(`/conversations/${id}/tool-result`, request);
  }

  updateContextWindow(id: string, maxContextMessages: number | null): Observable<ConversationDetail> {
    return this.api.patch<ConversationDetail>(`/conversations/${id}/context-window`, { maxContextMessages });
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/conversations/${id}`);
  }

  addTag(id: string, tag: string): Observable<ConversationDetail> {
    return this.api.post<ConversationDetail>(`/conversations/${id}/tags`, { tag } as AddTagRequest);
  }

  removeTag(id: string, tag: string): Observable<ConversationDetail> {
    return this.api.delete<ConversationDetail>(`/conversations/${id}/tags/${tag}`);
  }
}
