package ClientCasa.P2P.Boost;

import ClientCasa.LocalStatistics.smartMeter.SmartMeterSimulator;
import Shared.MessageSenderThread;
import ClientCasa.P2P.Message.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import Shared.Configuration;
import Shared.Http;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// oggetto condiviso con dati riguardanti lo stato del power boost ("sto usando"/no; coda di chi ha richiesto....)
// Condiviso fra PowerBoostThread e PowerBoostWorker
public class PowerBoost
{
	private static final Logger LOGGER = Logger.getLogger(PowerBoost.class.getName());
	public enum PowerBoostState { REQUESTED, USING, NOT_INTERESTED };

	// lock per non richiedere 2 boost allo stesso tempo (stessa casa)
	private Object boostLock;

	private String casaId;
	private SmartMeterSimulator simulator;
	private PowerBoostState state;

	// coda di case che hanno richiesto boost mentre lo stavi usando
	private List<String[]> queue;
	// numero case in gioco quando manda la prima richiesta boost
	private int caseAttive;
	// contatore degli OK
	private int OKCount;
	// timestamp del messaggio di richiesta appena inviato
	private long messageTimestamp;

	public PowerBoost(SmartMeterSimulator simulator)
	{
		this.casaId = Configuration.CASA_ID;
		this.simulator = simulator;
		this.state = PowerBoostState.NOT_INTERESTED;
		this.caseAttive = 1;
		this.OKCount = 0;
		this.queue = new ArrayList<>();
		this.messageTimestamp = -1;
		this.boostLock = new Object();

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

	// numero case quando manda la richiesta: serve poi per contare numero OK che ritornano (devono coincidere)
	public synchronized void setCaseAttive(int caseAttive) { this.caseAttive = caseAttive; }

	public synchronized int getCaseAttive() { return this.caseAttive; }

	// timestamp di quando invia la richiesta
	private synchronized void setMessageTimestamp(long timestamp) { this.messageTimestamp = timestamp; }

	public synchronized long getMessageTimestamp() { return this.messageTimestamp; }

	// aggiungi / rimuovi casa dalla coda delle richieste (null se vuota)
	private synchronized String[] deaccodaRichiesta()
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
		return ret;
	}

	public synchronized void accodaRichiesta(String senderId, String senderIp, int senderPort)
	{
		// inserisce in coda (una coda senderId, senderIp, senderPort) per poter poi mandare OK
		queue.add(new String[] {senderId, senderIp, String.valueOf(senderPort)});
	}

	// aggiorna / check del numero di OK ricevuti dopo una richiesta di BOOST
	public synchronized void incrOKCount() { this.OKCount = this.OKCount+1; }

	public synchronized int getOKCount() { return this.OKCount; }

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// INIZIO BOOST
	// simile a Election.startElection(): avvisa tutti che serve boost; poi il thread rispondera' e gestira' lui
	public void requestPowerBoost()
	{
		Condominio condominio;
		MessageSenderThread boostMessageSender;
		PowerBoostState currentState;

		try
		{
			// check semaforo in modo che non puoi richiedere un altro boost mentre questo e' gia' attivo, ma dovrai aspettare
			synchronized(boostLock)
			{
				// se ha gia' richiesto / sta gia' usando, deve aspettare
				if(getState().equals(PowerBoostState.USING) || getState().equals(PowerBoostState.REQUESTED))
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Hai gia' richiesto il boost! Devi aspettare di terminare");

					boostLock.wait();
				}
			}

			// ORA PUO INIZIARE
			// setta il proprio stato interno: ha richiesto il boost
			setState(PowerBoostState.REQUESTED);

			// MANDA A TUTTI MSG BOOST REQUEST
			// chiede elenco case e si salva il numero, per contare poi gli OK
			condominio = Http.getCondominio();
			setCaseAttive(condominio.size());

			// invia "BOOST" a tutti compreso se stesso, con anche timestamp
			setMessageTimestamp(new Date().getTime());

			// se c'e' solo una casa (o 2), sa gia' per certo di poter richiedere subito BOOST
			if(condominio.size() <= 2)
			{
				LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Sono l'unica casa attiva e quindi ottengo BOOST!");

				// chiama metodo per fare POWER BOOST
				beginPowerBoost();
			}
			else
			{
				for(Casa c : condominio.getCaselist())
				{
					boostMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getPort(), new P2PMessage(casaId, Configuration.CASA_PORT, "BOOST", getMessageTimestamp(), "BOOST"));
					boostMessageSender.start();
				}

				LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Inviato msg BOOST a tutte le " + getCaseAttive() + " case");
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
	public synchronized void beginPowerBoost() throws InterruptedException
	{
		// setta stato, cosi' se riceve altre richieste BOOST nel frattempo, le accodera'
		this.state = PowerBoost.PowerBoostState.USING;

		System.out.println("{ " + casaId + " } POWER BOOST iniziato");

		// informa il server amministratore inviando notifica PUSH
		Http.notifyBoost(casaId);

		// fa partire il thread che inizia e finisce il boost
		PowerBoostWaiterThread waiter = new PowerBoostWaiterThread(this, this.simulator);
		waiter.start();
	}

	// finito il power boost, rilascia la risorsa (sync perche' modifica tanti campi ed e' meglio farlo in modo "atomico"
	public synchronized void endPowerBoost() throws JAXBException
	{
		// riazzera tutto per poter ricominciare: come da oggetto nuobo (costruttore)
		String[] richiesta;
		String senderId, senderIp;
		int senderPort;
		MessageSenderThread boostMessageSender;

		System.out.println("{ " + casaId + " } POWER BOOST terminato");

		// azzera timestamp utlima richiesta (-1)
		this.messageTimestamp = -1;

		// resetta il conto degli OK ricevuti (0)
		this.OKCount = 0;

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
}
