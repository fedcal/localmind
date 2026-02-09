import { Component, signal } from '@angular/core';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-guide-page',
  imports: [TranslatePipe],
  template: `
    <div class="guide-page">
      <div class="page-header">
        <div>
          <h1>{{ 'GUIDE.TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'GUIDE.SUBTITLE' | translate }}</p>
        </div>
      </div>

      <!-- TOC -->
      <div class="toc card">
        <h3>{{ 'GUIDE.TOC_TITLE' | translate }}</h3>
        <div class="toc-grid">
          <a class="toc-item" (click)="scrollTo('start')">
            <span class="toc-icon">&#x1F680;</span>
            <span>{{ 'GUIDE.TOC_START' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('dashboard')">
            <span class="toc-icon">&#x1F4CA;</span>
            <span>{{ 'GUIDE.TOC_DASHBOARD' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('chat')">
            <span class="toc-icon">&#x1F4AC;</span>
            <span>{{ 'GUIDE.TOC_CHAT' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('documents')">
            <span class="toc-icon">&#x1F4C4;</span>
            <span>{{ 'GUIDE.TOC_DOCUMENTS' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('search')">
            <span class="toc-icon">&#x1F50D;</span>
            <span>{{ 'GUIDE.TOC_SEARCH' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('folders')">
            <span class="toc-icon">&#x1F4C1;</span>
            <span>{{ 'GUIDE.TOC_FOLDERS' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('providers')">
            <span class="toc-icon">&#x2699;&#xFE0F;</span>
            <span>{{ 'GUIDE.TOC_PROVIDERS' | translate }}</span>
          </a>
          <a class="toc-item" (click)="scrollTo('mcp')">
            <span class="toc-icon">&#x1F527;</span>
            <span>{{ 'GUIDE.TOC_MCP' | translate }}</span>
          </a>
        </div>
      </div>

      <!-- SEZIONE 1: PRIMI PASSI -->
      <section id="start" class="guide-section">
        <div class="section-header" (click)="toggle('start')">
          <div class="section-title">
            <span class="section-icon">&#x1F680;</span>
            <h2>{{ 'GUIDE.START.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'start' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'start') {
          <div class="section-body">
            <p class="section-intro">{{ 'GUIDE.START.INTRO' | translate }}</p>

            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.START.STEP1_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.START.STEP1_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.START.STEP2_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.START.STEP2_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.START.STEP3_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.START.STEP3_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <div>
                  <strong>{{ 'GUIDE.START.STEP4_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.START.STEP4_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">5</span>
                <div>
                  <strong>{{ 'GUIDE.START.STEP5_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.START.STEP5_DESC' | translate"></p>
                </div>
              </div>
            </div>

            <div class="info-box" [innerHTML]="'GUIDE.START.NAV_INFO' | translate"></div>
          </div>
        }
      </section>

      <!-- SEZIONE 2: DASHBOARD -->
      <section id="dashboard" class="guide-section">
        <div class="section-header" (click)="toggle('dashboard')">
          <div class="section-title">
            <span class="section-icon">&#x1F4CA;</span>
            <h2>{{ 'GUIDE.DASHBOARD.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'dashboard' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'dashboard') {
          <div class="section-body">
            <p class="section-intro">{{ 'GUIDE.DASHBOARD.INTRO' | translate }}</p>

            <h3>{{ 'GUIDE.DASHBOARD.SYSTEM_STATUS' | translate }}</h3>
            <p>{{ 'GUIDE.DASHBOARD.SYSTEM_STATUS_DESC' | translate }}</p>
            <table class="info-table">
              <thead>
                <tr><th>{{ 'GUIDE.DASHBOARD.TH_INDICATOR' | translate }}</th><th>{{ 'GUIDE.DASHBOARD.TH_MEANING' | translate }}</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td><span class="inline-badge bg-green">UP</span></td>
                  <td>{{ 'GUIDE.DASHBOARD.UP_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td><span class="inline-badge bg-red">DOWN</span></td>
                  <td>{{ 'GUIDE.DASHBOARD.DOWN_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.DASHBOARD.COUNTERS' | translate }}</h3>
            <p>{{ 'GUIDE.DASHBOARD.COUNTERS_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.DASHBOARD.COUNTER_DOCS' | translate"></li>
              <li [innerHTML]="'GUIDE.DASHBOARD.COUNTER_MCP' | translate"></li>
              <li [innerHTML]="'GUIDE.DASHBOARD.COUNTER_LLM' | translate"></li>
            </ul>

            <h3>{{ 'GUIDE.DASHBOARD.QUICK_ACTIONS' | translate }}</h3>
            <p>{{ 'GUIDE.DASHBOARD.QUICK_ACTIONS_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.DASHBOARD.QA_CHAT' | translate"></li>
              <li [innerHTML]="'GUIDE.DASHBOARD.QA_UPLOAD' | translate"></li>
              <li [innerHTML]="'GUIDE.DASHBOARD.QA_SEARCH' | translate"></li>
              <li [innerHTML]="'GUIDE.DASHBOARD.QA_MCP' | translate"></li>
            </ul>
          </div>
        }
      </section>

      <!-- SEZIONE 3: CHAT -->
      <section id="chat" class="guide-section">
        <div class="section-header" (click)="toggle('chat')">
          <div class="section-title">
            <span class="section-icon">&#x1F4AC;</span>
            <h2>{{ 'GUIDE.CHAT.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'chat' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'chat') {
          <div class="section-body">
            <p class="section-intro" [innerHTML]="'GUIDE.CHAT.INTRO' | translate"></p>

            <h3>{{ 'GUIDE.CHAT.SELECT_PROVIDER' | translate }}</h3>
            <p [innerHTML]="'GUIDE.CHAT.SELECT_PROVIDER_DESC' | translate"></p>

            <h3>{{ 'GUIDE.CHAT.SEND_MESSAGE' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr><th>{{ 'GUIDE.CHAT.TH_ACTION' | translate }}</th><th>{{ 'GUIDE.CHAT.TH_HOW' | translate }}</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.CHAT.ACTION_SEND' | translate }}</td>
                  <td [innerHTML]="'GUIDE.CHAT.ACTION_SEND_HOW' | translate"></td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.CHAT.ACTION_NEWLINE' | translate }}</td>
                  <td [innerHTML]="'GUIDE.CHAT.ACTION_NEWLINE_HOW' | translate"></td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.CHAT.ACTION_NEW_CHAT' | translate }}</td>
                  <td [innerHTML]="'GUIDE.CHAT.ACTION_NEW_CHAT_HOW' | translate"></td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.CHAT.QUICK_SUGGESTIONS' | translate }}</h3>
            <p [innerHTML]="'GUIDE.CHAT.QUICK_SUGGESTIONS_DESC' | translate"></p>

            <h3>{{ 'GUIDE.CHAT.HISTORY' | translate }}</h3>
            <p>{{ 'GUIDE.CHAT.HISTORY_DESC' | translate }}</p>

            <h3>{{ 'GUIDE.CHAT.RAG_MODE' | translate }}</h3>
            <p [innerHTML]="'GUIDE.CHAT.RAG_MODE_DESC' | translate"></p>

            <div class="info-box" [innerHTML]="'GUIDE.CHAT.TIP' | translate"></div>
          </div>
        }
      </section>

      <!-- SEZIONE 4: DOCUMENTI -->
      <section id="documents" class="guide-section">
        <div class="section-header" (click)="toggle('documents')">
          <div class="section-title">
            <span class="section-icon">&#x1F4C4;</span>
            <h2>{{ 'GUIDE.DOCUMENTS.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'documents' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'documents') {
          <div class="section-body">
            <p class="section-intro">{{ 'GUIDE.DOCUMENTS.INTRO' | translate }}</p>

            <h3>{{ 'GUIDE.DOCUMENTS.FORMATS' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_FORMAT' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_EXTENSIONS' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_NOTES' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_PDF' | translate }}</td>
                  <td [innerHTML]="'GUIDE.DOCUMENTS.FORMAT_PDF_EXT' | translate"></td>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_PDF_NOTE' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_WORD' | translate }}</td>
                  <td [innerHTML]="'GUIDE.DOCUMENTS.FORMAT_WORD_EXT' | translate"></td>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_WORD_NOTE' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_TEXT' | translate }}</td>
                  <td [innerHTML]="'GUIDE.DOCUMENTS.FORMAT_TEXT_EXT' | translate"></td>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_TEXT_NOTE' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_MD' | translate }}</td>
                  <td [innerHTML]="'GUIDE.DOCUMENTS.FORMAT_MD_EXT' | translate"></td>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_MD_NOTE' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_CSV' | translate }}</td>
                  <td [innerHTML]="'GUIDE.DOCUMENTS.FORMAT_CSV_EXT' | translate"></td>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_CSV_NOTE' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_JSON' | translate }}</td>
                  <td [innerHTML]="'GUIDE.DOCUMENTS.FORMAT_JSON_EXT' | translate"></td>
                  <td>{{ 'GUIDE.DOCUMENTS.FORMAT_JSON_NOTE' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.DOCUMENTS.UPLOAD_TITLE' | translate }}</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.DOCUMENTS.UPLOAD_STEP1' | translate }}</strong>
                  <p>{{ 'GUIDE.DOCUMENTS.UPLOAD_STEP1_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.DOCUMENTS.UPLOAD_STEP2' | translate }}</strong>
                  <p>{{ 'GUIDE.DOCUMENTS.UPLOAD_STEP2_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.DOCUMENTS.UPLOAD_STEP3' | translate }}</strong>
                  <p>{{ 'GUIDE.DOCUMENTS.UPLOAD_STEP3_DESC' | translate }}</p>
                </div>
              </div>
            </div>

            <h3>{{ 'GUIDE.DOCUMENTS.STATUSES' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_STATUS' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_BADGE' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_DESCRIPTION' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_PENDING' | translate }}</td>
                  <td><span class="inline-badge bg-yellow">PENDING</span></td>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_PENDING_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_PROCESSING' | translate }}</td>
                  <td><span class="inline-badge bg-blue">PROCESSING</span></td>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_PROCESSING_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_INDEXED' | translate }}</td>
                  <td><span class="inline-badge bg-green">INDEXED</span></td>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_INDEXED_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_FAILED' | translate }}</td>
                  <td><span class="inline-badge bg-red">FAILED</span></td>
                  <td>{{ 'GUIDE.DOCUMENTS.STATUS_FAILED_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.DOCUMENTS.FILTERS' | translate }}</h3>
            <p [innerHTML]="'GUIDE.DOCUMENTS.FILTERS_DESC' | translate"></p>

            <h3>{{ 'GUIDE.DOCUMENTS.CARD_INFO' | translate }}</h3>
            <p>{{ 'GUIDE.DOCUMENTS.CARD_INFO_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.DOCUMENTS.CARD_FILENAME' | translate"></li>
              <li [innerHTML]="'GUIDE.DOCUMENTS.CARD_SIZE' | translate"></li>
              <li [innerHTML]="'GUIDE.DOCUMENTS.CARD_DATE' | translate"></li>
              <li [innerHTML]="'GUIDE.DOCUMENTS.CARD_BADGE' | translate"></li>
              <li [innerHTML]="'GUIDE.DOCUMENTS.CARD_CHUNKS' | translate"></li>
            </ul>

            <h3>{{ 'GUIDE.DOCUMENTS.DELETE_TITLE' | translate }}</h3>
            <p [innerHTML]="'GUIDE.DOCUMENTS.DELETE_DESC' | translate"></p>

            <div class="info-box" [innerHTML]="'GUIDE.DOCUMENTS.DELETE_WARNING' | translate"></div>
          </div>
        }
      </section>

      <!-- SEZIONE 5: RICERCA SEMANTICA -->
      <section id="search" class="guide-section">
        <div class="section-header" (click)="toggle('search')">
          <div class="section-title">
            <span class="section-icon">&#x1F50D;</span>
            <h2>{{ 'GUIDE.SEARCH.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'search' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'search') {
          <div class="section-body">
            <p class="section-intro" [innerHTML]="'GUIDE.SEARCH.INTRO' | translate"></p>

            <h3>{{ 'GUIDE.SEARCH.HOW_IT_WORKS' | translate }}</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.SEARCH.STEP1_TITLE' | translate }}</strong>
                  <p>{{ 'GUIDE.SEARCH.STEP1_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.SEARCH.STEP2_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.SEARCH.STEP2_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.SEARCH.STEP3_TITLE' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.SEARCH.STEP3_DESC' | translate"></p>
                </div>
              </div>
            </div>

            <h3>{{ 'GUIDE.SEARCH.RESULTS' | translate }}</h3>
            <p>{{ 'GUIDE.SEARCH.RESULTS_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.SEARCH.RESULT_SOURCE' | translate"></li>
              <li [innerHTML]="'GUIDE.SEARCH.RESULT_SCORE' | translate"></li>
              <li [innerHTML]="'GUIDE.SEARCH.RESULT_EXCERPT' | translate"></li>
            </ul>

            <h3>{{ 'GUIDE.SEARCH.TIPS' | translate }}</h3>
            <ul class="detail-list">
              <li>{{ 'GUIDE.SEARCH.TIP1' | translate }}</li>
              <li>{{ 'GUIDE.SEARCH.TIP2' | translate }}</li>
              <li>{{ 'GUIDE.SEARCH.TIP3' | translate }}</li>
              <li>{{ 'GUIDE.SEARCH.TIP4' | translate }}</li>
            </ul>

            <div class="info-box" [innerHTML]="'GUIDE.SEARCH.PREREQUISITE' | translate"></div>
          </div>
        }
      </section>

      <!-- SEZIONE 6: CARTELLE MONITORATE -->
      <section id="folders" class="guide-section">
        <div class="section-header" (click)="toggle('folders')">
          <div class="section-title">
            <span class="section-icon">&#x1F4C1;</span>
            <h2>{{ 'GUIDE.FOLDERS.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'folders' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'folders') {
          <div class="section-body">
            <p class="section-intro">{{ 'GUIDE.FOLDERS.INTRO' | translate }}</p>

            <h3>{{ 'GUIDE.FOLDERS.ADD_TITLE' | translate }}</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.FOLDERS.ADD_STEP1' | translate }}</strong>
                  <p>{{ 'GUIDE.FOLDERS.ADD_STEP1_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.FOLDERS.ADD_STEP2' | translate }}</strong>
                  <p>{{ 'GUIDE.FOLDERS.ADD_STEP2_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.FOLDERS.ADD_STEP3' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.FOLDERS.ADD_STEP3_DESC' | translate"></p>
                </div>
              </div>
            </div>

            <h3>{{ 'GUIDE.FOLDERS.FORM_FIELDS' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.FOLDERS.TH_FIELD' | translate }}</th>
                  <th>{{ 'GUIDE.FOLDERS.TH_REQUIRED' | translate }}</th>
                  <th>{{ 'GUIDE.FOLDERS.TH_DESCRIPTION' | translate }}</th>
                  <th>{{ 'GUIDE.FOLDERS.TH_EXAMPLE' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_PATH' | translate }}</td>
                  <td>{{ 'COMMON.YES' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_PATH_DESC' | translate }}</td>
                  <td [innerHTML]="'GUIDE.FOLDERS.FIELD_PATH_EX' | translate"></td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_PATTERN' | translate }}</td>
                  <td>{{ 'COMMON.NO' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_PATTERN_DESC' | translate }}</td>
                  <td [innerHTML]="'GUIDE.FOLDERS.FIELD_PATTERN_EX' | translate"></td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_RECURSIVE' | translate }}</td>
                  <td>{{ 'COMMON.NO' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_RECURSIVE_DESC' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.FIELD_RECURSIVE_EX' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.FOLDERS.STATUSES' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_STATUS' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_BADGE' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_DESCRIPTION' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_ACTIVE' | translate }}</td>
                  <td><span class="inline-badge bg-green">ACTIVE</span></td>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_ACTIVE_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_SYNCING' | translate }}</td>
                  <td><span class="inline-badge bg-blue">SYNCING</span></td>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_SYNCING_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_ERROR' | translate }}</td>
                  <td><span class="inline-badge bg-red">ERROR</span></td>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_ERROR_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_PAUSED' | translate }}</td>
                  <td><span class="inline-badge bg-neutral">PAUSED</span></td>
                  <td>{{ 'GUIDE.FOLDERS.STATUS_PAUSED_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.FOLDERS.CARD_INFO' | translate }}</h3>
            <p>{{ 'GUIDE.FOLDERS.CARD_INFO_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.FOLDERS.CARD_PATH' | translate"></li>
              <li [innerHTML]="'GUIDE.FOLDERS.CARD_BADGE' | translate"></li>
              <li [innerHTML]="'GUIDE.FOLDERS.CARD_DOCS' | translate"></li>
              <li [innerHTML]="'GUIDE.FOLDERS.CARD_PATTERN' | translate"></li>
              <li [innerHTML]="'GUIDE.FOLDERS.CARD_MODE' | translate"></li>
              <li [innerHTML]="'GUIDE.FOLDERS.CARD_SYNC' | translate"></li>
            </ul>

            <h3>{{ 'GUIDE.FOLDERS.ACTIONS' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.FOLDERS.TH_ACTION' | translate }}</th>
                  <th>{{ 'GUIDE.FOLDERS.TH_ICON' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_DESCRIPTION' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.ACTION_SYNC' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.ACTION_SYNC_ICON' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.ACTION_SYNC_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.FOLDERS.ACTION_DELETE' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.ACTION_DELETE_ICON' | translate }}</td>
                  <td>{{ 'GUIDE.FOLDERS.ACTION_DELETE_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <div class="info-box" [innerHTML]="'GUIDE.FOLDERS.AUTO_SCAN' | translate"></div>
          </div>
        }
      </section>

      <!-- SEZIONE 7: IMPOSTAZIONI PROVIDER LLM -->
      <section id="providers" class="guide-section">
        <div class="section-header" (click)="toggle('providers')">
          <div class="section-title">
            <span class="section-icon">&#x2699;&#xFE0F;</span>
            <h2>{{ 'GUIDE.PROVIDERS.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'providers' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'providers') {
          <div class="section-body">
            <p class="section-intro">{{ 'GUIDE.PROVIDERS.INTRO' | translate }}</p>

            <h3>{{ 'GUIDE.PROVIDERS.SUPPORTED' | translate }}</h3>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.PROVIDERS.TH_PROVIDER' | translate }}</th>
                  <th>{{ 'GUIDE.PROVIDERS.TH_TYPE' | translate }}</th>
                  <th>{{ 'GUIDE.PROVIDERS.TH_APIKEY' | translate }}</th>
                  <th>{{ 'GUIDE.PROVIDERS.TH_URL' | translate }}</th>
                  <th>{{ 'GUIDE.PROVIDERS.TH_MODELS' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong>Ollama</strong></td>
                  <td>{{ 'GUIDE.PROVIDERS.OLLAMA_TYPE' | translate }}</td>
                  <td>{{ 'COMMON.NO' | translate }}</td>
                  <td><code>http://localhost:11434</code></td>
                  <td>llama3.2, mistral, codellama, phi3</td>
                </tr>
                <tr>
                  <td><strong>OpenAI</strong></td>
                  <td>{{ 'GUIDE.PROVIDERS.CLOUD_TYPE' | translate }}</td>
                  <td>{{ 'COMMON.YES' | translate }} (<code>sk-...</code>)</td>
                  <td><code>https://api.openai.com</code></td>
                  <td>gpt-4o, gpt-4o-mini, gpt-3.5-turbo</td>
                </tr>
                <tr>
                  <td><strong>Anthropic</strong></td>
                  <td>{{ 'GUIDE.PROVIDERS.CLOUD_TYPE' | translate }}</td>
                  <td>{{ 'COMMON.YES' | translate }} (<code>sk-ant-...</code>)</td>
                  <td><code>https://api.anthropic.com</code></td>
                  <td>claude-sonnet-4-20250514, claude-haiku</td>
                </tr>
                <tr>
                  <td><strong>Google Gemini</strong></td>
                  <td>{{ 'GUIDE.PROVIDERS.CLOUD_TYPE' | translate }}</td>
                  <td>{{ 'COMMON.YES' | translate }} (<code>AIza...</code>)</td>
                  <td><code>https://generativelanguage.googleapis.com</code></td>
                  <td>gemini-2.0-flash, gemini-pro</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.PROVIDERS.ADD_TITLE' | translate }}</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.PROVIDERS.ADD_STEP1' | translate }}</strong>
                  <p>{{ 'GUIDE.PROVIDERS.ADD_STEP1_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.PROVIDERS.ADD_STEP2' | translate }}</strong>
                  <p>{{ 'GUIDE.PROVIDERS.ADD_STEP2_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.PROVIDERS.ADD_STEP3' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.PROVIDERS.ADD_STEP3_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <div>
                  <strong>{{ 'GUIDE.PROVIDERS.ADD_STEP4' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.PROVIDERS.ADD_STEP4_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">5</span>
                <div>
                  <strong>{{ 'GUIDE.PROVIDERS.ADD_STEP5' | translate }}</strong>
                  <p>{{ 'GUIDE.PROVIDERS.ADD_STEP5_DESC' | translate }}</p>
                </div>
              </div>
            </div>

            <h3>{{ 'GUIDE.PROVIDERS.CARD_TITLE' | translate }}</h3>
            <p>{{ 'GUIDE.PROVIDERS.CARD_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.PROVIDERS.CARD_ICON' | translate"></li>
              <li [innerHTML]="'GUIDE.PROVIDERS.CARD_NAME' | translate"></li>
              <li [innerHTML]="'GUIDE.PROVIDERS.CARD_STATUS' | translate"></li>
              <li [innerHTML]="'GUIDE.PROVIDERS.CARD_TYPE' | translate"></li>
              <li [innerHTML]="'GUIDE.PROVIDERS.CARD_MODEL' | translate"></li>
              <li [innerHTML]="'GUIDE.PROVIDERS.CARD_MODELS' | translate"></li>
            </ul>

            <h3>{{ 'GUIDE.PROVIDERS.TEST_TITLE' | translate }}</h3>
            <p [innerHTML]="'GUIDE.PROVIDERS.TEST_DESC' | translate"></p>

            <h3>{{ 'GUIDE.PROVIDERS.REMOVE_TITLE' | translate }}</h3>
            <p [innerHTML]="'GUIDE.PROVIDERS.REMOVE_DESC' | translate"></p>

            <div class="info-box" [innerHTML]="'GUIDE.PROVIDERS.NOTIFICATIONS' | translate"></div>
          </div>
        }
      </section>

      <!-- SEZIONE 8: MCP -->
      <section id="mcp" class="guide-section">
        <div class="section-header" (click)="toggle('mcp')">
          <div class="section-title">
            <span class="section-icon">&#x1F527;</span>
            <h2>{{ 'GUIDE.MCP.TITLE' | translate }}</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'mcp' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'mcp') {
          <div class="section-body">
            <p class="section-intro" [innerHTML]="'GUIDE.MCP.INTRO' | translate"></p>

            <h3>{{ 'GUIDE.MCP.TAB_SERVERS' | translate }}</h3>

            <h4>{{ 'GUIDE.MCP.REGISTER_TITLE' | translate }}</h4>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.REGISTER_STEP1' | translate }}</strong>
                  <p>{{ 'GUIDE.MCP.REGISTER_STEP1_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.REGISTER_STEP2' | translate }}</strong>
                  <p>{{ 'GUIDE.MCP.REGISTER_STEP2_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.REGISTER_STEP3' | translate }}</strong>
                  <p>{{ 'GUIDE.MCP.REGISTER_STEP3_DESC' | translate }}</p>
                </div>
              </div>
            </div>

            <h4>{{ 'GUIDE.MCP.COMMON_FIELDS' | translate }}</h4>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.MCP.TH_FIELD' | translate }}</th>
                  <th>{{ 'GUIDE.MCP.TH_REQUIRED' | translate }}</th>
                  <th>{{ 'GUIDE.MCP.TH_DESCRIPTION' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.MCP.FIELD_NAME' | translate }}</td>
                  <td>{{ 'COMMON.YES' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.FIELD_NAME_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.FIELD_DESC' | translate }}</td>
                  <td>{{ 'COMMON.NO' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.FIELD_DESC_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.FIELD_TYPE' | translate }}</td>
                  <td>{{ 'COMMON.YES' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.FIELD_TYPE_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.FIELD_TIMEOUT' | translate }}</td>
                  <td>{{ 'COMMON.NO' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.FIELD_TIMEOUT_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.FIELD_RECONNECT' | translate }}</td>
                  <td>{{ 'COMMON.NO' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.FIELD_RECONNECT_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h4>{{ 'GUIDE.MCP.TYPE_FIELDS' | translate }}</h4>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.MCP.TH_TYPE' | translate }}</th>
                  <th>{{ 'GUIDE.MCP.TH_FIELD_NAME' | translate }}</th>
                  <th>{{ 'GUIDE.MCP.TH_DESCRIPTION' | translate }}</th>
                  <th>{{ 'GUIDE.MCP.TH_EXAMPLE' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td rowspan="2"><strong>STDIO</strong></td>
                  <td>{{ 'GUIDE.MCP.STDIO_COMMAND' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.STDIO_COMMAND_DESC' | translate }}</td>
                  <td><code>npx</code></td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.STDIO_ARGS' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.STDIO_ARGS_DESC' | translate }}</td>
                  <td><code>-y,&#64;modelcontextprotocol/server-filesystem,./</code></td>
                </tr>
                <tr>
                  <td><strong>SSE</strong></td>
                  <td>{{ 'GUIDE.MCP.SSE_URL' | translate }}</td>
                  <td>{{ 'GUIDE.MCP.SSE_URL_DESC' | translate }}</td>
                  <td><code>http://localhost:8082/sse</code></td>
                </tr>
              </tbody>
            </table>

            <h4>{{ 'GUIDE.MCP.STATUSES' | translate }}</h4>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_STATUS' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_BADGE' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_DESCRIPTION' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ 'GUIDE.MCP.STATUS_CONNECTED' | translate }}</td>
                  <td><span class="inline-badge bg-green">CONNECTED</span></td>
                  <td>{{ 'GUIDE.MCP.STATUS_CONNECTED_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.STATUS_DISCONNECTED' | translate }}</td>
                  <td><span class="inline-badge bg-neutral">DISCONNECTED</span></td>
                  <td>{{ 'GUIDE.MCP.STATUS_DISCONNECTED_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.STATUS_ERROR' | translate }}</td>
                  <td><span class="inline-badge bg-red">ERROR</span></td>
                  <td>{{ 'GUIDE.MCP.STATUS_ERROR_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td>{{ 'GUIDE.MCP.STATUS_CONNECTING' | translate }}</td>
                  <td><span class="inline-badge bg-orange">CONNECTING</span></td>
                  <td>{{ 'GUIDE.MCP.STATUS_CONNECTING_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h4>{{ 'GUIDE.MCP.ACTIONS' | translate }}</h4>
            <table class="info-table">
              <thead>
                <tr>
                  <th>{{ 'GUIDE.FOLDERS.TH_ACTION' | translate }}</th>
                  <th>{{ 'GUIDE.DOCUMENTS.TH_DESCRIPTION' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td [innerHTML]="'GUIDE.MCP.ACTION_TEST' | translate"></td>
                  <td>{{ 'GUIDE.MCP.ACTION_TEST_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td [innerHTML]="'GUIDE.MCP.ACTION_RECONNECT' | translate"></td>
                  <td>{{ 'GUIDE.MCP.ACTION_RECONNECT_DESC' | translate }}</td>
                </tr>
                <tr>
                  <td [innerHTML]="'GUIDE.MCP.ACTION_DELETE' | translate"></td>
                  <td>{{ 'GUIDE.MCP.ACTION_DELETE_DESC' | translate }}</td>
                </tr>
              </tbody>
            </table>

            <h3>{{ 'GUIDE.MCP.TAB_TOOLS' | translate }}</h3>

            <h4>{{ 'GUIDE.MCP.LOCAL_TOOLS' | translate }}</h4>
            <p [innerHTML]="'GUIDE.MCP.LOCAL_TOOLS_DESC' | translate"></p>

            <h4>{{ 'GUIDE.MCP.EXTERNAL_TOOLS' | translate }}</h4>
            <p [innerHTML]="'GUIDE.MCP.EXTERNAL_TOOLS_DESC' | translate"></p>

            <h4>{{ 'GUIDE.MCP.EXEC_TITLE' | translate }}</h4>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.EXEC_STEP1' | translate }}</strong>
                  <p>{{ 'GUIDE.MCP.EXEC_STEP1_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.EXEC_STEP2' | translate }}</strong>
                  <p [innerHTML]="'GUIDE.MCP.EXEC_STEP2_DESC' | translate"></p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.EXEC_STEP3' | translate }}</strong>
                  <p>{{ 'GUIDE.MCP.EXEC_STEP3_DESC' | translate }}</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <div>
                  <strong>{{ 'GUIDE.MCP.EXEC_STEP4' | translate }}</strong>
                  <p>{{ 'GUIDE.MCP.EXEC_STEP4_DESC' | translate }}</p>
                </div>
              </div>
            </div>

            <h4>{{ 'GUIDE.MCP.RESULTS_TITLE' | translate }}</h4>
            <p>{{ 'GUIDE.MCP.RESULTS_DESC' | translate }}</p>
            <ul class="detail-list">
              <li [innerHTML]="'GUIDE.MCP.RESULT_STATUS' | translate"></li>
              <li [innerHTML]="'GUIDE.MCP.RESULT_TIME' | translate"></li>
              <li [innerHTML]="'GUIDE.MCP.RESULT_ERROR' | translate"></li>
              <li [innerHTML]="'GUIDE.MCP.RESULT_DATA' | translate"></li>
            </ul>

            <div class="info-box" [innerHTML]="'GUIDE.MCP.REFRESH_INFO' | translate"></div>
          </div>
        }
      </section>

    </div>
  `,
  styles: [`
    .guide-page { max-width: 900px; }

    .page-header { margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: #888; font-size: 0.85rem; margin: 0.15rem 0 0 0; }

    /* Indice */
    .card { background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); padding: 1.25rem; }
    .toc { margin-bottom: 1.5rem; }
    .toc h3 { margin: 0 0 0.75rem 0; font-size: 0.95rem; color: #333; }
    .toc-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 0.5rem;
    }
    .toc-item {
      display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 0.75rem;
      border-radius: 8px; background: #f8f9fb; cursor: pointer; font-size: 0.82rem;
      font-weight: 500; color: #333; text-decoration: none; border: 1px solid #e8ecf1;
      transition: all 0.15s;
    }
    .toc-item:hover { background: #eef1f6; border-color: #cdd3dc; }
    .toc-icon { font-size: 1rem; }

    /* Sezioni */
    .guide-section {
      margin-bottom: 0.5rem;
      background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      overflow: hidden;
    }
    .section-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 1rem 1.25rem; cursor: pointer; user-select: none;
    }
    .section-header:hover { background: #fafbfc; }
    .section-title { display: flex; align-items: center; gap: 0.6rem; }
    .section-icon { font-size: 1.2rem; }
    .section-title h2 { margin: 0; font-size: 1.05rem; color: #0f3460; }
    .section-arrow { font-size: 0.7rem; color: #888; }

    .section-body { padding: 0 1.25rem 1.5rem 1.25rem; }
    .section-intro { font-size: 0.85rem; color: #555; line-height: 1.6; margin: 0 0 1.25rem 0; }

    .section-body h3 {
      font-size: 0.92rem; color: #0f3460; margin: 1.5rem 0 0.5rem 0;
      padding-bottom: 0.25rem; border-bottom: 1px solid #e8ecf1;
    }
    .section-body h3:first-child { margin-top: 0; }
    .section-body h4 {
      font-size: 0.85rem; color: #444; margin: 1rem 0 0.4rem 0;
    }
    .section-body p { font-size: 0.83rem; color: #555; line-height: 1.55; margin: 0 0 0.5rem 0; }

    /* Steps */
    .steps { display: flex; flex-direction: column; gap: 0.6rem; margin: 0.5rem 0; }
    .step {
      display: flex; gap: 0.75rem; align-items: flex-start;
      padding: 0.65rem 0.85rem; background: #f8fafe; border-radius: 6px;
      border-left: 3px solid #0f3460;
    }
    .step-num {
      display: flex; align-items: center; justify-content: center;
      width: 24px; height: 24px; border-radius: 50%; flex-shrink: 0;
      background: #0f3460; color: white; font-size: 0.72rem; font-weight: 700;
    }
    .step strong { font-size: 0.83rem; display: block; margin-bottom: 0.15rem; color: #222; }
    .step p { font-size: 0.8rem; color: #555; margin: 0; line-height: 1.5; }

    /* Tabelle */
    .info-table {
      width: 100%; border-collapse: collapse; font-size: 0.8rem; margin: 0.5rem 0;
    }
    .info-table th {
      text-align: left; padding: 0.45rem 0.6rem;
      background: #f0f2f5; color: #444; font-weight: 600; border-bottom: 2px solid #ddd;
    }
    .info-table td {
      padding: 0.45rem 0.6rem; border-bottom: 1px solid #eee; color: #555;
      vertical-align: top;
    }
    .info-table tr:last-child td { border-bottom: none; }
    .info-table code {
      background: #eef1f6; padding: 0.1rem 0.35rem; border-radius: 3px; font-size: 0.75rem;
    }

    /* Liste */
    .detail-list {
      margin: 0.4rem 0 0.6rem 1.25rem; padding: 0; font-size: 0.82rem; color: #555;
    }
    .detail-list li { margin-bottom: 0.3rem; line-height: 1.5; }
    .detail-list li strong { color: #333; }

    /* Info box */
    .info-box {
      padding: 0.75rem 1rem; background: #e8f4fd; border-radius: 6px;
      border-left: 3px solid #3498db; font-size: 0.82rem; color: #2c3e50;
      line-height: 1.5; margin-top: 0.75rem;
    }
    .info-box strong { color: #0f3460; }

    /* Badges inline */
    .inline-badge {
      display: inline-block; padding: 0.08rem 0.4rem; border-radius: 3px;
      font-size: 0.68rem; font-weight: 600; vertical-align: middle;
    }
    .bg-green { background: #d4edda; color: #155724; }
    .bg-yellow { background: #fff3cd; color: #856404; }
    .bg-blue { background: #cce5ff; color: #004085; }
    .bg-red { background: #f8d7da; color: #721c24; }
    .bg-neutral { background: #e2e3e5; color: #383d41; }
    .bg-orange { background: #ffe0b2; color: #e65100; }
    .bg-purple { background: #e8daef; color: #6c3483; }

    kbd {
      display: inline-block; padding: 0.05rem 0.35rem; font-size: 0.72rem;
      border: 1px solid #ccc; border-radius: 3px; background: #f7f7f7;
      font-family: inherit; box-shadow: 0 1px 0 #bbb;
    }
    code {
      background: #eef1f6; padding: 0.1rem 0.35rem; border-radius: 3px; font-size: 0.75rem;
    }
  `]
})
export class GuidePageComponent {
  openSection = signal<string | null>('start');

  toggle(section: string) {
    this.openSection.set(this.openSection() === section ? null : section);
  }

  scrollTo(id: string) {
    this.openSection.set(id);
    setTimeout(() => {
      document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 50);
  }
}
