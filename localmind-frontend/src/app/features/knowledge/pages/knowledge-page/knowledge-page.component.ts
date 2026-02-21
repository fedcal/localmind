import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { KnowledgeService } from '../../services/knowledge.service';
import { KnowledgeEntity, KnowledgeGraphStats, KnowledgeSubgraph } from '../../models/knowledge.model';

@Component({
  selector: 'app-knowledge-page',
  standalone: true,
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="knowledge-page">
      <header class="page-header">
        <h1>{{ 'KNOWLEDGE.TITLE' | translate }}</h1>
      </header>

      <!-- Stats Section -->
      <section class="stats-section">
        <h2>{{ 'KNOWLEDGE.STATS' | translate }}</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-value">{{ stats()?.totalEntities ?? 0 }}</span>
            <span class="stat-label">{{ 'KNOWLEDGE.TOTAL_ENTITIES' | translate }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">{{ stats()?.totalRelations ?? 0 }}</span>
            <span class="stat-label">{{ 'KNOWLEDGE.TOTAL_RELATIONS' | translate }}</span>
          </div>
        </div>
        @if (stats()?.entitiesByType && objectKeys(stats()!.entitiesByType).length > 0) {
          <div class="type-distribution">
            <h3>{{ 'KNOWLEDGE.BY_TYPE' | translate }}</h3>
            <div class="type-chips">
              @for (type of objectKeys(stats()!.entitiesByType); track type) {
                <span class="type-chip">{{ type }}: {{ stats()!.entitiesByType[type] }}</span>
              }
            </div>
          </div>
        }
      </section>

      <!-- Search Section -->
      <section class="search-section">
        <div class="search-bar">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            [placeholder]="'KNOWLEDGE.SEARCH_PLACEHOLDER' | translate"
            (keyup.enter)="onSearch()"
          />
          <button class="btn-primary" (click)="onSearch()" [disabled]="loading()">
            {{ 'KNOWLEDGE.SEARCH' | translate }}
          </button>
        </div>
      </section>

      <!-- Loading -->
      @if (loading()) {
        <div class="loading">{{ 'KNOWLEDGE.LOADING' | translate }}</div>
      }

      <!-- Results Section -->
      @if (entities().length > 0) {
        <section class="results-section">
          <div class="entity-cards">
            @for (entity of entities(); track entity.id) {
              <div class="entity-card" [class.selected]="selectedEntityId() === entity.id"
                   (click)="onSelectEntity(entity)">
                <div class="entity-header">
                  <span class="entity-name">{{ entity.name }}</span>
                  <span class="entity-type-badge">{{ entity.type }}</span>
                </div>
                @if (entity.properties && objectKeys(entity.properties).length > 0) {
                  <div class="entity-props">
                    @for (key of objectKeys(entity.properties); track key) {
                      <span class="prop-tag">{{ key }}: {{ entity.properties[key] }}</span>
                    }
                  </div>
                }
                <div class="entity-actions">
                  <button class="btn-small btn-danger" (click)="onDeleteEntity(entity.id, $event)">
                    {{ 'KNOWLEDGE.DELETE' | translate }}
                  </button>
                </div>
              </div>
            }
          </div>
        </section>
      } @else if (!loading() && searched()) {
        <div class="empty-state">{{ 'KNOWLEDGE.NO_RESULTS' | translate }}</div>
      }

      <!-- Subgraph Section -->
      @if (subgraph() && subgraph()!.entities.length > 0) {
        <section class="subgraph-section">
          <h2>{{ 'KNOWLEDGE.SUBGRAPH' | translate }}</h2>
          <table class="subgraph-table">
            <thead>
              <tr>
                <th>{{ 'KNOWLEDGE.ENTITY_NAME' | translate }}</th>
                <th>{{ 'KNOWLEDGE.ENTITY_TYPE' | translate }}</th>
                <th>{{ 'KNOWLEDGE.RELATIONS' | translate }}</th>
              </tr>
            </thead>
            <tbody>
              @for (entity of subgraph()!.entities; track entity.id) {
                <tr>
                  <td>{{ entity.name }}</td>
                  <td><span class="entity-type-badge small">{{ entity.type }}</span></td>
                  <td>
                    @for (rel of getRelationsForEntity(entity.id); track rel.id) {
                      <span class="relation-tag">
                        {{ rel.type }} -> {{ getEntityName(rel.fromEntityId === entity.id ? rel.toEntityId : rel.fromEntityId) }}
                      </span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </section>
      }

      <!-- Index Text Section -->
      <section class="index-section">
        <h2>{{ 'KNOWLEDGE.INDEX_TEXT' | translate }}</h2>
        <textarea
          [(ngModel)]="indexText"
          rows="4"
          [placeholder]="'KNOWLEDGE.SEARCH_PLACEHOLDER' | translate"
        ></textarea>
        <button class="btn-primary" (click)="onIndexText()" [disabled]="indexing() || !indexText.trim()">
          {{ 'KNOWLEDGE.INDEX_TEXT' | translate }}
        </button>
      </section>
    </div>
  `,
  styles: [`
    .knowledge-page {
      max-width: 1000px;
      margin: 0 auto;
    }
    .page-header h1 {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 1.5rem;
      color: var(--color-text);
    }
    .stats-section {
      background: var(--color-surface);
      border-radius: 12px;
      padding: 1.25rem;
      margin-bottom: 1.5rem;
      border: 1px solid var(--color-border);
    }
    .stats-section h2 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 1rem;
      color: var(--color-text);
    }
    .stats-grid {
      display: flex;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .stat-card {
      flex: 1;
      background: var(--color-bg);
      border-radius: 8px;
      padding: 1rem;
      text-align: center;
      border: 1px solid var(--color-border);
    }
    .stat-value {
      display: block;
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--color-primary);
    }
    .stat-label {
      font-size: 0.8rem;
      color: var(--color-text-muted);
    }
    .type-distribution h3 {
      font-size: 0.85rem;
      font-weight: 600;
      margin-bottom: 0.5rem;
      color: var(--color-text-muted);
    }
    .type-chips {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }
    .type-chip {
      background: var(--color-primary-light, rgba(52,152,219,0.1));
      color: var(--color-primary);
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 500;
    }
    .search-section {
      margin-bottom: 1.5rem;
    }
    .search-bar {
      display: flex;
      gap: 0.75rem;
    }
    .search-bar input {
      flex: 1;
      padding: 0.6rem 1rem;
      border: 1px solid var(--color-border);
      border-radius: 8px;
      font-size: 0.9rem;
      background: var(--color-surface);
      color: var(--color-text);
    }
    .search-bar input:focus {
      outline: none;
      border-color: var(--color-primary);
    }
    .btn-primary {
      padding: 0.6rem 1.25rem;
      background: var(--color-primary);
      color: white;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 500;
      font-size: 0.9rem;
      transition: background 0.2s;
    }
    .btn-primary:hover:not(:disabled) {
      opacity: 0.9;
    }
    .btn-primary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .loading {
      text-align: center;
      padding: 2rem;
      color: var(--color-text-muted);
      font-size: 0.9rem;
    }
    .empty-state {
      text-align: center;
      padding: 2rem;
      color: var(--color-text-muted);
      font-size: 0.9rem;
      background: var(--color-surface);
      border-radius: 12px;
      border: 1px solid var(--color-border);
    }
    .entity-cards {
      display: grid;
      gap: 0.75rem;
    }
    .entity-card {
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: 10px;
      padding: 1rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .entity-card:hover {
      border-color: var(--color-primary);
    }
    .entity-card.selected {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(52,152,219,0.2);
    }
    .entity-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 0.5rem;
    }
    .entity-name {
      font-weight: 600;
      font-size: 1rem;
      color: var(--color-text);
    }
    .entity-type-badge {
      background: var(--color-primary-light, rgba(52,152,219,0.1));
      color: var(--color-primary);
      padding: 0.2rem 0.6rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .entity-type-badge.small {
      font-size: 0.7rem;
      padding: 0.15rem 0.5rem;
    }
    .entity-props {
      display: flex;
      gap: 0.4rem;
      flex-wrap: wrap;
      margin-bottom: 0.5rem;
    }
    .prop-tag {
      background: var(--color-bg);
      padding: 0.15rem 0.5rem;
      border-radius: 6px;
      font-size: 0.75rem;
      color: var(--color-text-muted);
      border: 1px solid var(--color-border);
    }
    .entity-actions {
      display: flex;
      justify-content: flex-end;
    }
    .btn-small {
      padding: 0.25rem 0.6rem;
      font-size: 0.75rem;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      transition: background 0.2s;
    }
    .btn-danger {
      background: rgba(231,76,60,0.1);
      color: #e74c3c;
    }
    .btn-danger:hover {
      background: rgba(231,76,60,0.2);
    }
    .subgraph-section {
      margin-top: 1.5rem;
      background: var(--color-surface);
      border-radius: 12px;
      padding: 1.25rem;
      border: 1px solid var(--color-border);
    }
    .subgraph-section h2 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 1rem;
      color: var(--color-text);
    }
    .subgraph-table {
      width: 100%;
      border-collapse: collapse;
    }
    .subgraph-table th,
    .subgraph-table td {
      padding: 0.6rem 0.75rem;
      text-align: left;
      border-bottom: 1px solid var(--color-border);
      font-size: 0.85rem;
      color: var(--color-text);
    }
    .subgraph-table th {
      font-weight: 600;
      color: var(--color-text-muted);
      font-size: 0.8rem;
    }
    .relation-tag {
      display: inline-block;
      background: rgba(155,89,182,0.1);
      color: #9b59b6;
      padding: 0.15rem 0.5rem;
      border-radius: 6px;
      font-size: 0.75rem;
      margin: 0.1rem;
    }
    .index-section {
      margin-top: 1.5rem;
      background: var(--color-surface);
      border-radius: 12px;
      padding: 1.25rem;
      border: 1px solid var(--color-border);
    }
    .index-section h2 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 1rem;
      color: var(--color-text);
    }
    .index-section textarea {
      width: 100%;
      padding: 0.6rem 1rem;
      border: 1px solid var(--color-border);
      border-radius: 8px;
      font-size: 0.9rem;
      background: var(--color-bg);
      color: var(--color-text);
      resize: vertical;
      margin-bottom: 0.75rem;
      font-family: inherit;
      box-sizing: border-box;
    }
    .index-section textarea:focus {
      outline: none;
      border-color: var(--color-primary);
    }
    .results-section {
      margin-bottom: 1.5rem;
    }
  `]
})
export class KnowledgePageComponent {
  private knowledgeService = inject(KnowledgeService);

  searchQuery = '';
  indexText = '';

  entities = signal<KnowledgeEntity[]>([]);
  stats = signal<KnowledgeGraphStats | null>(null);
  subgraph = signal<KnowledgeSubgraph | null>(null);
  selectedEntityId = signal<string | null>(null);
  loading = signal(false);
  indexing = signal(false);
  searched = signal(false);

  objectKeys = Object.keys;

  constructor() {
    this.loadStats();
  }

  loadStats() {
    this.knowledgeService.getStats().subscribe({
      next: (stats) => this.stats.set(stats),
      error: () => {}
    });
  }

  onSearch() {
    if (!this.searchQuery.trim()) return;
    this.loading.set(true);
    this.searched.set(true);
    this.subgraph.set(null);
    this.selectedEntityId.set(null);

    this.knowledgeService.searchEntities(this.searchQuery).subscribe({
      next: (entities) => {
        this.entities.set(entities);
        this.loading.set(false);
      },
      error: () => {
        this.entities.set([]);
        this.loading.set(false);
      }
    });
  }

  onSelectEntity(entity: KnowledgeEntity) {
    this.selectedEntityId.set(entity.id);
    this.knowledgeService.getSubgraph(entity.id, 2).subscribe({
      next: (subgraph) => this.subgraph.set(subgraph),
      error: () => this.subgraph.set(null)
    });
  }

  onDeleteEntity(id: string, event: Event) {
    event.stopPropagation();
    this.knowledgeService.deleteEntity(id).subscribe({
      next: () => {
        this.entities.update(entities => entities.filter(e => e.id !== id));
        if (this.selectedEntityId() === id) {
          this.selectedEntityId.set(null);
          this.subgraph.set(null);
        }
        this.loadStats();
      },
      error: () => {}
    });
  }

  onIndexText() {
    if (!this.indexText.trim()) return;
    this.indexing.set(true);
    this.knowledgeService.indexText(this.indexText).subscribe({
      next: () => {
        this.indexText = '';
        this.indexing.set(false);
        this.loadStats();
      },
      error: () => this.indexing.set(false)
    });
  }

  getRelationsForEntity(entityId: string) {
    return this.subgraph()?.relations.filter(
      r => r.fromEntityId === entityId || r.toEntityId === entityId
    ) ?? [];
  }

  getEntityName(entityId: string): string {
    return this.subgraph()?.entities.find(e => e.id === entityId)?.name ?? entityId;
  }
}
