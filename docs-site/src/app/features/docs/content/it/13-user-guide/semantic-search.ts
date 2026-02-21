export const content = `# Guida Utente - Ricerca Semantica

## Accesso

Navigare a **Ricerca** dal sidebar oppure visitare \`http://localhost:4200/search\`.

## Panoramica

La Ricerca Semantica permette di cercare informazioni nei documenti indicizzati utilizzando il linguaggio naturale. A differenza di una ricerca testuale classica, questa funzione comprende il **significato** della domanda e restituisce i passaggi piu' rilevanti anche se non contengono le parole esatte cercate.

> **Prerequisito**: per ottenere risultati, e' necessario aver caricato e indicizzato almeno un documento nella sezione Documenti.

## Come effettuare una ricerca

### Barra di ricerca

1. Digitare la domanda nel campo di ricerca (placeholder: "Es: Come funziona il sistema di autenticazione?")
2. Opzionalmente, modificare il parametro **Top K** dal menu a tendina accanto
3. Cliccare il pulsante **Cerca** oppure premere Invio

### Parametro Top K

Il valore **Top K** determina quanti risultati vengono restituiti. Le opzioni disponibili sono:
- **3** - Restituisce i 3 passaggi piu' rilevanti
- **5** - Valore predefinito, buon bilanciamento
- **10** - Ricerca piu' ampia
- **20** - Massimo numero di risultati

Un valore piu' alto restituisce piu' risultati ma potenzialmente meno pertinenti.

### Suggerimenti

Quando non e' stata ancora effettuata una ricerca, vengono mostrati tre suggerimenti cliccabili:
- "Riassumi il contenuto dei documenti"
- "Quali sono i concetti principali?"
- "Trova informazioni su configurazione"

Cliccando un suggerimento, la ricerca parte automaticamente con quel testo.

## Risultati

### Intestazione risultati

Dopo una ricerca viene mostrato il numero di risultati trovati e la query utilizzata (es: "3 risultati per: sistema di autenticazione").

### Card risultato

Ogni risultato viene presentato come una card contenente:

#### Barra di punteggio
- Una barra orizzontale colorata che rappresenta visivamente la rilevanza del risultato
- Il punteggio percentuale e' mostrato a destra (es: "87.5%")
- Piu' alto e' il punteggio, piu' il passaggio e' pertinente alla domanda

#### Nome documento
- Icona file seguita dal nome del documento di provenienza
- Permette di identificare da quale file proviene il passaggio

#### Contenuto
- Anteprima del testo del passaggio (troncato a 4 righe)
- Mostra il chunk di documento che corrisponde alla ricerca

## Stato di caricamento

Durante l'esecuzione della ricerca viene mostrato uno spinner con il testo "Ricerca in corso...".

## Nessun risultato

Se la ricerca non produce risultati, viene mostrato il messaggio "Nessun risultato trovato. Prova a riformulare la ricerca."
`;
