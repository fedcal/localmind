# Contributing to LocalMind

Grazie per il tuo interesse nel contribuire a LocalMind! / Thank you for your interest in contributing to LocalMind!

## Sviluppo Locale / Local Development

### Prerequisiti / Prerequisites
- Java 17+
- Maven 3.9+
- Node.js 22+
- npm 11+
- Docker (per MySQL)

### Setup
1. Clona il repository / Clone the repository
2. Copia `.env.example` in `.env` / Copy `.env.example` to `.env`
3. Avvia MySQL: `docker start mysql-db-root` / Start MySQL
4. Backend: `./scripts/start-backend.sh`
5. Frontend: `./scripts/start-frontend.sh`

### Test
```bash
# Backend
cd localmind-backend && mvn test

# Frontend E2E
cd localmind-frontend && npm run test:e2e

# Frontend Build
cd localmind-frontend && npx ng build
```

## Struttura del Progetto / Project Structure

- `localmind-backend/` — Backend Spring Boot (architettura esagonale)
  - `localmind-domain/` — Domain puro Java
  - `localmind-infrastructure/` — Adapter Spring (DB, LLM, MCP)
  - `localmind-api/` — REST Controller e DTO
  - `localmind-batch/` — Job Spring Batch
  - `localmind-app/` — Entry point, Flyway migration
- `localmind-frontend/` — Frontend Angular 21
- `documentazione/` — Documentazione (IT)
- `documentation/` — Documentation (EN)
- `Sviluppi/` — Development tracking files

## Convenzioni / Conventions

### Commit
- Messaggi chiari e descrittivi / Clear and descriptive messages
- Formato: `Tipo: descrizione breve` (es. `Fix: corretta validazione password`)
- Esempi / Examples:
  - `Fix: corretta validazione password`
  - `Feature: aggiunta autenticazione locale`
  - `Docs: aggiornata documentazione API`
  - `Test: aggiunti test per ChatController`

### Codice / Code
- Backend: architettura esagonale, domain puro senza dipendenze Spring
- Frontend: standalone components, Angular Signals per state management
- Test: unit test puri (no servizi esterni)
- Documentazione bilingue (IT/EN)

### Flyway Migration
- Ogni script SQL contiene una sola query
- Naming: `V{numero}__descrizione.sql`
- Usare backtick per parole riservate MySQL (`recursive`, `timestamp`)

### Sviluppi / Development Tracking
- Ogni sviluppo tracciato in `Sviluppi/YYYY-MM-DD_NN_NomeFunzionalita.md`
- Formato nome file: anno-mese-giorno_NumeroProgressivo_NomeFunzionalità
- Numero progressivo riparte da 01 ogni giorno
- Descrivere problema, soluzione, checkpoint

## Pull Request Process

1. Assicurati che tutti i test passino / Ensure all tests pass
2. Aggiorna la documentazione se necessario / Update documentation if needed
3. Aggiungi voci al CHANGELOG.md / Add entries to CHANGELOG.md
4. Richiedi review da un maintainer / Request review from a maintainer

## Segnalazione Bug / Bug Reports

Apri una issue descrivendo / Open an issue describing:
1. Comportamento atteso / Expected behavior
2. Comportamento attuale / Actual behavior
3. Passi per riprodurre / Steps to reproduce
4. Ambiente (OS, versione Java/Node) / Environment (OS, Java/Node version)
5. Log rilevanti / Relevant logs

## Feature Request

Per proporre nuove funzionalità / To propose new features:
1. Apri una issue con label `enhancement`
2. Descrivi il caso d'uso / Describe the use case
3. Spiega il valore aggiunto / Explain the added value
4. (Opzionale) Proponi un'implementazione / (Optional) Propose an implementation

## Domande / Questions

Per domande generali / For general questions:
- Apri una discussion su GitHub / Open a GitHub discussion
- Controlla prima la documentazione / Check the documentation first

## Codice di Condotta / Code of Conduct

Sii rispettoso e professionale in tutte le interazioni. / Be respectful and professional in all interactions.

## Licenza / License

Contribuendo, accetti che il tuo codice sia rilasciato sotto la licenza del progetto. / By contributing, you agree that your code will be released under the project's license.
