import { Injectable } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class McpTimeService {
  private readonly path = '/mcp/time';

  constructor(private api: ApiService) {}

  startTimer(data: any): Observable<any> {
    return this.api.post(`${this.path}/start`, data);
  }

  stopTimer(): Observable<any> {
    return this.api.post(`${this.path}/stop`, {});
  }

  logTime(data: any): Observable<any> {
    return this.api.post(`${this.path}/log`, data);
  }

  getTimesheet(startDate: string, endDate: string, userId?: string): Observable<any> {
    let params = `?startDate=${startDate}&endDate=${endDate}`;
    if (userId) params += `&userId=${userId}`;
    return this.api.get(`${this.path}/timesheet${params}`);
  }
}
