**TODO**

- StatisticheService accetta statistiche da Casa (simile a CondominioService=
  e le aggiunge a struttura Statistiche (forse serve anche classe Measurement in beans?)

- SimulatorBuffer.addMeasurement() fa chiamate REST a StatisticheService
  per aggiungere queste statistiche

- Measure andrebbe new Beans: da definire con le annotations tutti i suoi campi
  per poi fargli fare lo stesso lavoro XML jaxb tipo Casa

  Statistiche = Condominio.
  Measure = Casa.



**DOMANDE**

- ServerAmministratore (REST) e GestioneStatistiche sono 2 separati?  
  main in cui viene lanciato rest + lanciato thread invio statistiche?

- POST create casa non ritorna niente, anche se su progetto dice
  che dovrebbe tornare l'elenco delle case (come GET), perche'
  il metodo Response.created() accetta solo URI e non un oggetto,
  quindi non si puo' ritornare il condominio.... a meno di cambiare
  response code della POST da created() a ok();

- Buffer simulatore va gestito con sync??? In realta' ogni thread ha il suo... (quindi no?)

- Questione Statistiche, media e sliding window...


[ - Codice simulatore e' stato modificato aggiungendo dichiarazione di package. ]



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


