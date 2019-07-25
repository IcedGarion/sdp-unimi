# TODO now
- Sposta p2pthread in casaApp
  - interfaccia EXIT: check se sei coordinatore (anche per questo devi spostare tutti in casaApp)
    e invii a tutti i electionThread un MSG "NEED_REELEcTION"

- check uscita casa / cosa provoca su elezione

- server concorrente per electionThread



# TODO
- StatsReceiverServerThread: 
  una volta calcolato il consumo globale, qualcuno deve mandarlo al server.
  indice elezione la prima volta; se sei l'eletto invii tu; se non sei l'eletto non fai niente.
  Se e' la prima volta (chissa' come capirlo?) devi indire elezione...
  Tutto nel StatsReceiver di seguito? o lancia altro thread elezione? in oni caso p2pthread e' inutile

- 3 rami da checkare

- Election: fare elezione BULLY

- SERVER CONCORRENTE ANCHE PER QUESTO? (ElectionListener)
  STESSSA ROBA DI STATSRECEIVER: SERVE OGGETTO CONDIVISO...
   SERVER CONCORRENTE SIA PER ELECTION (quindi un electionThread da lanciare ogni volta + oggetto condiviso)
   SERVER CONCORRENTE ANCHE PER ELECTIONLISTENER (idem come sopra: un electionListenerThread + oggetto condiviso)
- TESTARE ANCHE CASO IN CUI C'E' UNA SOLA CASA: COORD LEI SUBITO!



( vecchia idea da guardare per election: 
  -- [un altro thread, P2Pcoord], che invece si coordina con gli altri P2Pcoord della rete: indice "elezione" o meccanismo simile
     per decidere chi invia le statistiche al serverAmministratore. STATE?
     Poi, una volta deciso coordinatore, lui invia la statistica al server.
     STATE perche' ci sara' credo un loop in cui periodicamente, a seconda dello stato in cui si e', si eseguono diverse azioni:
     <elezione> sai che devi indire elezione
     <coord> sai che sei il coord e devi mandare info
     <stato_particolare_in_mezzo_a_elezione> sai cosa devi fare.
)


- crea spazio nel server rest per poter aggiungere stat globali


# DOMANDE FATTE / COSE DA SISTEMARE POI
- pool di thread opzionale, non serve per forza
- bully algo va bene per elezione, tanto uscite sono controllate:
  nel caso di uscita, avvisa il server e tutte le case (che si scaricano di nuovo la lista)
  
  MA POI VA RI-INDETTA ELEZIONE! 
  DEVONO CAPIRE CHE SI E' IN "NEED_ELECTION" QUANDO UNO ESCE.... TIMEOUT SE NON RISPONDE NESSUN COORD? VEDI BULLY  
	DA IMPLEMENTARE IN UN RAMO DI STATSRECEIVERSERVERTHREAD: se non sei coord (ma neanche in need_election, perche' c'e' gia' stata),
	dovresti comunque cercare di pingare il coord per ssapere se indirne una nuova


  ( QUANDO CASA ESCE, CHE SUCCEDE A STATS SENDER/ RECEIVER? CONTINUA A FUNZIONARE OPPURE SI BLOCCA IN ATTESA DELL'ULTIMA CASA
  ( CHE NON MANDERA' MAI LA SUA STATISTICA?? -> ANDREBBE TOLTA LA CASA ANCHE DALL'OGGETTO CONDIVISO (HASHMAP)
  ( ANDREBBE RI-AGGIORNATO IL CONDOMINIO E PASSATO AL STATSRECEIVER (seno' crede che ci sia ancora una casa in piu)
   -> dovrebbe essere ok questa parte, perche' si scarica condominio gia' di suo ogni volta che manda / riceve stats

DOPPIA (O TRIPLA) PORTA PER OGNI CASA!!!
QUANDO SI REGISTRA COMUNICA PIU' DI UNA PORTA
UNA PER PARLARSI CON LE ALTRE CASE SULLE STATISTICHE
UNA PER ELEZIONE
UNA PER POWER BOOST

- invece per power boost va usato algoritmo mutua esclusione distribuita (ricart&agrawala) o ring
- state ok per casa



# COSE DA TENERE SEMPRE A MENTE
- Quando mandi una richiesta al server, se non leggi la risposta (url-rest)
(conn.getResponseMessage / Code) e' come se non l'avessi inviata... va ignorata boh
- Quando sei in debug, alcune richieste non arrivano a chi ascolta.
- marshalling con socket deve chiudere la socket... altrimenti unmarshaller si blocca senza dire niente
- Comunicazioni in broadcast non devono mai essere sequenziali! lancia thread che invia, ogni volta


# REFACTOR
- Aggiungi LOG ovunque (Service e Apps)
- Togli eventuali System.out.println()
- SYNCHRONIZED da aggiungere in posti (es. in services)
  Metodi sync invece che sync statement nei services? anche nelle letture?
- FILE CONFIG (tipo SERVER_URL in giro ovunque)
- TOGLI / CONTROLLA i TODO e FIXME
- suppress log per esame






**CORRENTE EXTRA**
TOKEN RING
con wait e notify va gestita una coda di attesa:
tanti richiedono BOOST ma solo 2 alla volta possono usarlo.
Quando uno finisce, rilascia lock e entra il prossimo
(Syncro per chiamare localmente un metodo)


=================================================================================================

**DOMANDE**


**ESAME**
- schema architettura da spiegare, con anche casi limite
- esecuzione per vedere se va tutto
- guarda codice, parti sync


==================================================================================================
**DOCUMENTAZIONE**

*APPUNTI / SCELTE*
- POST create casa non ritorna niente, anche se su progetto dice
  che dovrebbe tornare l'elenco delle case (come GET), perche'
  il metodo Response.created() accetta solo URI e non un oggetto,
  quindi non si puo' ritornare il condominio.... a meno di cambiare
  response code della POST da created() a ok();

- Codice simulatore e' stato modificato aggiungendo dichiarazione di package


*CLASSI*

**ServerAmministratore**
Fa partire il server REST, e lancia il thread di invio statistiche.


**CondominioService**
Gestisce inserimento / cancellazione / GET di tutte le info sulle case.
Assegnato al PATH /condominio

- GET /condominio: restituisce l'elenco delle case (200 OK)
  getCaseList(): non e' synchronized perche', a un livello piu' sotto, viene chiamato
  Condominio.getInstance() ed e' gia' synchronized.

- POST /condominio/add: permette di aggiungere una nuova casa al condominio
  409 conflict se esiste gia'; 201 created se ok

  addCasa(Casa c): ha un synchronized statement che fa check se esiste gia' la stessa casa
  nel condiminio + aggiunge (se non presente)
  A livelli piu' bassi accede a Condominio.getInstance() e Condominio.getByName() -> Condominio.getCaseList() (synchronized)

- DELETE /condominio/delete: rimuove una casa dal condominio
  404 not found se non esiste; 204 no content se ok
  simile a POST.



**CasaApp**
Fa partire SmartMeterSimulator; si registra al server amministratore (a condominio); chiede elenco case da salvarsi;
gestisce rete p2p; ha interfaccia per power boost e per uscire da Condominio.

- SmartMeterSimulator periodicamente chiama SimulatorBuffer.addMeasurement().
  Questo metodo si salva in un buffer interno le misurazioni; inoltre dialoga col server REST per inviare queste
  statistiche.


**STATISTICHE**
MeanThread lanciato da CasaApp calcola periodicamente la media di 24 misurazioni, (cancella le prime 12)
e invia a StatisticheLocali la media calcolata, con timestamp minore e maggiore fra i 24 considerati.

- MeanThread manda statistica locale a StatisticheService.
  Se e' la prima volta (StatisticheLocali non ha in memoria id casa), crea mapping vuoto fra id casa e lista MeanMeasurement.
  Altrimenti aggiunge in coda alla lista (MAP) MeanMeasurement la nuova Measurement ricevuta.

  Cosi' si ha:
  <idCasa>
    <MeanMeasurement> ... </>
    <MeanMeasurement> ... </>
  </idCasa>
 
  <idCasa>
  ... ecc


**StatisticheLocali**
Come Condominio, e' il contenitore (singleton) per accedere alle statistiche locali delle varie case.
Contiene una lista di CasaMeasurement: ogni elemento di questo oggetto ha un ID casa e poi una lista
di MeanMeasurement, cioe' una lista di medie calcolate. (Calcolo fatto da CasaApp - MeanThread)

(Vedi variante HashMap)


**Scambio Statistiche fra case (Statistiche Globali)
- P2PThread, StatsReceiverThread e MeanThread:
  MeanT manda a tutte le case la sua statistica locale calcolata.
  StatsReceiver ascolta e riceve queste statistiche da tutte le case: aspetta finche' non le riceve da TUTTO il condominio:
    - StatsRceiverThread riceve e basta; poi salva la statistica in un oggetto condiviso con StatsReceiverServer
    - StatsReceiverServer tira le somme: controlla questo oggetto dopo ogni richiesta ricevuta e si assicura che ci siano
         stat da tutte le case... Poi, quando succede, stampa il consumo globale e "azzera" l'oggetto condiviso, per ricominciare
    - E se non arrivano tutte le case per bene ma, mentre aspetti l'ultima, arriva di nuovo stat di qualcun altra???
      -> la ignora e aspetta il ritardatario, che tanto prima o poi arriva




======================================================================================================================================
**APPUNTI**


(segue le slide lab5)

PARTE1

- un thread per casa:
	simulare dati che escono

- client amministratore

- server e' un server REST (jersey?):
	bisogna risolvere tutti i problemi di syncro


(c'e' una classe statistiche e una classe condominio lato server)

PARTE2

- Rete p2p fra le case:
	architettura e protocolli
	pensare a tutti i casi limite che possono succedere
	(fault tolerance, es: una casa esce durante elezione)

	CASI LIMITE VANNO PENSATI TUTTI E GESTITI; VENGONO VALUTATi

PASSI
- prova aggiunte / rimozione di case dalla rete
- analisi sensori e comunicazione server
- algoritmo di mutua esclusione


....


GESTIONE CASE

Si vuole aggiungere / togliere case
(casa e' IP + PORT. "esiste gia" = esiste gia un IP+PORT)
Server AMMINISTRATORE gestisce (metodi syncronized non funzionano sempre, usa syncro statement SEMPRE)
Va protetta la sincronizzazione sia in lettura che in scrittura


GESTIONE STATISTICHE
"ha senso bloccare ogni operazione (ad esempio
aggiunta/rimozione di case) mentre vengono calcolate le
statistiche?"
  -> NO. Se usi una unica classe in cui metti strutture dati di case e di statistiche,
  e poi fai tutti i syncronized methods, hai un solo lock e quindi una operazione blocca tutto
  Quindi separa in 2 classi oppure usa i sync statement.

Stessa cosa per:
"Ha senso bloccare l’intera struttura dati con tutte le statistiche
anche se vogliamo analizzare le statistiche di una specifica
casa?" 
  -> NO. Locka solo certe cose, cerca di tenere i lock al minimo.
  Es, se leggi statistiche di una casa non serve bloccarle tutte


JERSEY REMINDER
non mettere come synchronized sui metodi annotati tipo @PATH.
Usa SINGLETON su tutte le risorse che sono condivise. Es, strutture dati,
statistiche globali e locali sono condivise e quindi singleton.
(serve perche' jersey istanzia tante volte cose ???)


