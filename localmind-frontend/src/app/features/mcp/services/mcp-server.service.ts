import { Injectable } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Observable } from 'rxjs';
import { McpServer, CreateMcpServerRequest } from '../models/mcp.model';

@Injectable({ providedIn: 'root' })
export class McpServerService {
  private readonly path = '/mcp/servers';

  constructor(private api: ApiService) {}

  listServers(): Observable<McpServer[]> {
    return this.api.get<McpServer[]>(this.path);
  }

  getServer(id: string): Observable<McpServer> {
    return this.api.get<McpServer>(`${this.path}/${id}`);
  }

  registerServer(request: CreateMcpServerRequest): Observable<McpServer> {
    return this.api.post<McpServer>(this.path, request);
  }

  removeServer(id: string): Observable<void> {
    return this.api.delete<void>(`${this.path}/${id}`);
  }

  testConnection(id: string): Observable<McpServer> {
    return this.api.post<McpServer>(`${this.path}/${id}/test`, {});
  }

  reconnect(id: string): Observable<void> {
    return this.api.post<void>(`${this.path}/${id}/reconnect`, {});
  }
}
