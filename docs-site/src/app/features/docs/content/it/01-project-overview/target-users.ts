export const content = `# Analisi Target Utenti

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Analisi Target Utenti           |
| **Versione** | 0.1.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Introduzione](#1-introduzione)
2. [Macro-Categorie di Utenti](#2-macro-categorie-di-utenti)
3. [Sviluppatori e Team Tecnici](#3-sviluppatori-e-team-tecnici)
4. [Professionisti Business e Legal](#4-professionisti-business-e-legal)
5. [Utenti Comuni](#5-utenti-comuni)
6. [Modalita' Interfaccia Utente](#6-modalita-interfaccia-utente)
7. [Riepilogo Use Case per Target](#7-riepilogo-use-case-per-target)

---

## 1. Introduzione

Il presente documento analizza le tre macro-categorie di utenti a cui LocalMind si rivolge, dettagliando per ciascuna le esigenze specifiche, gli scenari d'uso concreti e le funzionalita' della piattaforma che rispondono a tali esigenze. L'analisi e' propedeutica alla definizione delle priorita' di sviluppo e alla progettazione dell'interfaccia utente.

---

## 2. Macro-Categorie di Utenti

LocalMind identifica tre macro-categorie principali di utenti:

| Categoria                   | Profilo                                      | Agente Primario        |
|-----------------------------|----------------------------------------------|------------------------|
| **Sviluppatori**            | Developer, DevOps, team tecnici              | Tech Agent             |
| **Professionisti Business** | Manager, analisti, avvocati, consulenti      | Business / Legal Agent |
| **Utenti Comuni**           | Studenti, freelancer, utenti domestici       | Personal Agent         |

Ciascuna categoria richiede un diverso livello di complessita' nell'interfaccia, diverse funzionalita' prioritarie e diversi pattern di interazione con la piattaforma.

---

## 3. Sviluppatori e Team Tecnici

### 3.1 Profilo

Sviluppatori software, ingegneri DevOps, architetti di sistema e team tecnici che necessitano di assistenza AI per attivita' quotidiane di sviluppo.

### 3.2 Esigenze Primarie

- **Debug assistito**: analisi di stack trace, log di errore e comportamenti anomali
- **Code review**: revisione di codice con suggerimenti su qualita', sicurezza e performance
- **Analisi tecnica**: comprensione di architetture, librerie e pattern
- **Documentazione**: generazione automatica di documentazione tecnica da codice sorgente
- **Spiegazione codice**: comprensione di codice legacy o unfamiliar

### 3.3 Funzionalita' Apprezzate

Gli sviluppatori rappresentano il target che maggiormente apprezza le scelte architetturali di LocalMind:

- **Architettura esagonale**: separazione netta tra dominio e infrastruttura, testabilita'
- **API REST documentate**: integrazione programmatica con strumenti di sviluppo
- **Estensibilita'**: possibilita' di aggiungere nuovi adapter, provider e tool
- **Multi-modulo Maven**: struttura del progetto pulita e modulare
- **Script di avvio**: esecuzione nativa di backend e frontend tramite script nella cartella \`scripts/\`

### 3.4 Scenari d'Uso Concreti

**Scenario 1 - Debug di Produzione**
Uno sviluppatore riceve un'eccezione \`NullPointerException\` in produzione. Copia lo stack trace nella chat LocalMind con il Tech Agent attivo. L'agente analizza il trace, identifica la riga problematica, suggerisce la causa probabile (campo nullable non gestito) e propone una correzione con codice.

**Scenario 2 - Code Review Pre-Commit**
Prima di un merge request, lo sviluppatore sottopone una classe Java al Tech Agent. L'agente identifica: un potenziale memory leak (stream non chiuso), una violazione del principio Single Responsibility e un campo non thread-safe. Fornisce suggerimenti con codice correttivo.

**Scenario 3 - Comprensione Libreria**
Il team deve integrare una libreria open-source sconosciuta. Lo sviluppatore carica la documentazione della libreria nel RAG di LocalMind e interroga il Tech Agent per comprendere pattern di utilizzo, best practice e potenziali criticita'.

**Scenario 4 - Generazione Documentazione**
Lo sviluppatore indicizza il codice sorgente di un progetto nel sistema RAG. Utilizza il Tech Agent per generare automaticamente documentazione API, diagrammi di flusso e guide di architettura basate sul codice reale.

---

## 4. Professionisti Business e Legal

### 4.1 Profilo

Manager, analisti di business, consulenti aziendali, avvocati e professionisti legali che necessitano di assistenza AI per analisi documentale e reporting.

### 4.2 Esigenze Primarie

- **Report automatici**: generazione di report strutturati da dati e documenti
- **Sintesi documenti**: riassunti esecutivi di documenti lunghi e complessi
- **Analisi contratti**: identificazione di clausole critiche, rischi e incoerenze
- **Riferimenti normativi**: ricerca e citazione di normative pertinenti
- **Compliance**: verifica di conformita' a regolamenti e standard

### 4.3 Agenti Dedicati

**Business Agent**: specializzato in analisi di business, reporting e sintesi. Il system prompt e' calibrato per produrre output strutturati, professionali e orientati alle decisioni.

**Legal Agent**: specializzato in analisi giuridica, contrattualistica e compliance. Il system prompt include istruzioni per citare fonti normative, identificare rischi contrattuali e produrre output conformi agli standard legali.

### 4.4 Scenari d'Uso Concreti

**Scenario 1 - Sintesi Report Trimestrale**
Un manager carica il report finanziario trimestrale (PDF, 120 pagine) nel sistema RAG. Chiede al Business Agent una sintesi esecutiva dei KPI principali, delle variazioni rispetto al trimestre precedente e dei rischi identificati. L'agente produce una sintesi strutturata con tabelle e bullet point.

**Scenario 2 - Analisi Contratto di Fornitura**
Un avvocato carica un contratto di fornitura nel sistema RAG. Chiede al Legal Agent di identificare: clausole di limitazione di responsabilita', penali contrattuali, clausole di recesso e potenziali rischi. L'agente produce un'analisi strutturata con riferimenti alle clausole specifiche (numero articolo, pagina).

**Scenario 3 - Due Diligence Documentale**
Un consulente deve analizzare 50 documenti aziendali per una due diligence. Indicizza tutti i documenti tramite folder scanning. Utilizza la ricerca semantica per identificare rapidamente clausole relative a proprieta' intellettuale, contenziosi pendenti e obblighi finanziari.

**Scenario 4 - Report Settimanale Automatico**
Il manager configura un'automazione n8n: ogni venerdi', LocalMind genera automaticamente un report settimanale basato sui documenti indicizzati durante la settimana, con sintesi, classificazione e metriche aggregate. Il report viene inviato via email tramite n8n.

---

## 5. Utenti Comuni

### 5.1 Profilo

Studenti, freelancer, utenti domestici e professionisti non tecnici che desiderano un assistente AI personale senza la complessita' di soluzioni enterprise.

### 5.2 Esigenze Primarie

- **Interfaccia semplice**: esperienza d'uso intuitiva, senza parametri tecnici esposti
- **Spiegazioni accessibili**: risposte chiare, comprensibili, prive di gergo tecnico
- **Assistenza quotidiana**: aiuto nella scrittura, traduzione, organizzazione, ricerca
- **Privacy**: garanzia che i dati personali non vengano inviati a servizi cloud

### 5.3 Agente Dedicato

**Personal Agent**: specializzato in comunicazione chiara e accessibile. Il system prompt e' calibrato per produrre risposte semplici, utilizzare esempi concreti e evitare terminologia tecnica non necessaria.

### 5.4 Scenari d'Uso Concreti

**Scenario 1 - Assistente allo Studio**
Uno studente universitario carica le dispense del corso nel sistema RAG. Chiede al Personal Agent di spiegare concetti complessi con linguaggio semplice, generare riassunti per argomento e creare quiz di autovalutazione.

**Scenario 2 - Gestione Documenti Personali**
Un utente indicizza la cartella dei propri documenti personali (bollette, contratti, ricevute). Utilizza la ricerca semantica per trovare rapidamente documenti specifici ("contratto affitto 2024", "bolletta luce gennaio").

**Scenario 3 - Scrittura Assistita**
Un freelancer utilizza il Personal Agent per assistenza nella scrittura di email professionali, proposte commerciali e contenuti per social media. L'agente adatta il tono e lo stile al contesto richiesto.

---

## 6. Modalita' Interfaccia Utente

LocalMind offre tre modalita' di interfaccia per adattarsi ai diversi livelli di competenza tecnica degli utenti:

### 6.1 Modalita' Semplice

- Interfaccia essenziale con le sole funzionalita' fondamentali
- Nessun parametro tecnico visibile (temperatura, top_p, max_tokens nascosti)
- Selezione agente con descrizioni in linguaggio naturale
- Upload documenti con drag-and-drop
- Ricerca con barra di testo semplice
- Destinata a: utenti comuni, professionisti non tecnici

### 6.2 Modalita' Avanzata

- Tutti i parametri LLM configurabili (temperatura, top_p, max_tokens, presence_penalty, frequency_penalty)
- Configurazione RAG visibile (chunk size, overlap, similarity threshold, top_k)
- Selezione esplicita di provider e modello LLM
- Visualizzazione metriche dettagliate (token usage, latency, costi)
- Accesso completo a log e diagnostica
- Destinata a: sviluppatori, utenti tecnici

### 6.3 Preset per Ruolo

- Configurazioni predefinite ottimizzate per ruolo specifico
- Ogni preset attiva l'agente appropriato, imposta i parametri LLM ottimali e configura la UI
- Preset disponibili:
  - **Developer**: Tech Agent, temperatura bassa (0.2), max_tokens alto (4096), modalita' avanzata
  - **Business**: Business Agent, temperatura media (0.5), output strutturato, modalita' semplice
  - **Legal**: Legal Agent, temperatura bassa (0.1), citazioni obbligatorie, modalita' semplice
  - **Personal**: Personal Agent, temperatura media (0.7), linguaggio accessibile, modalita' semplice

---

## 7. Riepilogo Use Case per Target

| Target         | Use Case Primario                   | Agente   | Modalita' UI |
|----------------|-------------------------------------|----------|--------------|
| Sviluppatore   | Debug, code review, analisi tecnica | Tech     | Avanzata     |
| Sviluppatore   | Generazione documentazione          | Tech     | Avanzata     |
| Business       | Report automatici, sintesi          | Business | Semplice     |
| Legal          | Analisi contratti, compliance       | Legal    | Semplice     |
| Legal          | Riferimenti normativi               | Legal    | Semplice     |
| Utente Comune  | Assistenza studio                   | Personal | Semplice     |
| Utente Comune  | Gestione documenti personali        | Personal | Semplice     |
| Utente Comune  | Scrittura assistita                 | Personal | Semplice     |
`;
