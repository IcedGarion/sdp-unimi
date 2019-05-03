*CLASSI*

**CaseService**
Gestisce inserimento / cancellazione / GET di tutte le info sulle case.
Assegnato al PATH /case

- GET /case: restituisce l'elenco delle case
  getCaseList(): non e' synchronized perche', a un livello piu' sotto, viene chiamato
  Condominio.getInstance() ed e' gia' synchronized.

- POST /case/add: permette di aggiungere una nuova casa al condominio
  addCasa(Casa c): ha un synchronized statement che fa check se esiste gia' la stessa casa
  nel condiminio + aggiunge (se non presente)
  A livelli piu' bassi accede a Condominio.getInstance() e Condominio.getByName() -> Condominio.getCaseList() (synchronized)






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


