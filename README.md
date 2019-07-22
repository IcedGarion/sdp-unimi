# TODO vero
- P2PThread, StatsReceiverThread e MeanThread:
  MeanT manda a tutte le case la sua statistica locale calcolata. (SOCKET + JAXB!!)
  StatsReceiver ascolta e riceve queste statistiche da tutte le case: aspetta finche' non le riceve da TUTTO il condominio
      inventa cosa fare se ne manca qualcuna.

ocio che devi inviare statisiche solo quando tutte le case te la mandano; (mapping casa_x - ha mandato a sto turno)
inoltre la stessa non deve contribuire piu volte.... COSA FARE DI QUELLE CHE NON ARRIVANO? LE BUTTI?
 
  quando ci sono tutte le statistiche, stampa il consumo globale


CHECK RICEZIONE STATISTICHE (StatsReceiverThread):
	RICEVE SOLO UNA VOLTA
SERVER DEVE ESSERE CONCORRENTEEEE (StatsReceiverThread)



  - poi avanti con p2pthread e elezione! (se non eletto termini electionThread, atrimenti l'eletto manda sempre statistica media globale)


Comunicazioni in broadcast non devono mai essere sequenziali! lancia thread che invia, ogni volta


# DOMANDE FATTE
- pool di thread opzionale, non serve per forza
- bully algo va bene per elezione, tanto uscite sono controllate:
  nel caso di uscita, avvisa il server e tutte le case (che si scaricano di nuovo la lista)
  
  MA POI VA RI-INDETTA ELEZIONE!


DOPPIA (O TRIPLA) PORTA PER OGNI CASA!!!
QUANDO SI REGISTRA COMUNICA PIU' DI UNA PORTA
UNA PER PARLARSI CON LE ALTRE CASE SULLE STATISTICHE
UNA PER ELEZIONE
UNA PER POWER BOOST




- invece per power boost va usato algoritmo mutua esclusione distribuita (ricart&agrawala) o ring
- state ok per casa



(
**TODO**
- CasaApp rete p2p
  -- lancia thread p2p
  -- Questo ha un altro thread, "StatisticheGlobali" che ascolta sempre: riceve le misurazioni dalle altre case
     e le mette in un buffer CONDIVISO (condiviso solo fra i thread della stessa casa, non "globale")
  -- Questo thread server e' come i server visti concorrenti: pool di thread e lancia thread per ogni connessione in arrivo
  POI
  -- MeanThread manda le sue misurazioni locali a tutte le case della rete, e il thread di cui sopra riceve
     queste misurazioni da tutto il resto della rete e le mette nel buffer.
     Come succedeva per le statistiche locali, c'e' un thread aggiuntivo (Tipo MeanThread) che invece continua a svuotare
     buffer e calcolare la media globale (+ stampa).
  -- un altro thread, P2Pcoord, che invece si coordina con gli altri P2Pcoord della rete: indice "elezione" o meccanismo simile
     per decidere chi invia le statistiche al serverAmministratore. STATE?
     Poi, una volta deciso coordinatore, lui invia la statistica al server.
     STATE perche' ci sara' credo un loop in cui periodicamente, a seconda dello stato in cui si e', si eseguono diverse azioni:
     <elezione> sai che devi indire elezione
     <coord> sai che sei il coord e devi mandare info
     <stato_particolare_in_mezzo_a_elezione> sai cosa devi fare.

Ogni casa stampa a schermo il consumo complessivo del condominio.
)



[
- ADMIN APP
  : in 1-4 va anche calcolato Min e Max timestamp? controlla testo progetto
  : taglia i timestamp, stampa solo le ore (no data)
]



**REFACTOR**
- Aggiungi LOG ovunque (Service e Apps)
- Togli eventuali System.out.println()
- SYNCHRONIZED da aggiungere in posti (es. in services)
  Metodi sync invece che sync statement nei services? anche nelle letture?

- FILE CONFIG (tipo SERVER_URL in giro ovunque)



** NBBBBBB **
Quando mandi una richiesta al server, se non leggi la risposta
(conn.getResponseMessage / Code) e' come se non l'avessi inviata... va ignorata boh
Quando sei in debug, alcune richieste non arrivano a chi ascolta.






**STATISTICHE**
Vanno separate locali e globali: classi diverse o comunque lock diversi!
per inserire locali non devi bloccare locali e viceversa.
poi XML non contiene tutto insieme (condominio+globali + case+locali)
ma separa: condominio resta com'era, tutto a posto.
Statistiche xml ha: globale + <id casa + lista locali>, <id casa + lista locali>....
cosi' mantieni scollegate le 2 cose.


*GLOBALI*
diverse soluzioni: (va evitata ridondanza)
- chiedi lock distribuito e chi lo ottiene manda lui le statistiche 
- elezione coordinatore (e se esce? nuova elezione)




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


