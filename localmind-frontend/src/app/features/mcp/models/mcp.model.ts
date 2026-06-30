export interface McpServer {
  id: string;
  name: string;
  description?: string;
  type: McpServerType;
  status: McpServerStatus;
  config: McpServerConfig;
  createdAt: string;
  lastConnectedAt?: string;
}

export type McpServerType = 'STDIO' | 'SSE';

export type McpServerStatus = 'CONNECTED' | 'DISCONNECTED' | 'ERROR' | 'CONNECTING';

export interface McpServerConfig {
  command?: string;
  args?: string[];
  url?: string;
  timeoutSeconds?: number;
  autoReconnect?: boolean;
}

export interface CreateMcpServerRequest {
  name: string;
  description?: string;
  type: McpServerType;
  command?: string;
  args?: string[];
  url?: string;
  timeoutSeconds?: number;
  autoReconnect?: boolean;
}

export interface McpTool {
  name: string;
  description: string;
  inputSchema?: string;
  serverId?: string;
  local: boolean;
}

export interface McpToolExecutionRequest {
  toolName: string;
  serverId: string;
  arguments: Record<string, unknown>;
}

export interface McpToolExecutionResult {
  toolName: string;
  result: unknown;
  success: boolean;
  errorMessage?: string;
  executionTimeMs: number;
}
