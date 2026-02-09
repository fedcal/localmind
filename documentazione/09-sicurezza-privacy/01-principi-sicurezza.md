# Principi di Sicurezza e Privacy

| | |
|---|---|
| **Documento** | Principi di Sicurezza e Privacy |
| **Versione** | 0.1.0 |
| **Data** | 2026-02-09 |
| **Progetto** | LocalMind |

---

## Indice

1. [Principio Fondamentale](#1-principio-fondamentale)
2. [I 5 Pilastri della Sicurezza LocalMind](#2-i-5-pilastri-della-sicurezza-localmind)
   - 2.1 [Local-First](#21-local-first)
   - 2.2 [Zero Data Leakage](#22-zero-data-leakage)
   - 2.3 [Cloud Opt-In](#23-cloud-opt-in)
   - 2.4 [No Telemetry](#24-no-telemetry)
   - 2.5 [Self-Hosted](#25-self-hosted)
3. [Confronto Privacy vs Competitor](#3-confronto-privacy-vs-competitor)
4. [Modalita' Offline Totale](#4-modalita-offline-totale)
5. [GDPR Compliance](#5-gdpr-compliance)
6. [Assenza di Vendor Lock-In](#6-assenza-di-vendor-lock-in)

---

## 1. Principio Fondamentale

> **"Your AI, your data, your machine."**

Il principio cardine su cui si fonda l'intera architettura di sicurezza di LocalMind e' che **i dati dell'utente non lasciano mai il computer**. Ogni documento caricato, ogni conversazione avviata, ogni embedding generato e ogni configurazione personale risiede esclusivamente sulla macchina dell'utente finale.

Questo approccio si contrappone radicalmente al modello cloud-first adottato dalla quasi totalita' dei competitor, dove i dati dell'utente vengono trasmessi, elaborati e archiviati su infrastrutture remote, spesso al di fuori del controllo diretto dell'utente stesso.

LocalMind garantisce che:

- Nessun dato transiti su server esterni senza il consenso esplicito dell'utente.
- L'utente mantenga la piena proprieta' e il controllo sui propri dati in ogni momento.
- L'eliminazione dei dati sia immediata, completa e verificabile, poiche' risiede su storage locale.

---

## 2. I 5 Pilastri della Sicurezza LocalMind

### 2.1 Local-First

Tutti i dati gestiti da LocalMind risiedono fisicamente sulla macchina dell'utente:

| Tipo di dato | Storage locale | Tecnologia |
|---|---|---|
| Documenti originali | File system locale | Path configurabili |
| Metadati documenti | MySQL | Istanza locale |
| Embedding vettoriali | Qdrant | Istanza locale (nativa o Docker) |
| Conversazioni e chat | MySQL | Istanza locale |
| Modelli LLM | Ollama model store | Istanza locale (nativa o Docker) |
| Workflow automazioni | n8n | Istanza locale (nativa o Docker) |
| Configurazioni utente | MySQL + file | Istanza locale + .env |

Non esiste alcun componente dell'architettura che richieda obbligatoriamente una connessione a server remoti per il funzionamento base del sistema.

### 2.2 Zero Data Leakage

Nessun dato viene trasmesso a servizi di terze parti senza il consenso esplicito dell'utente.

- **Ollama** opera interamente in locale: i modelli LLM vengono scaricati una sola volta e successivamente eseguiti senza alcuna comunicazione di rete.
- **MySQL** e **Qdrant** accettano connessioni esclusivamente da `localhost` nella configurazione di default.
- Le API REST del backend sono esposte solo su `localhost:8080`.
- Il frontend Angular viene servito su `localhost:4200`.

**Flusso dati in modalita' locale:**

```
Utente -> Angular (localhost:4200)
       -> Spring Boot (localhost:8080)
       -> Ollama (localhost:11434)     [LLM inference]
       -> MySQL (localhost:3306)       [persistence]
       -> Qdrant (localhost:6333)      [vector search]
```

Nessun hop di rete esce dal perimetro `localhost`.

### 2.3 Cloud Opt-In

I provider cloud sono **opzionali** e richiedono un'azione esplicita da parte dell'utente per essere attivati:

- **OpenAI**: richiede l'inserimento manuale di `OPENAI_API_KEY` e l'abilitazione esplicita tramite `localmind.llm.openai.enabled=true`.
- **Anthropic**: richiede l'inserimento manuale di `ANTHROPIC_API_KEY` e l'abilitazione esplicita tramite `localmind.llm.anthropic.enabled=true`.
- **Google AI**: richiede l'inserimento manuale di `GOOGLE_API_KEY` e l'abilitazione esplicita tramite `localmind.llm.google.enabled=true`.

Quando un provider cloud e' abilitato, l'utente e' consapevole che:

1. Il **prompt** (domanda dell'utente) viene inviato al provider.
2. Il **contesto RAG** (frammenti di documenti rilevanti) puo' essere incluso nel prompt.
3. La **risposta** viene ricevuta dal provider e memorizzata localmente.
4. I provider cloud possono applicare le proprie policy di data retention.

**Importante:** in modalita' predefinita (default), nessun provider cloud e' abilitato. Il sistema funziona esclusivamente con Ollama in locale.

### 2.4 No Telemetry

LocalMind **non raccoglie alcun dato di utilizzo** e non trasmette metriche, statistiche o informazioni diagnostiche a server esterni:

- Nessun servizio di analytics (Google Analytics, Mixpanel, Amplitude, ecc.).
- Nessun crash reporting remoto (Sentry, Bugsnag, ecc.).
- Nessuna phone-home call al primo avvio o durante l'utilizzo.
- Nessun tracking di funzionalita' utilizzate o pattern di navigazione.

Le uniche metriche raccolte sono quelle interne, memorizzate localmente su MySQL:

- Numero di token consumati per provider (per tracking costi personale).
- Numero di documenti indicizzati.
- Tempi di risposta delle query (per ottimizzazione locale).

Queste metriche sono accessibili esclusivamente dall'utente tramite la dashboard locale.

### 2.5 Self-Hosted

Ogni componente dell'architettura LocalMind gira sulla macchina dell'utente. Il backend e il frontend vengono eseguiti nativamente tramite script nella cartella `scripts/`, mentre i servizi infrastrutturali possono essere eseguiti nativamente o tramite Docker:

| Componente | Modalita' di esecuzione | Funzione |
|---|---|---|
| MySQL 8.0 | Istanza locale nativa | Database relazionale |
| Qdrant | Nativo o Docker (opzionale) | Vector store per embedding |
| Ollama | Nativo o Docker (opzionale) | LLM inference locale |
| n8n | Nativo o Docker (opzionale) | Workflow automation |
| Spring Boot | JVM locale (script) | Backend API |
| Angular | Dev server locale (script) | Frontend UI |

L'utente ha pieno controllo su:

- **Aggiornamenti**: decide quando e se aggiornare ogni componente.
- **Configurazione**: puo' modificare ogni parametro di ogni servizio.
- **Dati**: puo' eseguire backup, restore, migrazione o eliminazione in qualsiasi momento.
- **Risorse**: puo' allocare CPU, RAM e GPU secondo le proprie esigenze.

---

## 3. Confronto Privacy vs Competitor

| Caratteristica | ChatGPT | Notion AI | PrivateGPT | **LocalMind** |
|---|---|---|---|---|
| **Dove risiedono i dati** | Cloud OpenAI | Cloud Notion | Locale | **Locale** |
| **Training sui dati utente** | Possibile (opt-out disponibile) | Non dichiarato | No | **No** |
| **Persistence strutturata** | Cloud-only | Cloud-only | Limitata | **MySQL + Qdrant locale** |
| **Funziona offline** | No | No | Si' | **Si'** |
| **Vector store dedicato** | Non esposto | Non esposto | In-memory / Chroma | **Qdrant locale** |
| **Automazioni** | No | Limitato | No | **n8n locale** |
| **Multi-provider LLM** | Solo OpenAI | Solo OpenAI | Limitato | **Ollama + OpenAI + Anthropic + Google** |
| **GDPR by design** | No (trasferimento US) | No (trasferimento US) | Si' | **Si'** |
| **Open source** | No | No | Si' | **Si'** |
| **Self-hosted** | N/A | N/A | Parziale | **Completo (esecuzione nativa + Docker opzionale)** |

### Analisi dettagliata

**ChatGPT (OpenAI):**
- I dati delle conversazioni transitano e vengono memorizzati sui server di OpenAI negli Stati Uniti.
- OpenAI si riserva la possibilita' di utilizzare i dati per il training dei modelli, salvo opt-out esplicito tramite le impostazioni dell'account o l'utilizzo della API con data retention disabilitato.
- L'utente non ha controllo diretto sull'infrastruttura.

**Notion AI:**
- I documenti e le conversazioni AI risiedono sull'infrastruttura cloud di Notion.
- Le query AI vengono elaborate tramite provider terzi (tipicamente OpenAI).
- L'utente e' soggetto alle policy di data retention e privacy di Notion e dei suoi sub-processor.

**PrivateGPT:**
- Approccio locale simile a LocalMind, ma con persistence limitata.
- Non offre un database relazionale strutturato per metadati.
- Non include automazioni integrate.
- Architettura meno modulare e meno estensibile.

**LocalMind:**
- Combina il vantaggio della privacy locale con un'architettura enterprise-grade.
- MySQL per persistence strutturata e relazionale.
- Qdrant per vector search ad alte prestazioni.
- Ollama per inference LLM senza dipendenze cloud.
- n8n per automazioni avanzate, il tutto eseguibile localmente.

---

## 4. Modalita' Offline Totale

LocalMind e' progettato per funzionare **completamente offline** quando configurato con Ollama e modelli locali:

### Prerequisiti per il funzionamento offline

1. **Modelli LLM scaricati**: almeno un modello di chat (es. `llama3.2`) e un modello di embedding (es. `nomic-embed-text`) devono essere stati precedentemente scaricati tramite `ollama pull`.
2. **Servizi infrastrutturali avviati**: MySQL, Qdrant, Ollama e n8n devono essere in esecuzione (nativamente o tramite Docker).
3. **Applicazione avviata**: il backend Spring Boot e il frontend Angular devono essere stati compilati e avviati.

### Funzionalita' disponibili offline

| Funzionalita' | Disponibile offline | Note |
|---|---|---|
| Chat con LLM | Si' | Via Ollama locale |
| Indicizzazione documenti | Si' | Embedding via Ollama |
| Ricerca semantica | Si' | Qdrant locale |
| Q&A su documenti | Si' | RAG completo locale |
| Automazioni n8n | Si' | Workflow locali |
| Dashboard e metriche | Si' | Dati da MySQL locale |
| Provider cloud (OpenAI, ecc.) | **No** | Richiedono connessione internet |
| Download nuovi modelli | **No** | Richiede connessione per `ollama pull` |

### Scenari d'uso offline

- **Ambiente air-gapped**: reti aziendali senza accesso a internet.
- **Viaggi**: utilizzo su laptop senza connettivita'.
- **Dati classificati**: ambienti dove la connessione internet e' vietata per policy di sicurezza.
- **Continuita' operativa**: funzionamento garantito anche in caso di interruzione della connettivita'.

---

## 5. GDPR Compliance

LocalMind e', per la propria architettura, intrinsecamente conforme ai principi del **Regolamento Generale sulla Protezione dei Dati (GDPR - Regolamento UE 2016/679)**:

### Principi GDPR soddisfatti

| Principio GDPR | Come LocalMind lo soddisfa |
|---|---|
| **Minimizzazione dei dati** | Vengono raccolti solo i dati strettamente necessari al funzionamento |
| **Limitazione della conservazione** | L'utente ha pieno controllo sulla retention e puo' eliminare i dati in qualsiasi momento |
| **Integrita' e riservatezza** | I dati risiedono esclusivamente sulla macchina dell'utente, protetti dai meccanismi di sicurezza del sistema operativo |
| **Privacy by design** | L'architettura local-first e' progettata fin dall'origine per la privacy |
| **Privacy by default** | La configurazione predefinita non trasmette alcun dato a servizi esterni |
| **Diritto alla cancellazione** | L'utente puo' eliminare qualsiasi dato in qualsiasi momento, con effetto immediato e verificabile |
| **Portabilita' dei dati** | I dati sono in formati standard (MySQL, file system) e facilmente esportabili |

### Assenza di trasferimento transfrontaliero

Poiche' i dati non lasciano la macchina dell'utente:

- Non si verifica alcun trasferimento di dati verso paesi terzi.
- Non e' necessaria alcuna valutazione di adeguatezza (art. 45 GDPR).
- Non sono necessarie clausole contrattuali standard (SCC).
- Non si applicano le problematiche legate alla sentenza Schrems II.

**Eccezione:** quando l'utente abilita volontariamente un provider cloud (OpenAI, Anthropic, Google), i dati inviati nelle query sono soggetti alle policy di data processing del provider selezionato. In tal caso, e' responsabilita' dell'utente verificare la conformita' GDPR del provider scelto.

---

## 6. Assenza di Vendor Lock-In

LocalMind adotta un'architettura modulare basata su porte e adapter (Hexagonal Architecture) che garantisce la sostituibilita' di ogni componente:

### Componenti sostituibili

| Componente attuale | Alternative possibili | Porta/Interfaccia |
|---|---|---|
| MySQL | PostgreSQL, MariaDB, H2 | Spring Data JPA (astrazione) |
| Qdrant | Chroma, Milvus, Weaviate, Pinecone | `VectorStorePort` |
| Ollama | llama.cpp, vLLM, LocalAI | `LlmPort` |
| Spring AI | LangChain4j | `LlmPort` adapter |
| n8n | Apache Airflow, Prefect | `AutomationPort` |
| Angular | React, Vue.js | API REST (disaccoppiato) |

### Migrazione tra componenti

Grazie all'Hexagonal Architecture:

1. **Le porte (interfacce)** definiscono il contratto tra il dominio e l'infrastruttura.
2. **Gli adapter** implementano il contratto per una tecnologia specifica.
3. Per sostituire un componente, e' sufficiente **implementare un nuovo adapter** senza modificare il dominio o i servizi applicativi.

**Esempio: migrazione da Qdrant a Chroma**

```
// Porta esistente (invariata)
public interface VectorStorePort {
    void store(List<EmbeddingChunk> chunks);
    List<SearchResult> search(String query, int topK);
}

// Adapter attuale
@Component
@ConditionalOnProperty(name = "localmind.vectorstore.provider", havingValue = "qdrant")
public class QdrantVectorStoreAdapter implements VectorStorePort { ... }

// Nuovo adapter (da implementare)
@Component
@ConditionalOnProperty(name = "localmind.vectorstore.provider", havingValue = "chroma")
public class ChromaVectorStoreAdapter implements VectorStorePort { ... }
```

La sostituzione richiede unicamente:

1. Implementazione del nuovo adapter.
2. Modifica della property `localmind.vectorstore.provider` nel file di configurazione.
3. Nessuna modifica al codice di dominio o ai servizi applicativi.

Questo approccio garantisce che l'utente non sia mai vincolato a una specifica tecnologia e possa evolvere il proprio stack in base alle proprie esigenze.
