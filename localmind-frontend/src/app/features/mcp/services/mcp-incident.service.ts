import { Injectable } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class McpIncidentService {
  private readonly path = '/mcp/incidents';

  constructor(private api: ApiService) {}

  listIncidents(status?: string, severity?: string, limit = 50): Observable<any> {
    let params = `?limit=${limit}`;
    if (status) params += `&status=${status}`;
    if (severity) params += `&severity=${severity}`;
    return this.api.get(`${this.path}${params}`);
  }

  openIncident(data: any): Observable<any> {
    return this.api.post(this.path, data);
  }

  updateIncident(id: string, data: any): Observable<any> {
    return this.api.put(`${this.path}/${id}`, data);
  }

  addTimeline(id: string, data: any): Observable<any> {
    return this.api.post(`${this.path}/${id}/timeline`, data);
  }

  resolveIncident(id: string, data: any): Observable<any> {
    return this.api.post(`${this.path}/${id}/resolve`, data);
  }

  getPostmortem(id: string): Observable<any> {
    return this.api.get(`${this.path}/${id}/postmortem`);
  }
}
