import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { SearchRequest, SearchResult } from '../models/search.model';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private api = inject(ApiService);

  search(request: SearchRequest): Observable<SearchResult[]> {
    return this.api.post<SearchResult[]>('/search', request);
  }
}
