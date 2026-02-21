import { Injectable } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class McpScrumService {
  private readonly path = '/mcp/scrum';

  constructor(private api: ApiService) {}

  getBacklog(): Observable<any> {
    return this.api.get(`${this.path}/backlog`);
  }

  getSprint(sprintId: string): Observable<any> {
    return this.api.get(`${this.path}/sprints/${sprintId}`);
  }

  getSprintBoard(sprintId: string): Observable<any> {
    return this.api.get(`${this.path}/sprints/${sprintId}/board`);
  }

  createSprint(data: any): Observable<any> {
    return this.api.post(`${this.path}/sprints`, data);
  }

  createStory(data: any): Observable<any> {
    return this.api.post(`${this.path}/stories`, data);
  }

  createTask(data: any): Observable<any> {
    return this.api.post(`${this.path}/tasks`, data);
  }

  updateTaskStatus(taskId: string, status: string): Observable<any> {
    return this.api.put(`${this.path}/tasks/${taskId}/status`, { status });
  }
}
