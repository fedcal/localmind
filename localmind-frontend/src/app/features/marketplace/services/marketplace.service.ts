import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { MarketplaceAgent, MarketplaceReview } from '../models/marketplace.model';

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private readonly baseUrl = `${environment.apiUrl}/marketplace`;

  constructor(private http: HttpClient) {}

  listAgents(category?: string, search?: string): Observable<MarketplaceAgent[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<MarketplaceAgent[]>(`${this.baseUrl}/agents`, { params });
  }

  getAgent(id: string): Observable<MarketplaceAgent> {
    return this.http.get<MarketplaceAgent>(`${this.baseUrl}/agents/${id}`);
  }

  publishAgent(request: {
    name: string;
    description: string;
    author: string;
    version: string;
    category: string;
    downloadUrl: string;
  }): Observable<MarketplaceAgent> {
    return this.http.post<MarketplaceAgent>(`${this.baseUrl}/agents`, request);
  }

  installAgent(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/agents/${id}/install`, {});
  }

  addReview(agentId: string, rating: number, comment: string): Observable<MarketplaceReview> {
    return this.http.post<MarketplaceReview>(`${this.baseUrl}/agents/${agentId}/reviews`, {
      rating,
      comment
    });
  }

  getReviews(agentId: string): Observable<MarketplaceReview[]> {
    return this.http.get<MarketplaceReview[]>(`${this.baseUrl}/agents/${agentId}/reviews`);
  }

  deleteAgent(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/agents/${id}`);
  }
}
