import { Injectable } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Observable } from 'rxjs';
import { McpTool, McpToolExecutionRequest, McpToolExecutionResult } from '../models/mcp.model';

@Injectable({ providedIn: 'root' })
export class McpToolService {
  private readonly path = '/mcp/tools';

  constructor(private api: ApiService) {}

  listAllTools(): Observable<McpTool[]> {
    return this.api.get<McpTool[]>(this.path);
  }

  listLocalTools(): Observable<McpTool[]> {
    return this.api.get<McpTool[]>(`${this.path}/local`);
  }

  listServerTools(serverId: string): Observable<McpTool[]> {
    return this.api.get<McpTool[]>(`${this.path}/servers/${serverId}`);
  }

  executeTool(request: McpToolExecutionRequest): Observable<McpToolExecutionResult> {
    return this.api.post<McpToolExecutionResult>(`${this.path}/execute`, request);
  }
}
