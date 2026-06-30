export const content = `# Guida Utente - Cartelle Monitorate

## Accesso

Navigare a **Cartelle** dal sidebar oppure visitare \`http://localhost:4200/folders\`.

## Panoramica

La sezione Cartelle Monitorate permette di configurare delle cartelle locali sul filesystem che verranno scansionate automaticamente per individuare nuovi documenti da indicizzare. Il sistema controlla le cartelle configurate ogni 15 minuti e indicizza automaticamente i nuovi file trovati.

## Aggiungere una cartella

1. Cliccare il pulsante **+ Aggiungi Cartella** nell'intestazione
2. Compilare il form che appare:

### Campi del form

| Campo                     | Obbligatorio | Descrizione                                    | Esempio                                        |
|---------------------------|--------------|------------------------------------------------|------------------------------------------------|
| **Percorso cartella**     | Si           | Percorso assoluto della cartella da monitorare | \`/home/user/documenti\`                         | 
| **Pattern file**          | No           | Pattern glob per filtrare i tipi di file       | \`*.pdf,*.md,*.txt\`                             |
| **Includi sottocartelle** | No           | Checkbox per la scansione ricorsiva            | Selezionato = scansiona anche le sottocartelle |

3. Cliccare **Aggiungi** per salvare la configurazione

> **Nota**: il pulsante Aggiungi e' disabilitato finche' il campo percorso non viene compilato.

## Lista cartelle configurate

Ogni cartella configurata viene mostrata come una card contenente:

### Informazioni visualizzate

- **Icona cartella** e percorso completo (font monospace)
- **Badge stato** con codifica colore:
  - **ACTIVE** (verde): cartella attiva e monitorata regolarmente
  - **SYNCING** (blu): sincronizzazione in corso
  - **ERROR** (rosso): errore nella scansione (es: percorso non valido)
  - **PAUSED** (grigio): monitoraggio in pausa
- **Numero documenti** trovati nella cartella
- **Pattern** applicato (se configurato, in font monospace)
- **Modalita' scansione**: "Ricorsivo" oppure "Solo root"
- **Ultima sincronizzazione**: data e ora dell'ultimo scan completato

### Azioni disponibili

| Azione          | Icona             | Descrizione                                                                    |
|-----------------|-------------------|--------------------------------------------------------------------------------|
| **Sincronizza** | Freccia circolare | Avvia una scansione immediata della cartella (disabilitato durante il syncing) |
| **Elimina**     | Cestino (rosso)   | Rimuove la configurazione della cartella                                       |

## Funzionamento automatico

Le cartelle configurate vengono scansionate automaticamente dal backend ogni **15 minuti** (configurabile tramite \`localmind.batch.cron-folder-scan\` nel file di configurazione). I nuovi file che corrispondono ai pattern configurati vengono:

1. Rilevati durante la scansione
2. Aggiunti alla coda di elaborazione
3. Elaborati (estrazione testo, chunking, embedding)
4. Indicizzati nel vector store per la ricerca semantica

## Stati vuoti

- **Caricamento**: spinner con testo "Caricamento..."
- **Nessuna cartella**: card con messaggio "Nessuna cartella configurata" e suggerimento di aggiungerne una
`;
