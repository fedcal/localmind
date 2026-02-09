# Specifica Funzionale: Interfaccia Utente

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Interfaccia Utente              |
| **Versione** | 0.1.0                          |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Panoramica Tecnologica](#1-panoramica-tecnologica)
2. [Layout Generale](#2-layout-generale)
3. [Sezione Chat](#3-sezione-chat)
4. [Sezione Documents](#4-sezione-documents)
5. [Sezione Search](#5-sezione-search)
6. [Sezione Folders](#6-sezione-folders)
7. [Sezione Settings](#7-sezione-settings)
8. [Sezione Dashboard](#8-sezione-dashboard)
9. [Modalita' Interfaccia](#9-modalita-interfaccia)

---

## 1. Panoramica Tecnologica

L'interfaccia utente di LocalMind e' costruita con le seguenti tecnologie:

| Componente          | Tecnologia                        |
|--------------------|-----------------------------------|
| Framework           | Angular 21                        |
| Linguaggio          | TypeScript (strict mode)          |
| Architettura        | Standalone components             |
| State management    | Angular Signals                   |
| Styling             | SCSS con design system custom     |
| HTTP client         | Angular HttpClient                |
| Routing             | Angular Router (lazy loading)     |
| Build               | Angular CLI / esbuild             |

### 1.1 Principi di Design

- **Component-based**: ogni elemento dell'interfaccia e' un componente Angular standalone riusabile
- **Reactive**: lo stato dell'applicazione e' gestito tramite Signal per aggiornamenti granulari e performanti
- **Accessible**: conformita' WCAG 2.1 livello AA per accessibilita'
- **Responsive**: layout adattivo per desktop e tablet (mobile come obiettivo futuro)

---

## 2. Layout Generale

L'interfaccia segue un layout a due colonne:

```
+------------------+----------------------------------------------+
|                  |                                              |
|    SIDEBAR       |              AREA CONTENUTO                  |
|    (navigazione) |              (pagina corrente)               |
|                  |                                              |
|  +------------+  |                                              |
|  | Logo       |  |                                              |
|  +------------+  |                                              |
|  | Chat       |  |                                              |
|  | Documents  |  |                                              |
|  | Search     |  |                                              |
|  | Folders    |  |                                              |
|  | Settings   |  |                                              |
|  | Dashboard  |  |                                              |
|  +------------+  |                                              |
|                  |                                              |
|  +------------+  |                                              |
|  | Modalita'  |  |                                              |
|  | [Semplice] |  |                                              |
|  +------------+  |                                              |
|                  |                                              |
+------------------+----------------------------------------------+
```

### 2.1 Sidebar

- **Larghezza**: 260px (collassabile a 64px con icone)
- **Colore sfondo**: #1a1a2e (dark theme)
- **Colore testo**: #e0e0e0
- **Colore accento**: #4f46e5 (indigo)
- **Elementi**: logo, voci di navigazione con icone, selettore modalita'
- **Comportamento**: fissa su desktop, overlay su tablet

### 2.2 Area Contenuto

- **Sfondo**: #f8f9fa (light) o #121212 (dark mode)
- **Padding**: 24px
- **Header**: titolo pagina + breadcrumb + azioni contestuali
- **Contenuto**: specifico per ogni sezione

---

## 3. Sezione Chat

La sezione Chat e' l'interfaccia principale per l'interazione con gli LLM e gli agenti AI.

### 3.1 Layout

```
+----------------------------------------------+
| Chat                         [Provider: Ollama v] [Modello: llama3.2 v] |
+----------------------------------------------+
|                                              |
|  +----------------------------------------+  |
|  | [Sistema] Ciao! Come posso aiutarti?   |  |
|  +----------------------------------------+  |
|                                              |
|  +----------------------------------------+  |
|  | [Utente] Spiega l'architettura         |  |
|  | esagonale                               |  |
|  +----------------------------------------+  |
|                                              |
|  +----------------------------------------+  |
|  | [Assistente] L'architettura esagonale  |  |
|  | e' un pattern architetturale...        |  |
|  |                                        |  |
|  | Fonti: doc1.pdf (p.12), doc2.pdf (p.3) |  |
|  +----------------------------------------+  |
|                                              |
|  +----------------------------------------+  |
|  | [Loading...]                ///         |  |
|  +----------------------------------------+  |
|                                              |
+----------------------------------------------+
| [Agente: Tech v]  [Messaggio...        ] [>] |
+----------------------------------------------+
```

### 3.2 Componenti

| Componente              | Descrizione                                          |
|------------------------|------------------------------------------------------|
| **Header**              | Selezione provider e modello (dropdown)              |
| **Message list**        | Lista scrollabile di messaggi (utente/assistente)    |
| **Message bubble**      | Singolo messaggio con avatar, testo, timestamp       |
| **Citation block**      | Blocco citazione fonti RAG (documento, pagina, score)|
| **Loading indicator**   | Indicatore di caricamento durante la generazione     |
| **Input bar**           | Selezione agente, campo di testo, pulsante invio     |
| **Token counter**       | Contatore token utilizzati (modalita' avanzata)      |

### 3.3 Interazioni

- **Invio messaggio**: click pulsante o tasto Enter
- **Selezione provider**: dropdown con provider abilitati
- **Selezione modello**: dropdown con modelli disponibili per il provider selezionato
- **Selezione agente**: dropdown con agenti disponibili (Tech, Business, Legal, Personal)
- **Copy risposta**: pulsante copia per ogni messaggio assistente
- **Retry**: pulsante per rigenerare l'ultima risposta

---

## 4. Sezione Documents

La sezione Documents mostra i documenti caricati e il loro stato di indicizzazione.

### 4.1 Layout

```
+----------------------------------------------+
| Documents                    [Upload +]      |
+----------------------------------------------+
|                                              |
|  +----------+  +----------+  +----------+   |
|  | report   |  | contratto|  | email    |   |
|  | .pdf     |  | .docx    |  | .eml     |   |
|  |          |  |          |  |          |   |
|  | 2.3 MB   |  | 156 KB   |  | 45 KB    |   |
|  | 12 chunks|  | 8 chunks |  | 3 chunks |   |
|  |          |  |          |  |          |   |
|  | [INDEXED]|  | [PENDING]|  | [FAILED] |   |
|  +----------+  +----------+  +----------+   |
|                                              |
|  +----------+  +----------+                  |
|  | manuale  |  | slides   |                  |
|  | .txt     |  | .pdf     |                  |
|  |          |  |          |                  |
|  | 89 KB    |  | 5.1 MB   |                  |
|  | 15 chunks|  | PROCESSING|                  |
|  |          |  |          |                  |
|  | [INDEXED]|  | [...]    |                  |
|  +----------+  +----------+                  |
|                                              |
+----------------------------------------------+
```

### 4.2 Status Badge

| Stato        | Colore     | Codice Hex  | Icona        |
|-------------|------------|-------------|--------------|
| `PENDING`   | Giallo     | #f59e0b     | Clock        |
| `PROCESSING`| Blu        | #3b82f6     | Spinner      |
| `INDEXED`   | Verde      | #10b981     | Checkmark    |
| `FAILED`    | Rosso      | #ef4444     | X            |
| `ARCHIVED`  | Grigio     | #6b7280     | Archive      |

### 4.3 Azioni

- **Upload**: dialog per upload file (drag-and-drop o file picker)
- **Dettaglio documento**: click sulla card per visualizzare metadati, chunk, embedding info
- **Retry**: pulsante per ritentare l'indicizzazione di documenti FAILED
- **Archive**: pulsante per archiviare un documento
- **Delete**: pulsante per eliminare un documento e i relativi chunk/embedding

---

## 5. Sezione Search

La sezione Search offre un'interfaccia per la ricerca semantica nei documenti indicizzati.

### 5.1 Layout

```
+----------------------------------------------+
| Search                                       |
+----------------------------------------------+
|                                              |
| +------------------------------------------+ |
| | Cerca nei tuoi documenti...        [Cerca]| |
| +------------------------------------------+ |
|                                              |
| Risultati per: "clausola di recesso"         |
|                                              |
| +------------------------------------------+ |
| | contratto.docx - Chunk 4 - Score: 0.92   | |
| | "...la parte puo' recedere dal contratto  | |
| | con preavviso di 30 giorni..."            | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | accordo.pdf - Chunk 12 - Score: 0.87     | |
| | "...il recesso unilaterale e' consentito  | |
| | nei seguenti casi..."                     | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | policy.docx - Chunk 7 - Score: 0.74      | |
| | "...modalita' di recesso anticipato..."   | |
| +------------------------------------------+ |
|                                              |
+----------------------------------------------+
```

### 5.2 Componenti

- **Barra di ricerca**: campo di testo con pulsante di ricerca e suggerimenti
- **Risultato**: card con nome file, indice chunk, similarity score, testo estratto
- **Score indicator**: barra visuale del similarity score (0.0 - 1.0)
- **Filtri** (modalita' avanzata): top_k, similarity threshold, filtro per formato/data

---

## 6. Sezione Folders

La sezione Folders consente la configurazione delle cartelle del filesystem da indicizzare.

### 6.1 Layout

```
+----------------------------------------------+
| Folders                     [Aggiungi +]     |
+----------------------------------------------+
|                                              |
| +------------------------------------------+ |
| | /home/utente/documenti/lavoro             | |
| | Ricorsivo: Si | Abilitato: Si            | |
| | Ultimo scan: 2026-02-09 15:30            | |
| | File trovati: 42 | Indicizzati: 38       | |
| | [Scan ora] [Modifica] [Rimuovi]          | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | /home/utente/documenti/personali          | |
| | Ricorsivo: No | Abilitato: Si            | |
| | Ultimo scan: 2026-02-09 15:15            | |
| | File trovati: 15 | Indicizzati: 15       | |
| | [Scan ora] [Modifica] [Rimuovi]          | |
| +------------------------------------------+ |
|                                              |
+----------------------------------------------+
```

### 6.2 Funzionalita'

- **Aggiungi cartella**: dialog per specificare path, ricorsivita', abilitazione
- **Scan immediato**: trigger manuale della scansione per una cartella specifica
- **Modifica**: modifica dei parametri della cartella (ricorsivita', abilitazione)
- **Rimuovi**: rimozione della cartella dalla lista di monitoraggio (non elimina i file)
- **Statistiche**: numero file trovati, indicizzati, in errore

---

## 7. Sezione Settings

La sezione Settings permette la configurazione dei provider LLM e delle API key.

### 7.1 Layout

```
+----------------------------------------------+
| Settings                                     |
+----------------------------------------------+
|                                              |
| Provider LLM                                 |
| +------------------------------------------+ |
| | Ollama          [Abilitato: Si]          | |
| | URL: http://localhost:11434              | |
| | Modello: llama3.2                        | |
| | Stato: Connesso                          | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | OpenAI          [Abilitato: No]          | |
| | API Key: sk-...****                      | |
| | Modello: gpt-4o-mini                     | |
| | Stato: Non configurato                   | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | Anthropic       [Abilitato: No]          | |
| | API Key: sk-ant-...****                  | |
| | Modello: claude-3-5-sonnet               | |
| | Stato: Non configurato                   | |
| +------------------------------------------+ |
|                                              |
| Provider Default: [Ollama v]                 |
| Fallback abilitato: [Si]                    |
| Ordine fallback: [OLLAMA, OPENAI, ...]      |
|                                              |
| [Salva configurazione]                       |
+----------------------------------------------+
```

### 7.2 Funzionalita'

- **Abilitazione provider**: toggle per abilitare/disabilitare ciascun provider
- **API key**: campo masked per inserimento chiave API (cloud provider)
- **Test connessione**: pulsante per verificare la connettivita' al provider
- **Selezione provider default**: dropdown per scegliere il provider predefinito
- **Configurazione fallback**: toggle abilitazione e ordinamento catena fallback
- **Parametri avanzati** (modalita' avanzata): timeout, temperatura default, max_tokens default

---

## 8. Sezione Dashboard

La sezione Dashboard fornisce una panoramica dello stato del sistema e delle metriche di utilizzo.

### 8.1 Layout

```
+----------------------------------------------+
| Dashboard                                    |
+----------------------------------------------+
|                                              |
| +--------+ +--------+ +--------+ +--------+ |
| |Postgres| |Qdrant  | |Ollama  | |n8n     | |
| | UP     | | UP     | | UP     | | DOWN   | |
| | 23ms   | | 15ms   | | 45ms   | | --     | |
| +--------+ +--------+ +--------+ +--------+ |
|                                              |
| Statistiche Utilizzo                         |
| +------------------------------------------+ |
| | Token totali: 1,234,567                  | |
| | Costo totale: $2.45                      | |
| | Richieste totali: 342                    | |
| | Latenza media: 1,230ms                   | |
| +------------------------------------------+ |
|                                              |
| Documenti                                    |
| +------------------------------------------+ |
| | Totale: 156                              | |
| | Indicizzati: 142 | Pending: 8           | |
| | Failed: 4 | Archived: 2                 | |
| +------------------------------------------+ |
|                                              |
| Batch Jobs                                   |
| +------------------------------------------+ |
| | Completati: 45 | Falliti: 2             | |
| | In corso: 1                              | |
| | Ultimo job: 2026-02-09 15:30             | |
| +------------------------------------------+ |
|                                              |
+----------------------------------------------+
```

### 8.2 Aggiornamento Real-Time

Le stat card utilizzano Angular Signals per aggiornamenti reattivi. Il frontend effettua polling periodico (default: ogni 30 secondi) sull'endpoint `/api/v1/dashboard/health` e aggiorna le card tramite signal.

---

## 9. Modalita' Interfaccia

### 9.1 Modalita' Semplice

Interfaccia ridotta con le sole funzionalita' essenziali:

- Chat con selezione agente (nessun parametro tecnico)
- Upload documenti con drag-and-drop
- Ricerca con barra di testo semplice
- Dashboard con health status basilare
- Nessuna configurazione avanzata visibile

### 9.2 Modalita' Avanzata

Interfaccia completa con tutti i parametri configurabili:

- Chat con selezione provider, modello, temperatura, max_tokens
- Configurazione RAG visibile (chunk size, overlap, similarity threshold, top_k)
- Metriche dettagliate (token usage, latency, costi per richiesta)
- Log e diagnostica accessibili
- Configurazione completa dei provider

### 9.3 Preset per Ruolo

Configurazioni predefinite che attivano l'agente e i parametri ottimali per il ruolo:

| Preset       | Agente   | Temperatura | Max Tokens | Modalita' UI |
|-------------|----------|-------------|------------|-------------|
| Developer   | Tech     | 0.2         | 4096       | Avanzata    |
| Business    | Business | 0.5         | 4096       | Semplice    |
| Legal       | Legal    | 0.1         | 4096       | Semplice    |
| Personal    | Personal | 0.7         | 2048       | Semplice    |
