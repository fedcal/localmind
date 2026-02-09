import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-guide-page',
  imports: [],
  template: `
    <div class="guide-page">
      <div class="page-header">
        <div>
          <h1>Guida Utente</h1>
          <p class="subtitle">Documentazione completa su come utilizzare LocalMind</p>
        </div>
      </div>

      <!-- Indice di navigazione -->
      <div class="toc card">
        <h3>Indice</h3>
        <div class="toc-grid">
          <a class="toc-item" (click)="scrollTo('start')">
            <span class="toc-icon">&#x1F680;</span>
            <span>Primi Passi</span>
          </a>
          <a class="toc-item" (click)="scrollTo('dashboard')">
            <span class="toc-icon">&#x1F4CA;</span>
            <span>Dashboard</span>
          </a>
          <a class="toc-item" (click)="scrollTo('chat')">
            <span class="toc-icon">&#x1F4AC;</span>
            <span>Chat</span>
          </a>
          <a class="toc-item" (click)="scrollTo('documents')">
            <span class="toc-icon">&#x1F4C4;</span>
            <span>Documenti</span>
          </a>
          <a class="toc-item" (click)="scrollTo('search')">
            <span class="toc-icon">&#x1F50D;</span>
            <span>Ricerca Semantica</span>
          </a>
          <a class="toc-item" (click)="scrollTo('folders')">
            <span class="toc-icon">&#x1F4C1;</span>
            <span>Cartelle Monitorate</span>
          </a>
          <a class="toc-item" (click)="scrollTo('providers')">
            <span class="toc-icon">&#x2699;&#xFE0F;</span>
            <span>Impostazioni Provider</span>
          </a>
          <a class="toc-item" (click)="scrollTo('mcp')">
            <span class="toc-icon">&#x1F527;</span>
            <span>MCP Integration</span>
          </a>
        </div>
      </div>

      <!-- ============================================================ -->
      <!-- SEZIONE 1: PRIMI PASSI -->
      <!-- ============================================================ -->
      <section id="start" class="guide-section">
        <div class="section-header" (click)="toggle('start')">
          <div class="section-title">
            <span class="section-icon">&#x1F680;</span>
            <h2>Primi Passi</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'start' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'start') {
          <div class="section-body">
            <p class="section-intro">
              LocalMind e' un sistema di gestione della conoscenza locale con supporto AI.
              Permette di caricare documenti, indicizzarli semanticamente e interrogarli
              tramite chat con modelli LLM locali o cloud. Segui questi passaggi per iniziare.
            </p>

            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Configura un provider LLM</strong>
                  <p>
                    Vai in <strong>Impostazioni</strong> dal menu laterale e clicca <em>+ Aggiungi Provider</em>.
                    Per iniziare rapidamente in locale, seleziona il tipo <strong>Ollama</strong>: il sistema
                    rileva automaticamente i modelli scaricati sulla tua macchina e li mostra in un menu a tendina.
                    Per provider cloud (OpenAI, Anthropic, Google Gemini) dovrai inserire la tua API Key.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Carica i tuoi documenti</strong>
                  <p>
                    Vai nella sezione <strong>Documenti</strong> e carica i file che vuoi rendere ricercabili.
                    Sono supportati i formati PDF, DOCX, DOC, TXT, MD, CSV e JSON. I documenti verranno
                    elaborati automaticamente: il testo viene estratto, suddiviso in chunk e trasformato in
                    embedding vettoriali per la ricerca semantica.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Inizia a chattare</strong>
                  <p>
                    Apri la <strong>Chat</strong> dal menu. Seleziona il provider e il modello dai menu a tendina
                    in alto, poi scrivi la tua domanda. L'AI puo' rispondere attingendo anche ai documenti
                    caricati (modalita' RAG - Retrieval Augmented Generation).
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <div>
                  <strong>Cerca nei documenti</strong>
                  <p>
                    Usa la <strong>Ricerca Semantica</strong> per trovare informazioni specifiche nei documenti
                    indicizzati. Puoi fare domande in linguaggio naturale: il sistema capisce il significato
                    della tua richiesta, non solo le parole esatte.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">5</span>
                <div>
                  <strong>Automatizza con le cartelle monitorate</strong>
                  <p>
                    Configura delle <strong>Cartelle Monitorate</strong> per far indicizzare automaticamente i nuovi
                    file che salvi in una directory specifica. Il sistema controlla le cartelle ogni 15 minuti.
                  </p>
                </div>
              </div>
            </div>

            <div class="info-box">
              <strong>Navigazione</strong>: usa il menu laterale (sidebar) per spostarti tra le sezioni.
              La sidebar puo' essere compressa cliccando la freccia in alto. Tutte le sezioni sono
              accessibili anche quando la sidebar e' compressa, grazie alle icone.
            </div>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 2: DASHBOARD -->
      <!-- ============================================================ -->
      <section id="dashboard" class="guide-section">
        <div class="section-header" (click)="toggle('dashboard')">
          <div class="section-title">
            <span class="section-icon">&#x1F4CA;</span>
            <h2>Dashboard</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'dashboard' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'dashboard') {
          <div class="section-body">
            <p class="section-intro">
              La Dashboard e' la pagina di riepilogo del sistema. Fornisce una panoramica immediata
              dello stato di LocalMind e scorciatoie per le operazioni piu' comuni.
            </p>

            <h3>Stato del sistema</h3>
            <p>
              In alto viene mostrato lo stato dell'API backend:
            </p>
            <table class="info-table">
              <thead>
                <tr><th>Indicatore</th><th>Significato</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td><span class="inline-badge bg-green">UP</span></td>
                  <td>Il backend e' raggiungibile e funzionante. Tutte le operazioni sono disponibili.</td>
                </tr>
                <tr>
                  <td><span class="inline-badge bg-red">DOWN</span></td>
                  <td>Il backend non risponde. Verifica che il server sia in esecuzione sulla porta 8080.</td>
                </tr>
              </tbody>
            </table>

            <h3>Contatori</h3>
            <p>La dashboard mostra tre contatori principali:</p>
            <ul class="detail-list">
              <li><strong>Documenti</strong>: numero totale di documenti caricati nel sistema (in qualsiasi stato)</li>
              <li><strong>Server MCP</strong>: numero di server MCP registrati nella configurazione</li>
              <li><strong>Provider LLM</strong>: numero di provider configurati e attivi</li>
            </ul>

            <h3>Azioni rapide</h3>
            <p>Quattro pulsanti per accedere rapidamente alle funzionalita' principali:</p>
            <ul class="detail-list">
              <li><strong>Chat</strong>: apre direttamente l'interfaccia di conversazione</li>
              <li><strong>Upload Documenti</strong>: vai alla sezione documenti per caricare nuovi file</li>
              <li><strong>Ricerca RAG</strong>: apre la ricerca semantica nei documenti indicizzati</li>
              <li><strong>Gestione MCP</strong>: apre la sezione di gestione dei server MCP</li>
            </ul>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 3: CHAT -->
      <!-- ============================================================ -->
      <section id="chat" class="guide-section">
        <div class="section-header" (click)="toggle('chat')">
          <div class="section-title">
            <span class="section-icon">&#x1F4AC;</span>
            <h2>Chat</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'chat' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'chat') {
          <div class="section-body">
            <p class="section-intro">
              L'interfaccia Chat e' il cuore di LocalMind. Permette di conversare con i modelli AI
              configurati, con la possibilita' di attingere automaticamente ai documenti indicizzati
              per risposte contestualizzate (RAG).
            </p>

            <h3>Selezionare provider e modello</h3>
            <p>
              Prima di inviare un messaggio, seleziona il <strong>provider</strong> e il <strong>modello</strong>
              dai due menu a tendina nella barra superiore della chat. I provider disponibili sono quelli
              configurati nella sezione Impostazioni. Per ogni provider vengono mostrati i modelli disponibili.
            </p>

            <h3>Inviare un messaggio</h3>
            <table class="info-table">
              <thead>
                <tr><th>Azione</th><th>Come fare</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>Inviare il messaggio</td>
                  <td>Premi <kbd>Invio</kbd> oppure clicca il pulsante di invio</td>
                </tr>
                <tr>
                  <td>Andare a capo senza inviare</td>
                  <td>Premi <kbd>Shift</kbd> + <kbd>Invio</kbd></td>
                </tr>
                <tr>
                  <td>Nuova conversazione</td>
                  <td>Clicca il pulsante <em>Nuova Chat</em> in alto</td>
                </tr>
              </tbody>
            </table>

            <h3>Suggerimenti rapidi</h3>
            <p>
              Quando la chat e' vuota (nessun messaggio), vengono mostrati dei <strong>suggerimenti</strong>
              cliccabili. Cliccando su un suggerimento, il testo viene inserito automaticamente nella
              casella di input e inviato. Questo e' utile per iniziare rapidamente senza dover digitare
              una domanda da zero.
            </p>

            <h3>Cronologia conversazioni</h3>
            <p>
              Le conversazioni vengono salvate automaticamente. Nella sidebar della chat puoi
              vedere la lista delle conversazioni precedenti e riaprirle cliccandoci sopra.
              Ogni conversazione mantiene l'intero storico dei messaggi scambiati.
            </p>

            <h3>Modalita' RAG</h3>
            <p>
              Quando invii un messaggio, il sistema cerca automaticamente nei documenti indicizzati
              i passaggi piu' pertinenti alla tua domanda. Questi passaggi vengono inclusi nel contesto
              inviato al modello AI, permettendogli di fornire risposte basate sui tuoi documenti.
              Questa modalita' e' chiamata <strong>Retrieval Augmented Generation (RAG)</strong>.
            </p>

            <div class="info-box">
              <strong>Suggerimento</strong>: per ottenere risposte migliori con il RAG, formula
              domande specifiche e pertinenti ai documenti che hai caricato. Domande generiche
              riceveranno risposte generiche dal modello, senza riferimenti ai tuoi dati.
            </div>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 4: DOCUMENTI -->
      <!-- ============================================================ -->
      <section id="documents" class="guide-section">
        <div class="section-header" (click)="toggle('documents')">
          <div class="section-title">
            <span class="section-icon">&#x1F4C4;</span>
            <h2>Documenti</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'documents' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'documents') {
          <div class="section-body">
            <p class="section-intro">
              La sezione Documenti permette di caricare, visualizzare e gestire i file che alimentano
              la base di conoscenza di LocalMind. I documenti caricati vengono elaborati e resi
              disponibili per la ricerca semantica e il RAG nella chat.
            </p>

            <h3>Formati supportati</h3>
            <table class="info-table">
              <thead>
                <tr><th>Formato</th><th>Estensioni</th><th>Note</th></tr>
              </thead>
              <tbody>
                <tr><td>PDF</td><td><code>.pdf</code></td><td>Documenti con testo estraibile (non immagini scansionate)</td></tr>
                <tr><td>Word</td><td><code>.docx</code>, <code>.doc</code></td><td>Documenti Microsoft Word</td></tr>
                <tr><td>Testo</td><td><code>.txt</code></td><td>File di testo semplice</td></tr>
                <tr><td>Markdown</td><td><code>.md</code></td><td>File Markdown</td></tr>
                <tr><td>CSV</td><td><code>.csv</code></td><td>Dati tabulari separati da virgola</td></tr>
                <tr><td>JSON</td><td><code>.json</code></td><td>Dati strutturati in formato JSON</td></tr>
              </tbody>
            </table>

            <h3>Caricare un documento</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Clicca "Upload Documento"</strong>
                  <p>Il pulsante si trova nell'intestazione della pagina Documenti.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Seleziona il file</strong>
                  <p>Scegli un file dal tuo filesystem. Solo i formati supportati sono accettati.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Attendi l'elaborazione</strong>
                  <p>
                    Il documento passa attraverso diverse fasi: estrazione testo, suddivisione in chunk,
                    generazione degli embedding vettoriali e indicizzazione nel vector store.
                  </p>
                </div>
              </div>
            </div>

            <h3>Stati del documento</h3>
            <table class="info-table">
              <thead>
                <tr><th>Stato</th><th>Badge</th><th>Descrizione</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>In attesa</td>
                  <td><span class="inline-badge bg-yellow">PENDING</span></td>
                  <td>Il documento e' stato caricato ma non ancora elaborato. Sara' processato a breve.</td>
                </tr>
                <tr>
                  <td>Elaborazione</td>
                  <td><span class="inline-badge bg-blue">PROCESSING</span></td>
                  <td>Il documento e' in fase di elaborazione: estrazione testo, chunking, embedding.</td>
                </tr>
                <tr>
                  <td>Indicizzato</td>
                  <td><span class="inline-badge bg-green">INDEXED</span></td>
                  <td>Elaborazione completata. Il documento e' pronto per la ricerca semantica e il RAG.</td>
                </tr>
                <tr>
                  <td>Fallito</td>
                  <td><span class="inline-badge bg-red">FAILED</span></td>
                  <td>Errore durante l'elaborazione. Verifica che il file sia valido e non corrotto.</td>
                </tr>
              </tbody>
            </table>

            <h3>Filtri per stato</h3>
            <p>
              Sopra la lista dei documenti ci sono dei <strong>tab</strong> che permettono di filtrare
              per stato: <em>Tutti</em>, <em>In attesa</em>, <em>Elaborazione</em>, <em>Indicizzati</em>,
              <em>Falliti</em>. Clicca su un tab per vedere solo i documenti in quello stato.
            </p>

            <h3>Informazioni sulla card del documento</h3>
            <p>Ogni documento mostra:</p>
            <ul class="detail-list">
              <li><strong>Nome file</strong> e formato (icona)</li>
              <li><strong>Dimensione</strong> del file</li>
              <li><strong>Data di caricamento</strong></li>
              <li><strong>Badge stato</strong> con codifica colore</li>
              <li><strong>Numero di chunk</strong> generati (se indicizzato)</li>
            </ul>

            <h3>Eliminare un documento</h3>
            <p>
              Clicca l'icona <strong>cestino</strong> (rossa) sulla card del documento. Apparira'
              una finestra di conferma. Confermando, il documento e i relativi embedding verranno
              rimossi permanentemente dal sistema.
            </p>

            <div class="info-box">
              <strong>Attenzione</strong>: l'eliminazione di un documento rimuove anche tutti i suoi
              embedding dal vector store. I risultati di ricerca relativi a quel documento non saranno
              piu' disponibili.
            </div>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 5: RICERCA SEMANTICA -->
      <!-- ============================================================ -->
      <section id="search" class="guide-section">
        <div class="section-header" (click)="toggle('search')">
          <div class="section-title">
            <span class="section-icon">&#x1F50D;</span>
            <h2>Ricerca Semantica</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'search' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'search') {
          <div class="section-body">
            <p class="section-intro">
              La Ricerca Semantica permette di cercare informazioni nei documenti indicizzati
              utilizzando il linguaggio naturale. A differenza di una ricerca testuale tradizionale,
              il sistema comprende il <em>significato</em> della tua domanda e trova i passaggi
              piu' pertinenti, anche se usano parole diverse.
            </p>

            <h3>Come funziona</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Scrivi la tua domanda</strong>
                  <p>
                    Inserisci una domanda o una frase nel campo di ricerca. Usa il linguaggio naturale,
                    come faresti con un collega. Esempio: "come si configura l'autenticazione a due fattori?"
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Configura il parametro Top K</strong>
                  <p>
                    Seleziona quanti risultati vuoi ottenere dal menu a tendina. Valori disponibili:
                    <strong>3</strong>, <strong>5</strong>, <strong>10</strong>, <strong>20</strong>.
                    Un valore piu' basso restituisce meno risultati ma piu' pertinenti.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Avvia la ricerca</strong>
                  <p>Clicca il pulsante <em>Cerca</em> o premi <kbd>Invio</kbd>.</p>
                </div>
              </div>
            </div>

            <h3>Leggere i risultati</h3>
            <p>Ogni risultato mostra:</p>
            <ul class="detail-list">
              <li><strong>Documento sorgente</strong>: il nome del file da cui proviene il passaggio</li>
              <li><strong>Punteggio di rilevanza</strong>: una barra percentuale che indica quanto il passaggio e' pertinente alla domanda. Piu' alta = piu' pertinente.</li>
              <li><strong>Estratto del testo</strong>: il passaggio del documento corrispondente alla tua ricerca</li>
            </ul>

            <h3>Consigli per ricerche efficaci</h3>
            <ul class="detail-list">
              <li>Usa frasi complete invece di singole parole chiave</li>
              <li>Sii specifico nella domanda: "come configurare SMTP per l'invio email" funziona meglio di "email"</li>
              <li>Se non trovi risultati, prova a riformulare la domanda con sinonimi</li>
              <li>Inizia con Top K = 5 e aumenta se necessario</li>
            </ul>

            <div class="info-box">
              <strong>Prerequisito</strong>: la ricerca funziona solo sui documenti con stato
              <span class="inline-badge bg-green">INDEXED</span>. Assicurati di aver caricato e
              completato l'indicizzazione di almeno un documento.
            </div>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 6: CARTELLE MONITORATE -->
      <!-- ============================================================ -->
      <section id="folders" class="guide-section">
        <div class="section-header" (click)="toggle('folders')">
          <div class="section-title">
            <span class="section-icon">&#x1F4C1;</span>
            <h2>Cartelle Monitorate</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'folders' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'folders') {
          <div class="section-body">
            <p class="section-intro">
              Le Cartelle Monitorate permettono di configurare directory locali sul filesystem
              che verranno scansionate automaticamente per individuare e indicizzare nuovi documenti.
              Questo e' utile per automatizzare il caricamento senza intervento manuale.
            </p>

            <h3>Aggiungere una cartella</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Clicca "+ Aggiungi Cartella"</strong>
                  <p>Il pulsante si trova nell'intestazione della pagina Cartelle.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Compila il form</strong>
                  <p>Inserisci il percorso assoluto della cartella da monitorare.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Configura i filtri (opzionale)</strong>
                  <p>
                    Specifica un <strong>pattern file</strong> per filtrare i tipi di file da indicizzare
                    (es: <code>*.pdf,*.md,*.txt</code>). Lascia vuoto per tutti i formati supportati.
                    Attiva <strong>"Includi sottocartelle"</strong> per la scansione ricorsiva.
                  </p>
                </div>
              </div>
            </div>

            <h3>Campi del form</h3>
            <table class="info-table">
              <thead>
                <tr><th>Campo</th><th>Obbligatorio</th><th>Descrizione</th><th>Esempio</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>Percorso cartella</td><td>Si</td>
                  <td>Percorso assoluto della directory</td>
                  <td><code>/home/user/documenti</code></td>
                </tr>
                <tr>
                  <td>Pattern file</td><td>No</td>
                  <td>Glob pattern per filtrare i file</td>
                  <td><code>*.pdf,*.md,*.txt</code></td>
                </tr>
                <tr>
                  <td>Includi sottocartelle</td><td>No</td>
                  <td>Scansiona ricorsivamente le subdirectory</td>
                  <td>Checkbox</td>
                </tr>
              </tbody>
            </table>

            <h3>Stati della cartella</h3>
            <table class="info-table">
              <thead>
                <tr><th>Stato</th><th>Badge</th><th>Descrizione</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>Attiva</td>
                  <td><span class="inline-badge bg-green">ACTIVE</span></td>
                  <td>La cartella e' monitorata regolarmente e funzionante.</td>
                </tr>
                <tr>
                  <td>Sincronizzazione</td>
                  <td><span class="inline-badge bg-blue">SYNCING</span></td>
                  <td>Scansione in corso. Verranno indicizzati i nuovi file trovati.</td>
                </tr>
                <tr>
                  <td>Errore</td>
                  <td><span class="inline-badge bg-red">ERROR</span></td>
                  <td>Errore nella scansione. Verifica che il percorso sia valido e accessibile.</td>
                </tr>
                <tr>
                  <td>In pausa</td>
                  <td><span class="inline-badge bg-neutral">PAUSED</span></td>
                  <td>Il monitoraggio e' temporaneamente sospeso.</td>
                </tr>
              </tbody>
            </table>

            <h3>Informazioni sulla card</h3>
            <p>Ogni cartella configurata mostra:</p>
            <ul class="detail-list">
              <li><strong>Percorso</strong> completo della cartella (font monospace)</li>
              <li><strong>Badge stato</strong> con codifica colore</li>
              <li><strong>Numero documenti</strong> trovati nella cartella</li>
              <li><strong>Pattern</strong> applicato (se configurato)</li>
              <li><strong>Modalita'</strong>: "Ricorsivo" oppure "Solo root"</li>
              <li><strong>Ultima sincronizzazione</strong>: data e ora dell'ultimo scan</li>
            </ul>

            <h3>Azioni</h3>
            <table class="info-table">
              <thead>
                <tr><th>Azione</th><th>Icona</th><th>Descrizione</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>Sincronizza</td><td>Freccia circolare</td>
                  <td>Avvia una scansione immediata della cartella (disabilitato se gia' in corso)</td>
                </tr>
                <tr>
                  <td>Elimina</td><td>Cestino (rosso)</td>
                  <td>Rimuove la configurazione della cartella dal sistema</td>
                </tr>
              </tbody>
            </table>

            <div class="info-box">
              <strong>Scansione automatica</strong>: le cartelle vengono controllate dal backend ogni
              <strong>15 minuti</strong>. I nuovi file che corrispondono ai pattern configurati vengono
              rilevati, aggiunti alla coda di elaborazione, processati (estrazione testo, chunking, embedding)
              e indicizzati nel vector store.
            </div>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 7: IMPOSTAZIONI PROVIDER LLM -->
      <!-- ============================================================ -->
      <section id="providers" class="guide-section">
        <div class="section-header" (click)="toggle('providers')">
          <div class="section-title">
            <span class="section-icon">&#x2699;&#xFE0F;</span>
            <h2>Impostazioni Provider LLM</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'providers' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'providers') {
          <div class="section-body">
            <p class="section-intro">
              La sezione Impostazioni permette di configurare i provider LLM utilizzati dal sistema
              per la chat e le operazioni AI. E' possibile aggiungere piu' provider, ciascuno con
              le proprie credenziali e il modello predefinito.
            </p>

            <h3>Provider supportati</h3>
            <table class="info-table">
              <thead>
                <tr><th>Provider</th><th>Tipo</th><th>Richiede API Key</th><th>URL Base Default</th><th>Modelli esempio</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong>Ollama</strong></td><td>Locale</td><td>No</td>
                  <td><code>http://localhost:11434</code></td>
                  <td>llama3.2, mistral, codellama, phi3</td>
                </tr>
                <tr>
                  <td><strong>OpenAI</strong></td><td>Cloud</td><td>Si (<code>sk-...</code>)</td>
                  <td><code>https://api.openai.com</code></td>
                  <td>gpt-4o, gpt-4o-mini, gpt-3.5-turbo</td>
                </tr>
                <tr>
                  <td><strong>Anthropic</strong></td><td>Cloud</td><td>Si (<code>sk-ant-...</code>)</td>
                  <td><code>https://api.anthropic.com</code></td>
                  <td>claude-sonnet-4-20250514, claude-haiku</td>
                </tr>
                <tr>
                  <td><strong>Google Gemini</strong></td><td>Cloud</td><td>Si (<code>AIza...</code>)</td>
                  <td><code>https://generativelanguage.googleapis.com</code></td>
                  <td>gemini-2.0-flash, gemini-pro</td>
                </tr>
              </tbody>
            </table>

            <h3>Aggiungere un provider</h3>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Clicca "+ Aggiungi Provider"</strong>
                  <p>Appare il form "Nuovo Provider LLM".</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Compila Nome e Tipo</strong>
                  <p>
                    Inserisci un nome descrittivo (es: "Ollama Locale", "OpenAI Produzione") e seleziona
                    il tipo dal menu a tendina. L'URL base e i placeholder si aggiornano automaticamente.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Inserisci API Key (provider cloud)</strong>
                  <p>
                    Per OpenAI, Anthropic e Google il campo API Key e' obbligatorio. La chiave viene
                    mascherata (campo password) e <strong>salvata nel database</strong> in modo persistente:
                    non sara' necessario reinserirla al riavvio del sistema.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <div>
                  <strong>Seleziona il modello predefinito</strong>
                  <p>
                    Per <strong>Ollama</strong>: il campo mostra un menu a tendina popolato automaticamente
                    con i modelli scaricati sull'istanza. Usa il pulsante di aggiornamento per ricaricare
                    la lista. Per i <strong>provider cloud</strong>: digita il nome del modello nel campo testo.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">5</span>
                <div>
                  <strong>Salva</strong>
                  <p>
                    Clicca "Salva Provider". Il pulsante e' attivo solo quando tutti i campi obbligatori
                    sono compilati.
                  </p>
                </div>
              </div>
            </div>

            <h3>Card del provider</h3>
            <p>Ogni provider salvato mostra:</p>
            <ul class="detail-list">
              <li><strong>Icona tipo</strong>: cerchio colorato con l'iniziale (verde Ollama, verde acqua OpenAI, marrone Anthropic, blu Google)</li>
              <li><strong>Nome</strong> e <strong>URL base</strong> (font monospace)</li>
              <li><strong>Badge stato</strong>: "Attivo" (verde) o "Disattivato" (grigio)</li>
              <li><strong>Tipo</strong>: badge con il nome del provider</li>
              <li><strong>Modello default</strong> (se configurato)</li>
              <li><strong>Lista modelli</strong>: tag con i modelli disponibili (per Ollama)</li>
            </ul>

            <h3>Test connessione</h3>
            <p>
              Il pulsante <strong>Test Connessione</strong> verifica che il provider sia raggiungibile.
              Per Ollama mostra il numero di modelli disponibili. Per i provider cloud verifica che
              l'API key sia configurata. L'esito viene comunicato tramite notifica in alto nella pagina.
            </p>

            <h3>Rimuovere un provider</h3>
            <p>
              Clicca il pulsante rosso <strong>Rimuovi</strong> sulla card del provider. Il provider
              e tutte le sue configurazioni (inclusa l'API key) verranno eliminati dal database.
            </p>

            <div class="info-box">
              <strong>Notifiche</strong>: tutte le operazioni (aggiunta, rimozione, test) generano
              notifiche colorate in alto nella pagina che scompaiono automaticamente dopo 4 secondi.
              Verde = successo, Rosso = errore.
            </div>
          </div>
        }
      </section>

      <!-- ============================================================ -->
      <!-- SEZIONE 8: MCP -->
      <!-- ============================================================ -->
      <section id="mcp" class="guide-section">
        <div class="section-header" (click)="toggle('mcp')">
          <div class="section-title">
            <span class="section-icon">&#x1F527;</span>
            <h2>MCP (Model Context Protocol)</h2>
          </div>
          <span class="section-arrow">{{ openSection() === 'mcp' ? '&#9650;' : '&#9660;' }}</span>
        </div>
        @if (openSection() === 'mcp') {
          <div class="section-body">
            <p class="section-intro">
              La sezione MCP permette di registrare server MCP esterni e di esplorare
              ed eseguire i tool che mettono a disposizione. MCP (Model Context Protocol) e' un
              protocollo standard per l'integrazione di tool e risorse con modelli AI.
              L'interfaccia e' organizzata in due tab: <strong>Server Esterni</strong> e
              <strong>Tool Disponibili</strong>.
            </p>

            <h3>Tab: Server Esterni</h3>

            <h4>Registrare un server</h4>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Clicca "+ Aggiungi Server"</strong>
                  <p>Appare il form di registrazione.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Compila i campi</strong>
                  <p>Inserisci nome, descrizione (opzionale) e seleziona il tipo di connessione.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Configura la connessione</strong>
                  <p>In base al tipo selezionato, compila i campi specifici (vedi tabella sotto).</p>
                </div>
              </div>
            </div>

            <h4>Campi comuni</h4>
            <table class="info-table">
              <thead>
                <tr><th>Campo</th><th>Obbligatorio</th><th>Descrizione</th></tr>
              </thead>
              <tbody>
                <tr><td>Nome</td><td>Si</td><td>Nome identificativo del server</td></tr>
                <tr><td>Descrizione</td><td>No</td><td>Descrizione delle funzionalita'</td></tr>
                <tr><td>Tipo</td><td>Si</td><td>STDIO oppure SSE</td></tr>
                <tr><td>Timeout</td><td>No</td><td>Timeout in secondi (5-300, default 30)</td></tr>
                <tr><td>Auto-reconnect</td><td>No</td><td>Riconnessione automatica (default: attivo)</td></tr>
              </tbody>
            </table>

            <h4>Campi specifici per tipo</h4>
            <table class="info-table">
              <thead>
                <tr><th>Tipo</th><th>Campo</th><th>Descrizione</th><th>Esempio</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td rowspan="2"><strong>STDIO</strong></td>
                  <td>Comando</td><td>Comando da eseguire</td>
                  <td><code>npx</code></td>
                </tr>
                <tr>
                  <td>Argomenti</td><td>Argomenti separati da virgola</td>
                  <td><code>-y,&#64;modelcontextprotocol/server-filesystem,./</code></td>
                </tr>
                <tr>
                  <td><strong>SSE</strong></td>
                  <td>URL</td><td>URL dell'endpoint SSE del server</td>
                  <td><code>http://localhost:8082/sse</code></td>
                </tr>
              </tbody>
            </table>

            <h4>Stati del server</h4>
            <table class="info-table">
              <thead>
                <tr><th>Stato</th><th>Badge</th><th>Descrizione</th></tr>
              </thead>
              <tbody>
                <tr>
                  <td>Connesso</td>
                  <td><span class="inline-badge bg-green">CONNECTED</span></td>
                  <td>Il server e' connesso e funzionante</td>
                </tr>
                <tr>
                  <td>Disconnesso</td>
                  <td><span class="inline-badge bg-neutral">DISCONNECTED</span></td>
                  <td>Il server non e' connesso</td>
                </tr>
                <tr>
                  <td>Errore</td>
                  <td><span class="inline-badge bg-red">ERROR</span></td>
                  <td>Errore di connessione</td>
                </tr>
                <tr>
                  <td>Connessione in corso</td>
                  <td><span class="inline-badge bg-orange">CONNECTING</span></td>
                  <td>Tentativo di connessione in corso</td>
                </tr>
              </tbody>
            </table>

            <h4>Azioni sui server</h4>
            <table class="info-table">
              <thead>
                <tr><th>Azione</th><th>Descrizione</th></tr>
              </thead>
              <tbody>
                <tr><td><strong>Test</strong></td><td>Verifica la connettivita'. Mostra spinner durante il test e notifica con l'esito.</td></tr>
                <tr><td><strong>Riconnetti</strong></td><td>Tenta la riconnessione al server. Mostra spinner durante il tentativo.</td></tr>
                <tr><td><strong>Elimina</strong></td><td>Rimuove il server dalla configurazione (pulsante rosso).</td></tr>
              </tbody>
            </table>

            <h3>Tab: Tool Disponibili</h3>

            <h4>Tool Locali</h4>
            <p>
              I tool locali sono quelli esposti direttamente dal backend di LocalMind. Ogni tool e'
              presentato come card con nome (font monospace), descrizione e badge <span class="inline-badge bg-blue">Locale</span>.
            </p>

            <h4>Tool Esterni</h4>
            <p>
              I tool esterni provengono dai server MCP connessi. Ogni tool mostra nome, descrizione,
              badge <span class="inline-badge bg-purple">Esterno</span> e il Server ID abbreviato.
              Se non ci sono tool esterni, viene suggerito di connettere un server MCP.
            </p>

            <h4>Eseguire un tool</h4>
            <div class="steps">
              <div class="step">
                <span class="step-num">1</span>
                <div>
                  <strong>Seleziona il tool</strong>
                  <p>Clicca sulla card del tool che vuoi eseguire. Si evidenzia con bordo e ombra.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">2</span>
                <div>
                  <strong>Consulta lo schema di input</strong>
                  <p>
                    Nella sezione che appare in basso vengono mostrati nome, descrizione e lo
                    <strong>schema di input</strong> in formato JSON. Questo schema descrive la struttura
                    degli argomenti richiesti dal tool.
                  </p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">3</span>
                <div>
                  <strong>Compila gli argomenti</strong>
                  <p>Scrivi gli argomenti nel campo di testo in formato JSON.</p>
                </div>
              </div>
              <div class="step">
                <span class="step-num">4</span>
                <div>
                  <strong>Clicca "Esegui"</strong>
                  <p>Il tool viene eseguito e i risultati mostrati nella card.</p>
                </div>
              </div>
            </div>

            <h4>Risultati dell'esecuzione</h4>
            <p>Dopo l'esecuzione vengono mostrati:</p>
            <ul class="detail-list">
              <li><strong>Badge stato</strong>: verde "success" o rosso "error"</li>
              <li><strong>Tempo di esecuzione</strong> in millisecondi</li>
              <li><strong>Messaggio di errore</strong> (solo in caso di fallimento)</li>
              <li><strong>Dati risultato</strong>: output del tool in formato JSON (sfondo scuro, font monospace)</li>
            </ul>

            <div class="info-box">
              <strong>Aggiorna</strong>: il pulsante "Aggiorna" nell'intestazione del tab ricarica
              la lista di tutti i tool disponibili (locali ed esterni), utile dopo aver aggiunto
              o rimosso un server MCP.
            </div>
          </div>
        }
      </section>

    </div>
  `,
  styles: [`
    .guide-page { max-width: 900px; }

    .page-header { margin-bottom: 1.5rem; }
    .page-header h1 { margin: 0; font-size: 1.5rem; }
    .subtitle { color: #888; font-size: 0.85rem; margin: 0.15rem 0 0 0; }

    /* Indice */
    .card { background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); padding: 1.25rem; }
    .toc { margin-bottom: 1.5rem; }
    .toc h3 { margin: 0 0 0.75rem 0; font-size: 0.95rem; color: #333; }
    .toc-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 0.5rem;
    }
    .toc-item {
      display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 0.75rem;
      border-radius: 8px; background: #f8f9fb; cursor: pointer; font-size: 0.82rem;
      font-weight: 500; color: #333; text-decoration: none; border: 1px solid #e8ecf1;
      transition: all 0.15s;
    }
    .toc-item:hover { background: #eef1f6; border-color: #cdd3dc; }
    .toc-icon { font-size: 1rem; }

    /* Sezioni */
    .guide-section {
      margin-bottom: 0.5rem;
      background: white; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      overflow: hidden;
    }
    .section-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 1rem 1.25rem; cursor: pointer; user-select: none;
    }
    .section-header:hover { background: #fafbfc; }
    .section-title { display: flex; align-items: center; gap: 0.6rem; }
    .section-icon { font-size: 1.2rem; }
    .section-title h2 { margin: 0; font-size: 1.05rem; color: #0f3460; }
    .section-arrow { font-size: 0.7rem; color: #888; }

    .section-body { padding: 0 1.25rem 1.5rem 1.25rem; }
    .section-intro { font-size: 0.85rem; color: #555; line-height: 1.6; margin: 0 0 1.25rem 0; }

    .section-body h3 {
      font-size: 0.92rem; color: #0f3460; margin: 1.5rem 0 0.5rem 0;
      padding-bottom: 0.25rem; border-bottom: 1px solid #e8ecf1;
    }
    .section-body h3:first-child { margin-top: 0; }
    .section-body h4 {
      font-size: 0.85rem; color: #444; margin: 1rem 0 0.4rem 0;
    }
    .section-body p { font-size: 0.83rem; color: #555; line-height: 1.55; margin: 0 0 0.5rem 0; }

    /* Steps */
    .steps { display: flex; flex-direction: column; gap: 0.6rem; margin: 0.5rem 0; }
    .step {
      display: flex; gap: 0.75rem; align-items: flex-start;
      padding: 0.65rem 0.85rem; background: #f8fafe; border-radius: 6px;
      border-left: 3px solid #0f3460;
    }
    .step-num {
      display: flex; align-items: center; justify-content: center;
      width: 24px; height: 24px; border-radius: 50%; flex-shrink: 0;
      background: #0f3460; color: white; font-size: 0.72rem; font-weight: 700;
    }
    .step strong { font-size: 0.83rem; display: block; margin-bottom: 0.15rem; color: #222; }
    .step p { font-size: 0.8rem; color: #555; margin: 0; line-height: 1.5; }

    /* Tabelle */
    .info-table {
      width: 100%; border-collapse: collapse; font-size: 0.8rem; margin: 0.5rem 0;
    }
    .info-table th {
      text-align: left; padding: 0.45rem 0.6rem;
      background: #f0f2f5; color: #444; font-weight: 600; border-bottom: 2px solid #ddd;
    }
    .info-table td {
      padding: 0.45rem 0.6rem; border-bottom: 1px solid #eee; color: #555;
      vertical-align: top;
    }
    .info-table tr:last-child td { border-bottom: none; }
    .info-table code {
      background: #eef1f6; padding: 0.1rem 0.35rem; border-radius: 3px; font-size: 0.75rem;
    }

    /* Liste */
    .detail-list {
      margin: 0.4rem 0 0.6rem 1.25rem; padding: 0; font-size: 0.82rem; color: #555;
    }
    .detail-list li { margin-bottom: 0.3rem; line-height: 1.5; }
    .detail-list li strong { color: #333; }

    /* Info box */
    .info-box {
      padding: 0.75rem 1rem; background: #e8f4fd; border-radius: 6px;
      border-left: 3px solid #3498db; font-size: 0.82rem; color: #2c3e50;
      line-height: 1.5; margin-top: 0.75rem;
    }
    .info-box strong { color: #0f3460; }

    /* Badges inline */
    .inline-badge {
      display: inline-block; padding: 0.08rem 0.4rem; border-radius: 3px;
      font-size: 0.68rem; font-weight: 600; vertical-align: middle;
    }
    .bg-green { background: #d4edda; color: #155724; }
    .bg-yellow { background: #fff3cd; color: #856404; }
    .bg-blue { background: #cce5ff; color: #004085; }
    .bg-red { background: #f8d7da; color: #721c24; }
    .bg-neutral { background: #e2e3e5; color: #383d41; }
    .bg-orange { background: #ffe0b2; color: #e65100; }
    .bg-purple { background: #e8daef; color: #6c3483; }

    kbd {
      display: inline-block; padding: 0.05rem 0.35rem; font-size: 0.72rem;
      border: 1px solid #ccc; border-radius: 3px; background: #f7f7f7;
      font-family: inherit; box-shadow: 0 1px 0 #bbb;
    }
    code {
      background: #eef1f6; padding: 0.1rem 0.35rem; border-radius: 3px; font-size: 0.75rem;
    }
  `]
})
export class GuidePageComponent {
  openSection = signal<string | null>('start');

  toggle(section: string) {
    this.openSection.set(this.openSection() === section ? null : section);
  }

  scrollTo(id: string) {
    this.openSection.set(id);
    setTimeout(() => {
      document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 50);
  }
}
