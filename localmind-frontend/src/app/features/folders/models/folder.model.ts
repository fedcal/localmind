export interface MonitoredFolder {
  id: string;
  path: string;
  pattern?: string;
  recursive: boolean;
  status: 'ACTIVE' | 'SYNCING' | 'ERROR' | 'PAUSED';
  documentCount: number;
  lastSyncAt?: string;
  createdAt: string;
}

export interface CreateFolderRequest {
  path: string;
  pattern?: string;
  recursive: boolean;
}
