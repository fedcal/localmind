import { Injectable } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Observable } from 'rxjs';
import { MessagingChannel, CreateChannelRequest } from '../models/channel.model';

@Injectable({ providedIn: 'root' })
export class ChannelsService {
  private readonly path = '/channels';

  constructor(private api: ApiService) {}

  listChannels(): Observable<MessagingChannel[]> {
    return this.api.get<MessagingChannel[]>(this.path);
  }

  createChannel(request: CreateChannelRequest): Observable<MessagingChannel> {
    return this.api.post<MessagingChannel>(this.path, request);
  }

  getChannel(id: string): Observable<MessagingChannel> {
    return this.api.get<MessagingChannel>(`${this.path}/${id}`);
  }

  updateChannel(id: string, request: CreateChannelRequest): Observable<MessagingChannel> {
    return this.api.put<MessagingChannel>(`${this.path}/${id}`, request);
  }

  deleteChannel(id: string): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  testChannel(id: string): Observable<{ status: string; message: string }> {
    return this.api.post<{ status: string; message: string }>(`${this.path}/${id}/test`, {});
  }
}
