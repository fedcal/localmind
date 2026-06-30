export interface PluginInfo {
  id: string;
  name: string;
  version: string;
  description: string;
  author: string;
  license: string;
  state: 'CREATED' | 'DISABLED' | 'RESOLVED' | 'STARTED' | 'STOPPED' | 'FAILED' | 'UNLOADED';
}
