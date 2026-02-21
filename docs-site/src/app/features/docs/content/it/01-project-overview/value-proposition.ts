export const content = `# Proposta di Valore

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Proposta di Valore              |
| **Versione** | 0.1.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Introduzione](#1-introduzione)
2. [Local-First: Dati sotto Controllo](#2-local-first-dati-sotto-controllo)
3. [Multi-Provider LLM](#3-multi-provider-llm)
4. [RAG Integrato](#4-rag-integrato)
5. [Automazioni No-Code](#5-automazioni-no-code)
6. [Stack Enterprise Java](#6-stack-enterprise-java)
7. [Modello di Costo Sostenibile](#7-modello-di-costo-sostenibile)
8. [Estensibilita' Architetturale](#8-estensibilita-architetturale)
9. [Sintesi della Proposta di Valore](#9-sintesi-della-proposta-di-valore)

---

## 1. Introduzione

Il presente documento articola in dettaglio la proposta di valore di LocalMind, identificando per ogni elemento il vantaggio competitivo rispetto alle alternative presenti sul mercato e l'impatto concreto per l'utente finale.

---

## 2. Local-First: Dati sotto Controllo

### 2.1 Principio

I dati dell'utente non abbandonano mai il computer locale. Ogni operazione di elaborazione (estrazione testo, chunking, embedding, ricerca semantica) avviene interamente sulla macchina dell'utente.

### 2.2 Implementazione Tecnica

- **LLM locale**: Ollama esegue i modelli direttamente sulla macchina, senza chiamate di rete
- **Vector store locale**: Qdrant viene eseguito sulla macchina dell'utente (nativamente o tramite Docker)
- **Database locale**: MySQL archivia metadati e configurazioni localmente
- **Filesystem locale**: i documenti originali restano nella loro posizione sul filesystem

### 2.3 Vantaggio Competitivo

A differenza di ChatGPT, Claude.ai e Notion AI, che richiedono l'invio di dati a server remoti, LocalMind garantisce che informazioni sensibili (contratti, dati finanziari, codice proprietario) non vengano mai trasmesse a terze parti.

### 2.4 Impatto Utente

- Conformita' GDPR senza configurazioni aggiuntive
- Utilizzabile in contesti regolamentati (sanita', finanza, difesa)
- Nessun rischio di data breach derivante da provider cloud
- Operativita' in ambienti air-gapped

---

## 3. Multi-Provider LLM

### 3.1 Principio

LocalMind non e' vincolato a nessun singolo provider LLM. L'utente puo' scegliere liberamente tra provider locali e cloud, con fallback automatico in caso di indisponibilita'.

### 3.2 Provider Supportati

| Provider     | Tipo    | Modelli Esempio                    | Costo            |
|--------------|---------|------------------------------------|------------------|
| **Ollama**   | Locale  | Llama 3, Mistral, Phi-3, Gemma     | Gratuito         |
| **OpenAI**   | Cloud   | GPT-4o, GPT-4o-mini                | Pay-per-use      |
| **Anthropic**| Cloud   | Claude 3.5 Sonnet, Claude 3 Opus   | Pay-per-use      |
| **Google**   | Cloud   | Gemini 1.5 Pro, Gemini 1.5 Flash   | Pay-per-use      |

### 3.3 Meccanismo di Fallback

La catena di fallback e' configurabile dall'utente. La configurazione predefinita e':

\`\`\`
OLLAMA -> OPENAI -> ANTHROPIC -> GOOGLE
\`\`\`

Se il provider primario (Ollama) non e' disponibile o non risponde entro il timeout configurato, il sistema passa automaticamente al provider successivo nella catena, garantendo continuita' di servizio.

### 3.4 Vantaggio Competitivo

Nessuna soluzione local-first esistente (AnythingLLM, Jan.ai, GPT4All) offre un gateway multi-provider con fallback automatico e cost tracking integrato. Le soluzioni cloud (ChatGPT, Claude.ai) sono vincolate al singolo provider.

### 3.5 Impatto Utente

- Zero vendor lock-in: migrazione tra provider senza modifiche
- Continuita' di servizio garantita dal fallback automatico
- Ottimizzazione costi: utilizzo del provider piu' economico per ogni task
- Confronto qualitativo: possibilita' di testare lo stesso prompt su provider diversi

---

## 4. RAG Integrato

### 4.1 Principio

LocalMind integra un pipeline RAG (Retrieval-Augmented Generation) completo che permette di interrogare i propri documenti locali con ricerca semantica, ottenendo risposte basate sulla propria knowledge base.

### 4.2 Pipeline

1. **Ingestione**: upload manuale o scansione automatica di cartelle locali
2. **Estrazione testo**: Apache Tika 2.9.2 supporta PDF, DOCX, TXT, EML e altri formati
3. **Chunking**: suddivisione del testo in segmenti configurabili (default 500 caratteri, overlap 50)
4. **Embedding**: generazione vettori tramite Ollama (nomic-embed-text) o provider cloud
5. **Storage**: archiviazione vettori in Qdrant per ricerca semantica
6. **Retrieval**: ricerca per similarita' con score e citazione delle fonti

### 4.3 Indicizzazione da Filesystem

Una funzionalita' distintiva di LocalMind e' l'indicizzazione diretta da cartelle del filesystem locale:

- Configurazione di percorsi multipli da monitorare
- Scansione ricorsiva opzionale delle sottocartelle
- Scheduling automatico tramite Spring Batch (default: ogni 15 minuti)
- Indicizzazione incrementale con deduplicazione via hash SHA-256

### 4.4 Vantaggio Competitivo

Le soluzioni cloud (ChatGPT, Claude.ai) non accedono al filesystem locale. Le soluzioni local-first (Jan.ai, GPT4All) non offrono RAG o lo offrono in forma basilare. LangChain offre RAG avanzato ma richiede sviluppo Python custom.

### 4.5 Impatto Utente

- Ricerca intelligente sui propri documenti senza cloud
- Risposte basate sulla propria knowledge base con citazione delle fonti
- Indicizzazione automatica senza intervento manuale
- Analisi di grandi volumi documentali tramite batch processing

---

## 5. Automazioni No-Code

### 5.1 Principio

LocalMind si integra nativamente con n8n, piattaforma di automazione open-source e self-hosted, per consentire la creazione di workflow automatici senza scrittura di codice.

### 5.2 Esempi di Workflow

- **Documento caricato** -> summary automatica -> salvataggio in cartella specifica
- **Email ricevuta** -> classificazione AI -> tag automatici -> archiviazione
- **Report settimanale** -> generazione automatica -> invio via email
- **Nuovo file in cartella** -> indicizzazione RAG -> notifica utente

### 5.3 Vantaggio Competitivo

Nessun competitor nel panorama AI local-first offre integrazione nativa con piattaforme di automazione. Le soluzioni che offrono automazioni (come n8n stesso) non includono funzionalita' AI/RAG integrate.

### 5.4 Impatto Utente

- Automatizzazione di task ripetitivi senza competenze di programmazione
- Orchestrazione di workflow complessi tra AI, documenti e servizi esterni
- Riduzione del tempo dedicato ad attivita' manuali

---

## 6. Stack Enterprise Java

### 6.1 Principio

LocalMind e' costruito interamente su stack Java 17 / Spring Boot 3.4 / Angular 21, le tecnologie piu' diffuse nell'ecosistema enterprise mondiale.

### 6.2 Componenti Tecnologici

| Componente       | Tecnologia               | Motivazione                                    |
|------------------|--------------------------|------------------------------------------------|
| Backend          | Java 17, Spring Boot 3.4 | Standard enterprise, vasto ecosistema          |
| AI Integration   | Spring AI 1.0.0          | Integrazione AI nativa per Spring Boot         |
| Frontend         | Angular 21               | Framework enterprise-grade, TypeScript         |
| Database         | MySQL 8.0                | Database relazionale open-source di riferimento|
| Vector Store     | Qdrant                   | Vector database performante e open-source      |
| Batch            | Spring Batch             | Framework batch processing consolidato         |
| Build            | Maven                    | Gestione dipendenze e build standard           |

### 6.3 Vantaggio Competitivo

Tutte le alternative nel panorama AI local-first e open-source sono costruite su:

- **Python**: LangChain, LlamaIndex, PrivateGPT, Haystack
- **Electron/Node.js**: AnythingLLM, Jan.ai, LM Studio
- **C++/Go**: GPT4All, Ollama (solo runtime)

LocalMind e' l'unica piattaforma AI local-first su stack Java/Spring Boot, il che la rende immediatamente adottabile da team enterprise Java senza necessita' di nuove competenze.

### 6.4 Impatto Utente

- Manutenzione con competenze Java gia' presenti in azienda
- Integrazione con infrastrutture enterprise esistenti
- Deployment su piattaforme standard (Docker, Kubernetes)
- Monitoring con strumenti consolidati (Actuator, Micrometer)

---

## 7. Modello di Costo Sostenibile

### 7.1 Struttura dei Costi

| Componente         | Costo                                                    |
|--------------------|----------------------------------------------------------|
| **LocalMind**      | Open-source, gratuito                                    |
| **Ollama**         | Gratuito (esecuzione locale)                             |
| **MySQL**          | Gratuito (open-source)                                   |
| **Qdrant**         | Gratuito (open-source, self-hosted)                      |
| **n8n**            | Gratuito (open-source, self-hosted)                      |
| **OpenAI API**     | Pay-per-use (es. GPT-4o: ~$5/$15 per 1M token)           |
| **Anthropic API**  | Pay-per-use (es. Sonnet: ~$3/$15 per 1M token)           |
| **Google API**     | Pay-per-use (es. Gemini Pro: ~$3.50/$10.50 per 1M token) |

### 7.2 Confronto Costi

| Soluzione        | Costo Mensile Tipico   | Modello                    |
|-------------- ---|------------------------|----------------------------|
| ChatGPT Plus     | $20/mese per utente    | Abbonamento                |
| Claude Pro       | $20/mese per utente    | Abbonamento                |
| Notion AI        | $10/mese per utente    | Add-on abbonamento         |
| Copilot Pro      | $20/mese per utente    | Abbonamento                |
| **LocalMind**    | **$0 (solo locale)**   | **Gratuito + pay-per-use** |

### 7.3 Impatto Utente

- Nessun abbonamento obbligatorio
- Costo zero con Ollama locale
- Costi cloud controllati e monitorabili in tempo reale
- Scalabilita' economica: il costo cresce solo con l'effettivo utilizzo cloud

---

## 8. Estensibilita' Architetturale

### 8.1 Principio

L'architettura esagonale di LocalMind garantisce che nuovi provider LLM, nuovi formati documentali, nuovi tipi di agente e nuovi canali di automazione possano essere aggiunti senza modificare il dominio applicativo.

### 8.2 Meccanismo di Estensione

Per aggiungere un nuovo provider LLM e' sufficiente:

1. Creare un nuovo adapter che implementi l'interfaccia \`LlmClient\` (port out)
2. Registrare l'adapter nel container Spring
3. Aggiungere la configurazione del provider in \`application.yml\`

Il dominio (servizi, entita', use case) non viene toccato.

### 8.3 Vantaggio Competitivo

Le soluzioni monolitiche (ChatGPT, Jan.ai) non sono estensibili. Le soluzioni framework (LangChain) richiedono sviluppo custom. LocalMind offre un modello di estensione basato su interfacce Java standard, familiare a ogni sviluppatore Spring.

### 8.4 Impatto Utente

- Aggiornamento ai nuovi modelli AI senza riscritture
- Aggiunta di nuove funzionalita' senza impatto sulle esistenti
- Possibilita' di contribuire all'ecosistema con adapter custom

---

## 9. Sintesi della Proposta di Valore

| Pilastro               | Valore                                                    |
|------------------------|-----------------------------------------------------------|
| **Privacy**            | Dati locali, zero cloud obbligatorio                      |
| **Liberta'**           | Multi-provider, zero vendor lock-in                       |
| **Intelligenza**       | RAG integrato con ricerca semantica                       |
| **Automazione**        | n8n per workflow no-code                                  |
| **Enterprise**         | Java/Spring Boot/Angular, competenze diffuse              |
| **Economia**           | Ollama gratuito, cloud pay-per-use, zero abbonamenti      |
| **Evoluzione**         | Architettura esagonale, estensibilita' illimitata         |

LocalMind e' la piattaforma che unifica AI avanzata, privacy e controllo in un unico sistema enterprise-grade, accessibile a ogni tipo di utente e pronto per evolvere con il panorama AI.
`;
