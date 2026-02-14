# Security Policy / Politica di Sicurezza

## Versioni Supportate / Supported Versions

| Versione / Version | Supportata / Supported |
| --- | --- |
| 0.1.x | :white_check_mark: |
| < 0.1 | :x: |

## Segnalazione Vulnerabilità / Reporting Vulnerabilities

### Italiano
Se scopri una vulnerabilità di sicurezza, ti preghiamo di segnalarla responsabilmente:

1. **Non** aprire una issue pubblica
2. Invia una email a: **security@localmind.local** (sostituire con indirizzo reale)
3. Includi una descrizione dettagliata della vulnerabilità
4. Se possibile, includi i passi per riprodurla
5. Indica la versione del software interessata

Ci impegniamo a:
- Rispondere entro **48 ore**
- Fornire un aggiornamento sullo stato entro **7 giorni**
- Rilasciare una patch entro **30 giorni** (per vulnerabilità critiche entro 7 giorni)
- Accreditare i ricercatori di sicurezza (se lo desiderano)

### English
If you discover a security vulnerability, please report it responsibly:

1. **Do not** open a public issue
2. Send an email to: **security@localmind.local** (replace with actual address)
3. Include a detailed description of the vulnerability
4. If possible, include steps to reproduce
5. Specify the affected software version

We commit to:
- Respond within **48 hours**
- Provide a status update within **7 days**
- Release a patch within **30 days** (for critical vulnerabilities within 7 days)
- Credit security researchers (if they wish)

## Misure di Sicurezza / Security Measures

### Autenticazione / Authentication
- Password locale con hash BCrypt (costo 12)
- Token bearer con scadenza 24 ore
- HMAC-SHA256 per firma token
- Rate limiting per prevenire brute force (100 richieste/minuto per IP)
- Nessuna trasmissione password in chiaro

### Rete / Network
- CORS configurato per origini specifiche (default: `http://localhost:4200`)
- CSRF disabilitato (API stateless con token bearer)
- Header di sicurezza (X-Correlation-Id per tracciamento)
- HTTPS raccomandato in produzione

### Dati / Data
- Credenziali API provider criptate nel database (AES-256)
- Nessun dato inviato a servizi esterni senza configurazione esplicita
- File system access limitato alle cartelle configurate in `folder_configs`
- Password mai loggata (nemmeno in debug)

### Dipendenze / Dependencies
- Aggiornamenti regolari delle dipendenze Maven e npm
- Scanning automatico con GitHub Dependabot (se abilitato)
- Uso di versioni LTS e stabili (Java 17, Spring Boot 3.4.x)

### Logging & Monitoring
- Correlation ID per tracciare richieste attraverso i layer
- Log strutturati (no dati sensibili)
- Actuator endpoints protetti (solo in localhost di default)

## Configurazione Sicura / Secure Configuration

### Variabili Sensibili
Le seguenti variabili in `.env` devono essere protette:
- `DB_PASSWORD` — Password database MySQL
- `OPENAI_API_KEY` — API key OpenAI
- `ANTHROPIC_API_KEY` — API key Anthropic
- `GOOGLE_API_KEY` — API key Google
- `LOCALMIND_SECURITY_JWT_SECRET` — Secret per firma token

**Non committare mai il file `.env` su Git.**

### Hardening Consigliato
1. **Cambio password di default**: Impostare una password forte al primo accesso
2. **Firewall**: Limitare accesso alle porte 8080 (backend), 6333 (Qdrant), 3306 (MySQL)
3. **Reverse proxy**: Usare Nginx/Apache con HTTPS in produzione
4. **Backup criptati**: Criptare backup di database e vector store
5. **Separazione rete**: Isolare servizi (MySQL, Qdrant) in rete privata

## Limiti Noti / Known Limitations

### Locale-Only
LocalMind è progettato per uso locale/self-hosted:
- Non implementa multi-tenancy
- Non ha protezione DDoS avanzata
- Rate limiting in-memory (non distribuito)

### Token JWT-like
- Token non è JWT standard (UUID + HMAC)
- Non supporta refresh token (ri-login dopo 24h)
- Stored in localStorage (vulnerabile a XSS, ma Angular sanitizza)

## Audit Log

Al momento LocalMind **non implementa** audit log completo. Considerare l'aggiunta in futuro per:
- Login/logout
- Cambio password
- Accesso a documenti sensibili
- Modifiche configurazioni

## Contatti / Contacts

Per questioni di sicurezza: **security@localmind.local**

Per supporto generale: aprire una issue su GitHub
