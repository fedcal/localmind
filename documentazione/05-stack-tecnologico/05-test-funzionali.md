# Test Funzionali - LocalMind

Questo documento elenca tutti i test funzionali dell'applicativo LocalMind, organizzati per modulo. Ogni test verifica un flusso end-to-end dal punto di vista dell'utente finale.

**Totale test: 211** | **Aree coperte: 17**

---

## Indice

1. [Autenticazione](#1-autenticazione)
2. [Chat LLM](#2-chat-llm)
3. [Gestione Conversazioni](#3-gestione-conversazioni)
4. [Gestione Documenti](#4-gestione-documenti)
5. [Ricerca Semantica](#5-ricerca-semantica)
6. [Cartelle Monitorate](#6-cartelle-monitorate)
7. [Configurazione Provider LLM](#7-configurazione-provider-llm)
8. [Server MCP](#8-server-mcp)
9. [Tool MCP](#9-tool-mcp)
10. [Modelli LLM](#10-modelli-llm)
11. [Webhook e Automazione](#11-webhook-e-automazione)
12. [Dashboard](#12-dashboard)
13. [Agenti Specializzati](#13-agenti-specializzati)
14. [Internazionalizzazione](#14-internazionalizzazione)
15. [Navigazione e Layout](#15-navigazione-e-layout)
16. [Gestione Errori Trasversali](#16-gestione-errori-trasversali)
17. [MCP Scrum, Time e Incidents](#17-mcp-scrum-time-e-incidents)

---

## 1. Autenticazione

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| AUTH-01 | Setup password iniziale | Prima esecuzione, nessuna password configurata | Form setup visibile, dopo submit redirect a chat con token JWT |
| AUTH-02 | Setup - password non corrispondenti | `password` != `confirmPassword` | Errore di validazione, nessun account creato |
| AUTH-03 | Setup - password vuota | Campo password vuoto | Validazione @NotBlank, errore 400 |
| AUTH-04 | Setup - doppio setup | Tentativo di setup quando password gia configurata | Errore, setup gia completato |
| AUTH-05 | Login con password corretta | Credenziali valide | Token JWT restituito, redirect a chat |
| AUTH-06 | Login con password errata | Password sbagliata | Errore 401, messaggio "credenziali non valide" |
| AUTH-07 | Login con campo vuoto | Password non fornita | Validazione 400 |
| AUTH-08 | Verifica stato autenticazione | GET /auth/status | `configured: true/false`, `authenticated: true/false` |
| AUTH-09 | Cambio password con successo | Password corrente valida + nuova password | Password aggiornata, 204 |
| AUTH-10 | Cambio password - corrente errata | Password corrente sbagliata | Errore 401 |
| AUTH-11 | Token scaduto | Richiesta con token oltre 24h | Errore 401, redirect a login |
| AUTH-12 | Token invalido/malformato | Header Authorization con token corrotto | Errore 401 |
| AUTH-13 | Richiesta senza token | Accesso a endpoint protetto senza Authorization | Errore 401, redirect a login |
| AUTH-14 | Guard Angular su route protette | Navigazione a /chat senza autenticazione | Redirect a /login |
| AUTH-15 | Logout | Click su logout | Token rimosso da localStorage, redirect a /login |

---

## 2. Chat LLM

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| CHAT-01 | Invio messaggio base | Messaggio testuale semplice | Risposta LLM ricevuta, nuova conversazione creata |
| CHAT-02 | Invio messaggio in streaming | Tool calling disabilitato | Token ricevuti via SSE uno alla volta, risposta composta progressivamente |
| CHAT-03 | Invio messaggio sincrono | Tool calling abilitato | Risposta completa ricevuta in blocco |
| CHAT-04 | Messaggio vuoto | Campo messaggio vuoto, invio | Validazione @NotBlank, errore 400 |
| CHAT-05 | Selezione provider specifico | Scelta OPENAI dal dropdown | Richiesta instradata al provider scelto |
| CHAT-06 | Selezione modello specifico | Scelta llama3.2 | Risposta dal modello selezionato |
| CHAT-07 | Fallback provider | Provider primario non disponibile | Risposta dal provider successivo nella catena |
| CHAT-08 | Tutti i provider non disponibili | Nessun provider raggiungibile | Errore 502 con messaggio LlmProviderException |
| CHAT-09 | Continuazione conversazione | Invio messaggio con conversationId esistente | Messaggio aggiunto alla conversazione esistente |
| CHAT-10 | System prompt personalizzato | Impostazione system prompt prima dell'invio | LLM rispetta le istruzioni del system prompt |
| CHAT-11 | System prompt - limite caratteri | System prompt > 5000 caratteri | Troncamento o validazione frontend |
| CHAT-12 | RAG abilitato | `enableRag: true` con documenti indicizzati | Risposta contiene ragSources con documentId, score |
| CHAT-13 | RAG senza documenti | `enableRag: true` ma nessun documento indicizzato | Risposta normale senza fonti RAG |
| CHAT-14 | Tool calling abilitato | `enableToolCalling: true` con tool MCP disponibili | Loop agentico (max 3 iterazioni), tool eseguiti |
| CHAT-15 | Tool calling - max iterazioni | Tool che richiede piu di 3 iterazioni | Stop a 3 iterazioni, risposta parziale |
| CHAT-16 | Parametro temperatura | `temperature: 0.1` vs `temperature: 1.0` | Risposte piu deterministiche vs piu creative |
| CHAT-17 | Parametro maxTokens | `maxTokens: 50` | Risposta troncata entro il limite |
| CHAT-18 | Tracking utilizzo | Invio messaggio con successo | UsageRecord salvato (provider, model, tokens, latency) |
| CHAT-19 | SSE - eventi conversation | Inizio streaming | Evento `conversation` con conversationId |
| CHAT-20 | SSE - evento done | Fine streaming | Evento `done` ricevuto, UI aggiorna stato |
| CHAT-21 | SSE - evento error | Errore durante streaming | Evento `error` con messaggio, UI mostra errore |
| CHAT-22 | SSE - metadata | Fine streaming | Evento `metadata` con model, provider, latencyMs, tokenUsage |

---

## 3. Gestione Conversazioni

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| CONV-01 | Lista conversazioni | GET /conversations | Lista ordinata per updatedAt DESC |
| CONV-02 | Lista paginata | `page=0, size=20` | Prima pagina di 20 conversazioni con metadata paginazione |
| CONV-03 | Paginazione - pagina successiva | `page=1, size=20` con >20 conversazioni | Seconda pagina corretta |
| CONV-04 | Ricerca per contenuto | `query=keyword` | Solo conversazioni contenenti il termine |
| CONV-05 | Ricerca senza risultati | `query=xyznonexistent` | Lista vuota |
| CONV-06 | Dettaglio conversazione | GET /conversations/{id} | Conversazione completa con tutti i messaggi |
| CONV-07 | Conversazione inesistente | GET /conversations/{id-invalido} | Errore 404 |
| CONV-08 | Rinomina conversazione | PATCH con nuovo titolo | Titolo aggiornato |
| CONV-09 | Rinomina - titolo vuoto | Titolo blank | Validazione @NotBlank, errore 400 |
| CONV-10 | Auto-titolo | Nuova conversazione da messaggio lungo | Titolo = primi 100 caratteri + "..." |
| CONV-11 | Aggiorna system prompt | PATCH /system-prompt | System prompt aggiornato per la conversazione |
| CONV-12 | Aggiorna context window | PATCH /context-window con max=10 | Solo ultimi 10 messaggi inviati all'LLM |
| CONV-13 | Aggiungi tag | POST /tags con `tag: "progetto-x"` | Tag aggiunto, visibile nella lista |
| CONV-14 | Rimuovi tag | DELETE /tags/{tag} | Tag rimosso |
| CONV-15 | Filtra per tag | GET /conversations?tag=progetto-x | Solo conversazioni con quel tag |
| CONV-16 | Aggiungi tag vuoto | `tag: ""` | Validazione @NotBlank, errore 400 |
| CONV-17 | Elimina conversazione | DELETE /conversations/{id} | Conversazione rimossa, 204 |
| CONV-18 | Elimina - conferma modale | Click elimina nel frontend | Modale di conferma prima della cancellazione |
| CONV-19 | Aggiunta risultato tool | POST /tool-result con toolName e content | Messaggio TOOL aggiunto alla conversazione |
| CONV-20 | Risultato tool - campi vuoti | toolName o content vuoti | Validazione @NotBlank, errore 400 |
| CONV-21 | Metadata conversazione | Update metadata con chiavi custom | Metadata salvato e restituito |

---

## 4. Gestione Documenti

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| DOC-01 | Upload PDF | File .pdf valido | Documento creato con status PENDING -> INDEXED |
| DOC-02 | Upload TXT | File .txt | Testo estratto e indicizzato |
| DOC-03 | Upload Markdown | File .md | Contenuto markdown estratto |
| DOC-04 | Upload DOCX | File .docx | Testo estratto da Word |
| DOC-05 | Upload CSV | File .csv | Contenuto tabellare estratto |
| DOC-06 | Upload JSON | File .json | Contenuto JSON estratto |
| DOC-07 | Upload file duplicato | Stesso file (stesso hash SHA-256) | Documento esistente restituito, nessun duplicato |
| DOC-08 | Upload file vuoto | File 0 bytes | Gestione errore o documento vuoto |
| DOC-09 | Upload senza file | Richiesta POST senza multipart file | Errore 400 |
| DOC-10 | Lista documenti | GET /documents | Tutti i documenti con status, size, date |
| DOC-11 | Filtro per status frontend | Tab "Indexed" / "Pending" / "Failed" | Solo documenti con lo status selezionato |
| DOC-12 | Dettaglio documento | GET /documents/{id} | Tutti i campi del documento |
| DOC-13 | Documento inesistente | GET /documents/{id-invalido} | Errore 404 |
| DOC-14 | Elimina documento | DELETE /documents/{id} | Documento + chunk + vettori eliminati (cascade) |
| DOC-15 | Elimina - conferma modale | Click elimina nel frontend | Modale conferma prima della cancellazione |
| DOC-16 | Pipeline ingestion completa | Upload -> estrazione -> chunking -> embedding -> indexing | Status progression: PENDING -> PROCESSING -> INDEXED |
| DOC-17 | Pipeline - errore estrazione | File corrotto | Status = FAILED, DocumentProcessingException |
| DOC-18 | Chunking con overlap | Documento lungo | Chunk con sovrapposizione corretta |
| DOC-19 | Chunking - testo corto | Testo < chunkSize | Un singolo chunk |
| DOC-20 | MIME type detection | Upload .pdf, .docx, .md | MIME type corretto assegnato automaticamente |
| DOC-21 | Hash SHA-256 | Upload documento | Hash calcolato e salvato per deduplicazione |

---

## 5. Ricerca Semantica

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| SEARCH-01 | Ricerca base | Query testuale con documenti indicizzati | Risultati ordinati per score di similarita |
| SEARCH-02 | Ricerca con topK personalizzato | `topK: 3` | Esattamente 3 risultati (se disponibili) |
| SEARCH-03 | Ricerca senza risultati | Query non correlata ai documenti | Lista vuota o risultati con score basso |
| SEARCH-04 | Ricerca query vuota | `query: ""` | Validazione @NotBlank, errore 400 |
| SEARCH-05 | Ricerca senza documenti | Nessun documento indicizzato | Lista vuota |
| SEARCH-06 | Score di similarita | Query pertinente | Score tra 0 e 1, risultati piu rilevanti in cima |
| SEARCH-07 | Contenuto chunk nel risultato | Ricerca con match | content, documentId, filename, chunkIndex presenti |
| SEARCH-08 | TopK values frontend | Selezione 3, 5, 10, 20 | Numero risultati corrisponde alla selezione |
| SEARCH-09 | Hint di ricerca | Click su suggerimento nel frontend | Query auto-compilata e ricerca eseguita |

---

## 6. Cartelle Monitorate

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| FOLD-01 | Aggiungi cartella | Path valido | Cartella creata con status ACTIVE |
| FOLD-02 | Aggiungi cartella - path vuoto | `path: ""` | Validazione @NotBlank, errore 400 |
| FOLD-03 | Aggiungi cartella - path inesistente | Path che non esiste sul filesystem | Gestione errore appropriata |
| FOLD-04 | Cartella ricorsiva | `recursive: true` | Scansione sottodirectory incluse |
| FOLD-05 | Cartella non ricorsiva | `recursive: false` | Solo file nella directory radice |
| FOLD-06 | Watch abilitato | `watchEnabled: true` | Monitoraggio file system attivo |
| FOLD-07 | Lista cartelle | GET /folders | Tutte le cartelle con status e conteggio documenti |
| FOLD-08 | Rimuovi cartella | DELETE /folders/{id} | Cartella rimossa, 204 |
| FOLD-09 | Sync manuale | POST /folders/{id}/sync | Status SYNCING -> documenti ingestiti -> ACTIVE |
| FOLD-10 | Sync - errore | Cartella non piu accessibile | Status = ERROR |
| FOLD-11 | Sync - file duplicati | File gia indicizzati nella cartella | Deduplicazione tramite hash, nessun duplicato |
| FOLD-12 | Conteggio documenti | Dopo sync | documentCount aggiornato correttamente |
| FOLD-13 | Ultimo sync | Dopo sync completato | lastSyncAt aggiornato |

---

## 7. Configurazione Provider LLM

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| SETT-01 | Lista provider | GET /settings/providers | Tutti i provider configurati |
| SETT-02 | Crea provider Ollama | Nome, tipo OLLAMA, baseUrl | Provider creato con successo |
| SETT-03 | Crea provider OpenAI | Nome, tipo OPENAI, baseUrl, apiKey | Provider creato |
| SETT-04 | Crea provider - nome vuoto | `name: ""` | Validazione @NotBlank, errore 400 |
| SETT-05 | Crea provider - tipo nullo | `type: null` | Validazione @NotNull, errore 400 |
| SETT-06 | Crea provider - baseUrl vuota | `baseUrl: ""` | Validazione @NotBlank, errore 400 |
| SETT-07 | Elimina provider | DELETE /settings/providers/{id} | Provider rimosso, 204 |
| SETT-08 | Test connessione Ollama | Provider Ollama configurato e raggiungibile | Status "success" |
| SETT-09 | Test connessione - provider offline | Provider non raggiungibile | Status "error" con messaggio |
| SETT-10 | Test connessione OpenAI | API key valida | Status "success" (verifica presenza chiave) |
| SETT-11 | Status Ollama - online | Ollama in esecuzione | `online: true`, version, lista modelli |
| SETT-12 | Status Ollama - offline | Ollama non in esecuzione | `online: false`, errorMessage |
| SETT-13 | Lista modelli Ollama | Ollama online con modelli | Lista nomi modelli scaricati |
| SETT-14 | Pull modello Ollama | `modelName: "llama3.2"` | Download avviato, modello disponibile |
| SETT-15 | Pull modello - streaming | GET /ollama/models/pull/stream | Eventi SSE progress con stato download |
| SETT-16 | Elimina modello Ollama | DELETE /ollama/models/{modelName} | Modello rimosso |
| SETT-17 | Pull modello - campi vuoti | baseUrl o modelName vuoti | Validazione @NotBlank, errore 400 |
| SETT-18 | Aggiorna modello default | PUT /{id}/default-model | defaultModel aggiornato |
| SETT-19 | Avvio Ollama | POST /ollama/start | Tentativo avvio servizio |
| SETT-20 | Validazione form frontend | Compilazione incompleta | Errori inline, submit disabilitato |

---

## 8. Server MCP

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| MCP-S01 | Registra server STDIO | Tipo STDIO con command e args | Server creato, tentativo connessione |
| MCP-S02 | Registra server SSE | Tipo SSE con URL | Server creato, tentativo connessione |
| MCP-S03 | Registra - nome vuoto | `name: ""` | Validazione @NotBlank, errore 400 |
| MCP-S04 | Registra - tipo nullo | `type: null` | Validazione @NotNull, errore 400 |
| MCP-S05 | Lista server | GET /mcp/servers | Tutti i server con status |
| MCP-S06 | Dettaglio server | GET /mcp/servers/{id} | Config completa + status + timestamp |
| MCP-S07 | Elimina server | DELETE /mcp/servers/{id} | Server disconnesso e rimosso, 204 |
| MCP-S08 | Test connessione - server attivo | Server raggiungibile | Status CONNECTED |
| MCP-S09 | Test connessione - server offline | Server non raggiungibile | Status ERROR |
| MCP-S10 | Riconnessione | POST /reconnect su server disconnesso | Status CONNECTING -> CONNECTED |
| MCP-S11 | Timeout configurabile | `timeoutSeconds: 60` | Timeout rispettato nelle chiamate |
| MCP-S12 | Auto-reconnect | `autoReconnect: true`, server riavviato | Riconnessione automatica |
| MCP-S13 | Server inesistente | GET /mcp/servers/{id-invalido} | Errore 404 |

---

## 9. Tool MCP

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| MCP-T01 | Lista tool esterni | Server connesso con tool | Lista con name, description, inputSchema |
| MCP-T02 | Lista tool per server | GET /tools/servers/{serverId} | Solo tool del server specificato |
| MCP-T03 | Lista tool locali | GET /tools/local | Tool built-in (135 nativi) |
| MCP-T04 | Esegui tool con successo | toolName, serverId, arguments validi | result con success=true, executionTimeMs |
| MCP-T05 | Esegui tool - errore | Tool che fallisce | success=false, errorMessage presente |
| MCP-T06 | Esegui tool - nome vuoto | `toolName: ""` | Validazione @NotBlank, errore 400 |
| MCP-T07 | Esegui tool - serverId vuoto | `serverId: ""` | Validazione @NotBlank, errore 400 |
| MCP-T08 | Tool da server disconnesso | Server con status != CONNECTED | Errore, tool non disponibile |
| MCP-T09 | Risultato tool in conversazione | Esecuzione tool durante chat | Messaggio TOOL aggiunto con metadata |

---

## 10. Modelli LLM

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| MOD-01 | Lista tutti i modelli | GET /models | Modelli da tutti i provider disponibili |
| MOD-02 | Modelli per provider | Filtro per OLLAMA | Solo modelli Ollama |
| MOD-03 | Dettaglio modello | GET /models/{id} | name, provider, contextWindow, available |
| MOD-04 | Modello inesistente | GET /models/{id-invalido} | Errore 404 |
| MOD-05 | Disponibilita modello | Provider offline | `available: false` |

---

## 11. Webhook e Automazione

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| HOOK-01 | Crea webhook | Nome, URL, eventType validi | Webhook creato con active=true |
| HOOK-02 | Crea - nome vuoto | `name: ""` | Validazione @NotBlank, errore 400 |
| HOOK-03 | Crea - nome troppo corto | `name: "a"` (< 2 chars) | Validazione @Size, errore 400 |
| HOOK-04 | Crea - URL vuota | `url: ""` | Validazione @NotBlank, errore 400 |
| HOOK-05 | Crea - eventType vuoto | `eventType: ""` | Validazione @NotBlank, errore 400 |
| HOOK-06 | Lista webhook | GET /webhooks | Tutti i webhook con stato |
| HOOK-07 | Dettaglio webhook | GET /webhooks/{id} | Tutti i campi |
| HOOK-08 | Aggiorna webhook | PUT /webhooks/{id} | Campi aggiornati |
| HOOK-09 | Elimina webhook | DELETE /webhooks/{id} | Webhook rimosso, 204 |
| HOOK-10 | Test webhook | POST /webhooks/{id}/test | Chiamata HTTP alla URL configurata |
| HOOK-11 | Webhook inesistente | GET /webhooks/{id-invalido} | Errore 404 |
| HOOK-12 | Trigger DOCUMENT_INDEXED | Upload e indicizzazione documento | Webhook con eventType DOCUMENT_INDEXED invocato |
| HOOK-13 | Trigger CHAT_COMPLETED | Completamento chat | Webhook con eventType CHAT_COMPLETED invocato |
| HOOK-14 | Webhook disabilitato | `active: false` + evento trigger | Webhook NON invocato |

---

## 12. Dashboard

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| DASH-01 | Health check - tutto UP | Tutti i servizi attivi | `status: "UP"`, services tutti "UP" |
| DASH-02 | Health check - degradato | Ollama offline | `status: "DEGRADED"`, ollama: "DOWN" |
| DASH-03 | Statistiche documenti | Documenti presenti | Conteggio totale documenti |
| DASH-04 | Statistiche MCP | Server connessi | Conteggio server connessi |
| DASH-05 | Quick actions | Click su card azione | Navigazione alla feature corretta |
| DASH-06 | Refresh stats | Click refresh | Dati aggiornati |

---

## 13. Agenti Specializzati

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| AGT-01 | Agente TECH | Query tecnica | Risposta con analisi tecnica e citazioni |
| AGT-02 | Agente BUSINESS | Query business | Risposta con analisi business |
| AGT-03 | Agente LEGAL | Query legale | Risposta con riferimenti normativi |
| AGT-04 | Agente PERSONAL | Query generica | Risposta semplice e amichevole |
| AGT-05 | Agente con tool calls | Query che richiede tool | Tool invocati, risultati integrati |

---

## 14. Internazionalizzazione

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| I18N-01 | Lingua default | Primo accesso | Interfaccia in italiano |
| I18N-02 | Switch a inglese | Click bandiera EN | Tutta l'interfaccia tradotta in inglese |
| I18N-03 | Switch a italiano | Click bandiera IT | Tutta l'interfaccia tradotta in italiano |
| I18N-04 | Persistenza lingua | Cambio lingua + reload pagina | Lingua mantenuta (localStorage) |
| I18N-05 | Interpolazione parametri | `{{count}} documenti` con count=5 | "5 documenti" |
| I18N-06 | Enum tradotte | Status documento in UI | "Indicizzato" (IT) / "Indexed" (EN) |
| I18N-07 | Traduzione validazione | Errore campo obbligatorio | "Campo obbligatorio" (IT) / "Required field" (EN) |

---

## 15. Navigazione e Layout

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| NAV-01 | Sidebar navigation | Click su ogni link | Navigazione alla pagina corretta |
| NAV-02 | Route attiva evidenziata | Pagina chat aperta | Link "Chat" evidenziato in sidebar |
| NAV-03 | Sidebar collassabile | Click toggle sidebar | Sidebar ridotta a icone |
| NAV-04 | Pagina 404 | URL inesistente | Pagina "Not Found" |
| NAV-05 | Lazy loading | Prima navigazione a feature | Modulo caricato on demand |
| NAV-06 | Tema dark/light | Toggle tema | CSS variables aggiornate, tema persistente |
| NAV-07 | Tema persistente | Cambio tema + reload | Tema mantenuto (localStorage) |

---

## 16. Gestione Errori Trasversali

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| ERR-01 | Errore 400 - validazione | Body non valido su qualsiasi endpoint | ErrorResponseDto con campo e messaggio |
| ERR-02 | Errore 401 - non autenticato | Token assente o scaduto | ErrorResponseDto, redirect a login |
| ERR-03 | Errore 404 - risorsa non trovata | ID inesistente | ErrorResponseDto con "not found" |
| ERR-04 | Errore 500 - errore interno | Eccezione non gestita | ErrorResponseDto generico |
| ERR-05 | Errore 502 - provider LLM | Errore dal provider AI | ErrorResponseDto con "LLM provider error" |
| ERR-06 | Toast notifiche | Errore HTTP qualsiasi | Toast visibile per 4 secondi |
| ERR-07 | Loading skeleton | Dati in caricamento | Skeleton placeholder visibile |
| ERR-08 | Pulsanti disabilitati | Operazione asincrona in corso | Pulsante disabilitato, nessun doppio invio |

---

## 17. MCP Scrum, Time e Incidents

### Scrum

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| SCRUM-01 | Crea sprint | Nome, date, obiettivi | Sprint creato |
| SCRUM-02 | Crea user story | Titolo, descrizione, punti | Story creata nel backlog |
| SCRUM-03 | Crea task | Titolo, storyId | Task creato con status TODO |
| SCRUM-04 | Aggiorna status task | `status: "in_progress"` | Status aggiornato |
| SCRUM-05 | Sprint board | GET /sprints/{id}/board | Task raggruppati per status |
| SCRUM-06 | Backlog | GET /backlog | Story non assegnate a sprint |

### Time Tracking

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| TIME-01 | Avvia timer | POST /time/start | Timer avviato |
| TIME-02 | Ferma timer | POST /time/stop | Timer fermato, tempo registrato |
| TIME-03 | Log tempo manuale | POST /time/log | Entry creata |
| TIME-04 | Timesheet | GET /time/timesheet con date range | Entries filtrate per periodo |

### Incident Management

| ID | Test | Scenario | Risultato Atteso |
|----|------|----------|------------------|
| INC-01 | Apri incidente | POST /incidents | Incidente creato con severita |
| INC-02 | Aggiorna incidente | PUT /incidents/{id} | Campi aggiornati |
| INC-03 | Aggiungi timeline | POST /incidents/{id}/timeline | Entry aggiunta |
| INC-04 | Risolvi incidente | POST /incidents/{id}/resolve | Status resolved |
| INC-05 | Genera postmortem | GET /incidents/{id}/postmortem | Report postmortem generato |
| INC-06 | Filtra incidenti | `status=open, severity=high` | Lista filtrata |

---

## Riepilogo

| Area | Test |
|------|------|
| Autenticazione | 15 |
| Chat LLM | 22 |
| Conversazioni | 21 |
| Documenti | 21 |
| Ricerca Semantica | 9 |
| Cartelle Monitorate | 13 |
| Settings Provider | 20 |
| Server MCP | 13 |
| Tool MCP | 9 |
| Modelli LLM | 5 |
| Webhook/Automazione | 14 |
| Dashboard | 6 |
| Agenti | 5 |
| Internazionalizzazione | 7 |
| Navigazione/Layout | 7 |
| Gestione Errori | 8 |
| Scrum/Time/Incidents | 16 |
| **TOTALE** | **211** |
