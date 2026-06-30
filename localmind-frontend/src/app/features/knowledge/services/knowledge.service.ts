import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { KnowledgeEntity, KnowledgeGraphStats, KnowledgeSubgraph } from '../models/knowledge.model';

@Injectable({ providedIn: 'root' })
export class KnowledgeService {
  private api = inject(ApiService);

  searchEntities(query: string): Observable<KnowledgeEntity[]> {
    return this.api.get<KnowledgeEntity[]>(`/knowledge/entities?query=${encodeURIComponent(query)}`);
  }

  getSubgraph(entityId: string, depth: number = 2): Observable<KnowledgeSubgraph> {
    return this.api.get<KnowledgeSubgraph>(`/knowledge/entities/${entityId}/subgraph?depth=${depth}`);
  }

  indexText(text: string, sourceDocumentId?: string): Observable<void> {
    return this.api.post<void>('/knowledge/index', { text, sourceDocumentId });
  }

  getStats(): Observable<KnowledgeGraphStats> {
    return this.api.get<KnowledgeGraphStats>('/knowledge/stats');
  }

  deleteEntity(id: string): Observable<void> {
    return this.api.delete<void>(`/knowledge/entities/${id}`);
  }
}
