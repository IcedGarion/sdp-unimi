package ClientCasa.P2P.Boost;

import ClientCasa.LocalStatistics.smartMeter.SmartMeterSimulator;
import Shared.MessageSenderThread;
import ClientCasa.P2P.Message.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import Shared.Configuration;
import Shared.Http;

import javax.xml.bind.JAXBException;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// oggetto condiviso con dati riguardanti lo stato del power boost ("sto usando"/no; coda di chi ha richiesto....)
// condivso fra i vari thread che ricevono messaggi di boost
public class PowerBoost
{
	private static final Logger LOGGER = Logger.getLogger(PowerBoost.class.getName());
	private static final int BOOST_TIMEOUT_RETRY = 8000;
	public enum PowerBoostState { REQUESTED, USING, NOT_INTERESTED };

	// lock per non richiedere 2 boost allo stesso tempo (stessa casa)
	private Object boostLock;

	// flag per il thread timeout che richiede dopo un po': se sei riuscito a ottenere boost, il timeout si ferma
	private boolean boostObtained;

	private String casaId;
	private SmartMeterSimulator simulator;
	private PowerBoostState state;

	// coda di case che hanno richiesto boost mentre lo stavi usando
	private List<String[]> queue;
	// set di case messe in coda (per ricerca se c'è gia' uno in coda queue)
	private Set<String> queued;

	// numero case in gioco quando manda la prima richiesta boost
	private int caseAttive;
	// contatore degli OK
	private int OKCount;
	// mappa ok duplicati: segna chi mi ha mandato gia' l'ok a sto giro
	private Set<String> receivedOkIds;
	// timestamp del messaggio di richiesta appena inviato
	private long messageTimestamp;

	// thread timeout che continua a riprovare boost request
	private PowerBoostWaiterThread timeoutThread;

	public PowerBoost(SmartMeterSimulator simulator)
	{
		this.casaId = Configuration.CASA_ID;
		this.simulator = simulator;
		this.state = PowerBoostState.NOT_INTERESTED;
		this.caseAttive = 1;
		this.OKCount = 0;
		this.queue = new ArrayList<>();
		this.queued = new HashSet<>();
		this.messageTimestamp = -1;
		this.boostLock = new Object();
		this.boostObtained = false;
		this.receivedOkIds = new HashSet<>();

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public synchronized void setState(PowerBoostState state)
	{
		this.state = state;
	}

	public synchronized PowerBoostState getState()
	{
		return this.state;
	}

	public synchronized boolean getObtained()
	{
		return this.boostObtained;
	}

	// numero case quando manda la richiesta: serve poi per contare numero OK che ritornano (devono coincidere)
	public synchronized void setCaseAttive(int caseAttive) { this.caseAttive = caseAttive; }

	public synchronized int getCaseAttive() { return this.caseAttive; }

	// timestamp di quando invia la richiesta
	private synchronized void setMessageTimestamp(long timestamp) { this.messageTimestamp = timestamp; }

	public synchronized long getMessageTimestamp() { return this.messageTimestamp; }

	// aggiungi / rimuovi casa dalla coda delle richieste (null se vuota)
	public synchronized String[] deaccodaRichiesta()
	{
		// prende elemento in testa, rimuove e ritorna
		String[] ret;

		try
		{
			ret = queue.get(0);
			queue.remove(0);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}

		// rimuove anche dal set degli accodati la stessa casa
		queued.remove(ret[0]);

		return ret;
	}

	public synchronized void accodaRichiesta(String senderId, String senderIp, int senderPort)
	{
		// non puoi accodare piu' di una volta la stessa casa
		if(! queued.contains(senderId))
		{
			// aggiunge al set di case accodate
			queued.add(senderId);

			// inserisce in coda (una coda senderId, senderIp, senderPort) per poter poi mandare OK
			queue.add(new String[]{senderId, senderIp, String.valueOf(senderPort)});
		}
	}

	// aggiorna / check del numero di OK ricevuti dopo una richiesta di BOOST
	// fa anche check se ricevut
	public synchronized void incrOKCount()
	{
		this.OKCount = this.OKCount+1;
	}

	public synchronized int getOKCount() { return this.OKCount; }

	// a sto giro la casa 'senderId' mi ha gia' mandato il suo ok? se si, rispondo TRUE; se no, lo aggiungo e rispondo false
	public synchronized boolean duplicatedOk(String senderId)
	{
		if(receivedOkIds.contains(senderId))
		{
			return true;
		}
		else
		{
			receivedOkIds.add(senderId);
			return false;
		}
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// INIZIO BOOST
	// simile a Election.startElection(): avvisa tutti che serve boost; poi il thread rispondera' e gestira' lui
	// param: chiama il ciclo di retry oppure l'utente?
	public void requestPowerBoost(boolean timeoutThreadCaller)
	{
		Condominio condominio;
		MessageSenderThread boostMessageSender;

		try
		{
			// check semaforo in modo che non puoi richiedere un altro boost mentre questo e' gia' attivo, ma dovrai aspettare
			synchronized(boostLock)
			{
				// se ha gia' richiesto / sta gia' usando, deve aspettare ( solo per input tastiera; se sei il timeout thread puoi riprovare )
				if((! timeoutThreadCaller) && (getState().equals(PowerBoostState.USING) || getState().equals(PowerBoostState.REQUESTED)))
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Hai gia' richiesto il boost! Devi aspettare di terminare");

					try
					{
						boostLock.wait();
					}
					catch(InterruptedException e)
					{
						// era in wait per prossimo boost ma CasaApp ha deciso di uscire: niente
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Avevi richiesto un altro boost ma ora stai uscendo... ");
					}
				}
			}

			condominio = Http.getCondominio();

			// ORA PUO INIZIARE
			synchronized(this)
			{
				// setta il proprio stato interno: ha richiesto il boost
				this.state = PowerBoostState.REQUESTED;

				// setta obtained: ha richiesto boost ma non ancora ottenuto ( per thread timeout )
				this.boostObtained = false;

				// resetta il conto degli OK (se sta riprovando piu' volte e mi arriva ok dalla stessa casa, non deve sommarsi)
				this.OKCount = 0;

				// resetta gli id di chi mi ha gia' risposto OK
				this.receivedOkIds = new HashSet<>();

				// MANDA A TUTTI MSG BOOST REQUEST
				// chiede elenco case e si salva il numero, per contare poi gli OK
				this.caseAttive = condominio.size();

				// se c'e' solo una casa (o 2), sa gia' per certo di poter richiedere subito BOOST
				if(condominio.size() <= 2)
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Sono l'unica casa attiva (o siamo in 2) e quindi ottengo BOOST!");

					// chiama metodo per fare POWER BOOST
					beginPowerBoost();
				}
				else
				{
					// invia "BOOST" a tutti compreso se stesso, con anche timestamp
					this.messageTimestamp = new Date().getTime();

					for(Casa c : condominio.getCaselist())
					{
						boostMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getPort(), new P2PMessage(casaId, Configuration.CASA_PORT, "BOOST", getMessageTimestamp(), "BOOST"));
						boostMessageSender.start();
					}

					LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Inviato msg BOOST a tutte le " + getCaseAttive() + " case");
				}

				// alla fine della richiesta, fa partire un timer che richiedera' di nuovo il boost fra x secondi, in caso non ci sia ancora riuscito
				// se e' la prima volta che chiedo da tastiera fai partire timeoutThread, altrimenti NO (sta gia' girando)
				if(! timeoutThreadCaller)
				{
					timeoutThread = new PowerBoostWaiterThread(this, BOOST_TIMEOUT_RETRY);
					timeoutThread.start();
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred while requesting power boost");
			e.printStackTrace();
		}
	}

	// fa partire un thread che chiama il metodo del simulatore e, finito il tempo, chiama endPowerBoost
	// serve un thread separato, altrimenti questo rimarrebbe qua ad aspettare 5 sec boost, tenendo impegnato il lock
	public void beginPowerBoost() throws InterruptedException
	{
		synchronized(this)
		{
			// setta stato, cosi' se riceve altre richieste BOOST nel frattempo, le accodera'
			this.state = PowerBoost.PowerBoostState.USING;

			// azzera flag per il timeout thread ( deve smettere di provare perche' ce l'abbiamo fatta!)
			this.boostObtained = true;

			// interrompe anche il thread timeout, per sicurezza
			this.timeoutThread.interrupt();
		}

		// fa partire il thread che inizia e finisce il boost
		PowerBoostWaiterThread waiter = new PowerBoostWaiterThread(this, this.simulator);
		waiter.start();

		System.out.println("{ " + casaId + " } POWER BOOST iniziato");

		// informa il server amministratore inviando notifica PUSH
		Http.notifyBoost(casaId);
	}

	// finito il power boost, rilascia la risorsa (sync perche' modifica tanti campi ed e' meglio farlo in modo "atomico"
	public synchronized void endPowerBoost() throws JAXBException
	{
		String[] richiesta;
		String senderId, senderIp;
		int senderPort;
		MessageSenderThread boostMessageSender;


		// riazzera tutto per poter ricominciare: come da oggetto nuovo (costruttore)
		System.out.println("{ " + casaId + " } POWER BOOST terminato");

		// azzera timestamp utlima richiesta (-1)
		this.messageTimestamp = -1;

		// resetta il conto degli OK ricevuti (0)
		this.OKCount = 0;

		// resetta gli id chi ha gia' mandato ok
		this.receivedOkIds = new HashSet<>();

		// setta stato iniziale
		this.state = PowerBoostState.NOT_INTERESTED;

		// rilascia il semaforo in modo da poter richiedere di nuovo il boost (o svegliare se eri in attesa)
		synchronized(boostLock)
		{
			boostLock.notify();
		}

		// svuota coda di attesa, inviando OK a tutti i presenti
		while((richiesta = deaccodaRichiesta()) != null)
		{
			senderId = richiesta[0];
			senderIp = richiesta[1];
			senderPort = Integer.parseInt(richiesta[2]);

			// risponde OK
			boostMessageSender = new MessageSenderThread(casaId, senderIp, senderPort, new P2PMessage(casaId, Configuration.CASA_PORT, "OK", "BOOST"));
			boostMessageSender.start();

			LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Fine BOOST: mando OK a " + senderId);
		}
	}

	public synchronized void deleteQueueWithOK() throws JAXBException
	{
		String[] richiesta;
		String senderId, senderIp;
		int senderPort;
		MessageSenderThread boostMessageSender;

		// svuota coda di attesa, inviando OK a tutti i presenti
		while((richiesta = deaccodaRichiesta()) != null)
		{
			senderId = richiesta[0];
			senderIp = richiesta[1];
			senderPort = Integer.parseInt(richiesta[2]);

			// risponde OK
			boostMessageSender = new MessageSenderThread(casaId, senderIp, senderPort, new P2PMessage(casaId, Configuration.CASA_PORT, "OK", "BOOST"));
			boostMessageSender.start();

			LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Fine BOOST: mando OK a " + senderId);
		}
	}
}
