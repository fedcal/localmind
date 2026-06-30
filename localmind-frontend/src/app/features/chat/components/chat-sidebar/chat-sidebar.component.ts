import { Component, inject, OnInit, signal, computed, DestroyRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ChatStore } from '../../state/chat.store';
import { ConversationService } from '../../services/conversation.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { ConversationSummary } from '../../models/chat.model';

@Component({
  selector: 'app-chat-sidebar',
  imports: [FormsModule, TranslatePipe],
  template: `
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>{{ 'CHAT.CONVERSATION_HISTORY' | translate }}</h3>
        <button class="new-chat-btn" (click)="newChat()" [title]="'CHAT.NEW_CHAT' | translate">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          {{ 'CHAT.NEW_CHAT' | translate }}
        </button>
        <div class="search-bar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input class="search-input"
                 [ngModel]="store.searchQuery()"
                 (ngModelChange)="onSearchChange($event)"
                 [placeholder]="'CHAT.SEARCH_CONVERSATIONS' | translate" />
          @if (store.searchQuery()) {
            <button class="search-clear" (click)="clearSearch()">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          }
        </div>
      </div>

      @if (allTags().length > 0) {
        <div class="tag-filter">
          <div class="tag-filter-chips">
            <button class="tag-chip"
                    [class.active]="!store.filterTag()"
                    (click)="store.setFilterTag(null)">
              {{ 'CHAT.ALL_CONVERSATIONS' | translate }}
            </button>
            @for (tag of allTags(); track tag) {
              <button class="tag-chip"
                      [class.active]="store.filterTag() === tag"
                      [style.border-color]="getTagColor(tag)"
                      (click)="store.setFilterTag(store.filterTag() === tag ? null : tag)">
                <span class="tag-dot" [style.background]="getTagColor(tag)"></span>
                {{ tag }}
              </button>
            }
          </div>
        </div>
      }

      <div class="conversations-list">
        @if (store.conversationsLoading()) {
          <div class="sidebar-empty">{{ 'COMMON.LOADING' | translate }}</div>
        } @else if (store.filteredConversations().length === 0) {
          <div class="sidebar-empty">{{ 'CHAT.NO_CONVERSATIONS' | translate }}</div>
        } @else {
          @for (conv of store.filteredConversations(); track conv.id) {
            <div class="conversation-item"
                 [class.active]="conv.id === store.currentConversationId()"
                 (click)="selectConversation(conv)">
              @if (editingId() === conv.id) {
                <input class="rename-input"
                       [(ngModel)]="editTitle"
                       (keydown.enter)="confirmRename(conv.id)"
                       (keydown.escape)="cancelRename()"
                       (blur)="confirmRename(conv.id)"
                       (click)="$event.stopPropagation()" />
              } @else {
                <div class="conv-info">
                  <div class="conv-title">{{ conv.title }}</div>
                  <div class="conv-meta">
                    {{ conv.messageCount }} {{ 'CHAT.MESSAGES_LABEL' | translate }}
                  </div>
                  @if (conv.tags && conv.tags.length > 0) {
                    <div class="conv-tags">
                      @for (tag of conv.tags; track tag) {
                        <span class="conv-tag-badge"
                              [style.background]="getTagColor(tag)"
                              (click)="removeTagFromConversation(conv.id, tag, $event)"
                              [title]="'CHAT.REMOVE_TAG' | translate">
                          {{ tag }} &times;
                        </span>
                      }
                    </div>
                  }
                </div>
                <div class="conv-actions">
                  <button class="action-btn" (click)="startAddTag(conv, $event)" [title]="'CHAT.ADD_TAG' | translate">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                      <line x1="7" y1="7" x2="7.01" y2="7"/>
                    </svg>
                  </button>
                  <button class="action-btn" (click)="startRename(conv, $event)" [title]="'CHAT.RENAME' | translate">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                  <button class="action-btn delete" (click)="deleteConversation(conv.id, $event)" [title]="'COMMON.DELETE' | translate">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                    </svg>
                  </button>
                </div>
              }
            </div>
            @if (addingTagId() === conv.id) {
              <div class="tag-input-row" (click)="$event.stopPropagation()">
                <input class="tag-input"
                       [(ngModel)]="newTagValue"
                       (keydown.enter)="confirmAddTag(conv.id)"
                       (keydown.escape)="cancelAddTag()"
                       (blur)="confirmAddTag(conv.id)"
                       [placeholder]="'CHAT.ADD_TAG' | translate" />
              </div>
            }
          }
          @if (store.hasMore()) {
            <button class="load-more-btn" (click)="loadMore()" [disabled]="store.conversationsLoading()">
              @if (store.conversationsLoading()) {
                {{ 'COMMON.LOADING' | translate }}
              } @else {
                {{ 'CHAT.LOAD_MORE' | translate }}
              }
            </button>
          }
        }
      </div>
    </div>
  `,
  styles: [`
    .chat-sidebar {
      width: 260px;
      min-width: 260px;
      border-right: 1px solid var(--color-border, #e0e0e0);
      display: flex;
      flex-direction: column;
      height: 100%;
      background: var(--color-hover-bg);
    }
    .sidebar-header {
      padding: 0.75rem;
      border-bottom: 1px solid var(--color-border-light);
    }
    .sidebar-header h3 {
      font-size: 0.8rem;
      color: var(--color-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin: 0 0 0.5rem 0;
    }
    .new-chat-btn {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      width: 100%;
      padding: 0.5rem 0.75rem;
      background: var(--color-primary);
      color: white;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.8rem;
      transition: background 0.15s;
    }
    .new-chat-btn:hover { background: var(--color-primary-light); }

    .conversations-list {
      flex: 1;
      overflow-y: auto;
      padding: 0.5rem;
    }
    .sidebar-empty {
      text-align: center;
      color: var(--color-text-muted);
      font-size: 0.8rem;
      padding: 2rem 0.5rem;
    }
    .conversation-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0.5rem 0.6rem;
      border-radius: 6px;
      cursor: pointer;
      transition: background 0.1s;
      margin-bottom: 2px;
    }
    .conversation-item:hover { background: var(--color-hover-bg); }
    .conversation-item.active { background: var(--color-info-bg); border-left: 3px solid var(--color-primary); }

    .conv-info { flex: 1; min-width: 0; }
    .conv-title {
      font-size: 0.82rem;
      color: var(--color-text);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .conv-meta {
      font-size: 0.68rem;
      color: var(--color-text-muted);
      margin-top: 2px;
    }

    .conv-actions {
      display: flex;
      gap: 2px;
      opacity: 0;
      transition: opacity 0.1s;
    }
    .conversation-item:hover .conv-actions { opacity: 1; }
    .conversation-item.active .conv-actions { opacity: 1; }

    .action-btn {
      padding: 4px;
      background: none;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      color: var(--color-text-muted);
      display: flex;
      align-items: center;
    }
    .action-btn:hover { background: var(--color-border); color: var(--color-text); }
    .action-btn.delete:hover { background: var(--color-error-bg); color: var(--color-error-text); }

    .rename-input {
      width: 100%;
      padding: 0.3rem 0.5rem;
      border: 1px solid var(--color-primary);
      border-radius: 4px;
      font-size: 0.82rem;
      outline: none;
    }

    .tag-filter {
      padding: 0.4rem 0.5rem;
      border-bottom: 1px solid var(--color-border-light);
    }
    .tag-filter-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }
    .tag-chip {
      display: flex;
      align-items: center;
      gap: 3px;
      padding: 2px 8px;
      border: 1px solid var(--color-border);
      border-radius: 12px;
      background: var(--color-card-bg);
      font-size: 0.68rem;
      cursor: pointer;
      transition: background 0.1s;
    }
    .tag-chip:hover { background: var(--color-skeleton-base); }
    .tag-chip.active { background: var(--color-info-bg); border-color: var(--color-primary); font-weight: 600; }
    .tag-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      display: inline-block;
    }

    .conv-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 3px;
      margin-top: 3px;
    }
    .conv-tag-badge {
      display: inline-block;
      padding: 1px 6px;
      border-radius: 8px;
      font-size: 0.6rem;
      color: white;
      cursor: pointer;
      line-height: 1.4;
      transition: opacity 0.1s;
    }
    .conv-tag-badge:hover { opacity: 0.7; }

    .tag-input-row {
      padding: 2px 0.6rem 6px;
    }
    .tag-input {
      width: 100%;
      padding: 0.2rem 0.4rem;
      border: 1px solid var(--color-primary);
      border-radius: 4px;
      font-size: 0.75rem;
      outline: none;
    }

    .search-bar {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 8px;
      margin-top: 8px;
      background: var(--color-card-bg);
      border: 1px solid var(--color-border);
      border-radius: 6px;
      transition: border-color 0.15s;
    }
    .search-bar:focus-within { border-color: var(--color-primary); }
    .search-bar svg { color: var(--color-text-muted); flex-shrink: 0; }
    .search-input {
      flex: 1;
      border: none;
      outline: none;
      font-size: 0.78rem;
      background: transparent;
      color: var(--color-text);
      min-width: 0;
    }
    .search-input::placeholder { color: var(--color-text-muted); }
    .search-clear {
      background: none;
      border: none;
      cursor: pointer;
      color: var(--color-text-muted);
      padding: 2px;
      display: flex;
      border-radius: 50%;
    }
    .search-clear:hover { background: var(--color-border-light); color: var(--color-text-secondary); }

    .load-more-btn {
      display: block;
      width: 100%;
      padding: 8px;
      margin-top: 4px;
      background: transparent;
      border: 1px dashed var(--color-border);
      border-radius: 6px;
      color: var(--color-text-secondary);
      font-size: 0.78rem;
      cursor: pointer;
      transition: background 0.1s, border-color 0.1s;
    }
    .load-more-btn:hover { background: var(--color-skeleton-base); border-color: var(--color-primary); color: var(--color-primary); }
    .load-more-btn:disabled { cursor: not-allowed; opacity: 0.5; }
  `]
})
export class ChatSidebarComponent implements OnInit {
  store = inject(ChatStore);
  private conversationService = inject(ConversationService);
  private destroyRef = inject(DestroyRef);

  editingId = signal<string | null>(null);
  editTitle = '';
  addingTagId = signal<string | null>(null);
  newTagValue = '';
  private searchSubject = new Subject<string>();

  readonly allTags = computed(() => {
    const tags = new Set<string>();
    for (const conv of this.store.conversations()) {
      if (conv.tags) {
        for (const tag of conv.tags) {
          tags.add(tag);
        }
      }
    }
    return [...tags].sort();
  });

  private readonly TAG_COLORS = [
    '#2196F3', '#4CAF50', '#FF9800', '#9C27B0', '#F44336',
    '#00BCD4', '#795548', '#607D8B', '#E91E63', '#3F51B5'
  ];

  getTagColor(tag: string): string {
    let hash = 0;
    for (let i = 0; i < tag.length; i++) {
      hash = tag.charCodeAt(i) + ((hash << 5) - hash);
    }
    return this.TAG_COLORS[Math.abs(hash) % this.TAG_COLORS.length];
  }

  ngOnInit() {
    this.loadConversations();

    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(query => {
      this.store.setSearchQuery(query);
      this.store.setConversations([]);
      this.store.setPaginationInfo(0, 0, 0, false);
      this.loadConversations();
    });
  }

  loadConversations() {
    this.store.setConversationsLoading(true);
    const query = this.store.searchQuery();
    const page = this.store.currentPage();

    const obs$ = query
      ? this.conversationService.search(query, page, 20)
      : this.conversationService.listPaginated(page, 20);

    obs$.subscribe({
      next: (response) => {
        if (page === 0) {
          this.store.setConversations(response.content);
        } else {
          this.store.appendConversations(response.content);
        }
        this.store.setPaginationInfo(response.page, response.totalPages, response.totalElements, response.hasMore);
        this.store.setConversationsLoading(false);
      },
      error: () => {
        this.store.setConversationsLoading(false);
      }
    });
  }

  selectConversation(conv: ConversationSummary) {
    if (this.editingId()) return;
    if (conv.id === this.store.currentConversationId()) return;
    this.conversationService.getById(conv.id).subscribe({
      next: (detail) => {
        this.store.loadConversationMessages(
          detail.messages.map(m => ({ role: m.role as 'USER' | 'ASSISTANT' | 'SYSTEM', content: m.content })),
          detail.id,
          detail.title,
          detail.systemPrompt,
          detail.maxContextMessages
        );
      }
    });
  }

  newChat() {
    this.store.clearMessages();
  }

  onSearchChange(query: string) {
    this.searchSubject.next(query);
  }

  clearSearch() {
    this.searchSubject.next('');
    this.store.setSearchQuery('');
    this.store.setConversations([]);
    this.store.setPaginationInfo(0, 0, 0, false);
    this.loadConversations();
  }

  loadMore() {
    this.store.setPaginationInfo(this.store.currentPage() + 1, this.store.totalPages(), this.store.totalElements(), this.store.hasMore());
    this.loadConversations();
  }

  startRename(conv: ConversationSummary, event: Event) {
    event.stopPropagation();
    this.editingId.set(conv.id);
    this.editTitle = conv.title;
  }

  confirmRename(id: string) {
    if (this.editTitle.trim()) {
      this.conversationService.rename(id, this.editTitle.trim()).subscribe({
        next: () => {
          this.store.updateConversationTitle(id, this.editTitle.trim());
          this.editingId.set(null);
        }
      });
    } else {
      this.cancelRename();
    }
  }

  cancelRename() {
    this.editingId.set(null);
  }

  deleteConversation(id: string, event: Event) {
    event.stopPropagation();
    this.conversationService.delete(id).subscribe({
      next: () => {
        this.store.removeConversation(id);
      }
    });
  }

  startAddTag(conv: ConversationSummary, event: Event) {
    event.stopPropagation();
    this.addingTagId.set(conv.id);
    this.newTagValue = '';
  }

  confirmAddTag(id: string) {
    const tag = this.newTagValue.trim();
    if (tag) {
      this.conversationService.addTag(id, tag).subscribe({
        next: () => {
          this.store.setConversations(
            this.store.conversations().map(c =>
              c.id === id ? { ...c, tags: [...(c.tags || []), tag] } : c
            )
          );
          this.addingTagId.set(null);
        }
      });
    } else {
      this.cancelAddTag();
    }
  }

  cancelAddTag() {
    this.addingTagId.set(null);
  }

  removeTagFromConversation(id: string, tag: string, event: Event) {
    event.stopPropagation();
    this.conversationService.removeTag(id, tag).subscribe({
      next: () => {
        this.store.setConversations(
          this.store.conversations().map(c =>
            c.id === id ? { ...c, tags: (c.tags || []).filter(t => t !== tag) } : c
          )
        );
      }
    });
  }
}
