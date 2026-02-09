# Guida Utente - Documenti

## Accesso

Navigare a **Documenti** dal sidebar oppure visitare `http://localhost:4200/documents`.

## Panoramica

La sezione Documenti permette di caricare, visualizzare e gestire i file che vengono indicizzati per la ricerca semantica (RAG). I documenti caricati vengono elaborati, suddivisi in chunk e memorizzati nel vector store per le ricerche successive.

## Caricamento documenti

### Come caricare

1. Cliccare il pulsante **Carica Documento** nell'intestazione della pagina
2. Si apre il selettore file del sistema operativo
3. Selezionare uno o piu' file

### Formati supportati

| Formato | Estensioni |
|---------|------------|
| PDF | .pdf |
| Documenti Word | .doc, .docx |
| Testo semplice | .txt |
| Markdown | .md |
| CSV | .csv |
| JSON | .json |

### Progresso upload

Durante il caricamento viene mostrata una barra di progresso con:
- Icona spinner animata
- Nome del file in fase di caricamento
- Il messaggio "Caricamento in corso..."

### Notifiche

Al completamento dell'upload:
- **Successo**: notifica verde "Documento caricato con successo"
- **Errore**: notifica rossa con il messaggio di errore

## Filtri per stato

Sotto l'intestazione sono presenti dei tab per filtrare i documenti per stato:

| Filtro | Descrizione | Colore badge |
|--------|-------------|-------------|
| **Tutti** | Mostra tutti i documenti | - |
| **Indicizzati** | Documenti elaborati e pronti per la ricerca | Verde |
| **In attesa** | Documenti caricati in attesa di elaborazione | Giallo |
| **In elaborazione** | Documenti attualmente in fase di processing | Blu |
| **Falliti** | Documenti la cui elaborazione e' fallita | Rosso |

Ogni tab mostra il conteggio dei documenti in quello stato. Il tab attivo e' evidenziato.

## Lista documenti

Ogni documento viene mostrato come una card contenente:

- **Icona file** con simbolo documento
- **Nome file** in grassetto
- **Badge stato** colorato (Indicizzato, In attesa, Elaborazione, Fallito, Archiviato)
- **Dimensione file** in formato leggibile (B, KB, MB)
- **Tipo MIME** del file (es: application/pdf)
- **Data di caricamento** con ora

### Eliminazione documento

Ogni card ha un pulsante **cestino** (icona rossa) per eliminare il documento. Cliccandolo:

1. Appare una finestra di conferma con il nome del documento
2. Il messaggio chiede: "Sei sicuro di voler eliminare [nome file]? Questa azione non puo' essere annullata."
3. Due pulsanti disponibili:
   - **Annulla**: chiude la finestra senza eliminare
   - **Elimina**: procede con l'eliminazione definitiva

## Aggiornamento lista

Il pulsante **Aggiorna** (icona freccia circolare) nell'intestazione ricarica la lista dei documenti dal backend.

## Stati vuoti

- **Caricamento**: spinner con testo "Caricamento documenti..."
- **Nessun documento**: card centrale con icona, titolo "Nessun documento" e messaggio "Carica il primo documento per iniziare."
- **Nessun risultato nel filtro**: messaggio "Nessun documento con stato [stato selezionato]"
