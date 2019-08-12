package ClientCasa.P2P.Boost;

import ClientCasa.CasaApp;
import ClientCasa.P2P.MessageSenderThread;
import ClientCasa.P2P.P2PMessage;
import ClientCasa.LocalStatistics.smartMeter.SmartMeterSimulator;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

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

	private String casaId;
	private int casaBoostPort;
	private SmartMeterSimulator simulator;
	private PowerBoostState state;
	// numero case che stanno usando il boost (0-2)
	private int boostCount;
	// coda di case che hanno richiesto boost mentre lo stavi usando
	private List<String[]> queue;
	// numero case in gioco quando manda la prima richiesta boost
	private int caseAttive;
	// contatore degli OK
	private int OKCount;
	// timestamp del messaggio di richiesta appena inviato
	private long messageTimestamp;

	public PowerBoost(String casaId, int casaBoostPort, SmartMeterSimulator simulator)
	{
		this.casaId = casaId;
		this.casaBoostPort = casaBoostPort;
		this.simulator = simulator;
		this.state = PowerBoostState.NOT_INTERESTED;
		this.boostCount = 0;
		this.caseAttive = 1;
		this.OKCount = 0;
		this.queue = new ArrayList<>();
		this.messageTimestamp = -1;

		// logger levels
		LOGGER.setLevel(CasaApp.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(CasaApp.LOGGER_LEVEL);
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

		try
		{
			// setta il proprio stato interno: ha richiesto il boost
			setState(PowerBoostState.REQUESTED);

			// MANDA A TUTTI MSG BOOST REQUEST
			// chiede elenco case e si salva il numero, per contare poi gli OK
			condominio = CasaApp.getCondominio();
			setCaseAttive(condominio.size());

			// invia "BOOST" a tutti compreso se stesso, con anche timestamp
			setMessageTimestamp(new Date().getTime());

			// se c'e' solo una casa (o 2), sa gia' per certo di poter richiedere subito BOOST
			if(condominio.size() == 1)
			{
				LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Sono l'unica casa attiva e quindi ottengo BOOST!");

				// chiama metodo simulatore per fare effettivamente POWER BOOST
				beginPowerBoost();

				// finito il tempo in cui usa BOOST, rilascia risorsa e resetta lo stato
				endPowerBoost();
			}
			else
			{
				for(Casa c : condominio.getCaselist())
				{
					boostMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getBoostPort(), new P2PMessage(casaId, casaBoostPort, c.getId(), "BOOST", getMessageTimestamp()));
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

	// chiama metodo simulatore per il power boost
	public synchronized void beginPowerBoost() throws InterruptedException
	{
		// setta stato, cosi' se riceve altre richieste BOOST nel frattempo, le accodera'
		this.state = PowerBoost.PowerBoostState.USING;

		// aumenta di 1 il numero di case che stanno usando il boost
		this.boostCount += 1;

		System.out.println("{ " + casaId + " } POWER BOOST iniziato");
		this.simulator.boost();
		System.out.println("{ " + casaId + " } POWER BOOST terminato");
	}

	// finito il power boost, rilascia la risorsa (sync perche' modifica tanti campi ed e' meglio farlo in modo "atomico"
	public synchronized void endPowerBoost() throws JAXBException
	{
		// riazzera tutto per poter ricominciare: come da oggetto nuobo (costruttore)
		String richiesta[];
		String senderId, senderIp;
		int senderPort;
		MessageSenderThread boostMessageSender;

		// azzera timestamp utlima richiesta (-1)
		this.messageTimestamp = -1;

		// resetta il conto degli OK ricevuti (0)
		this.OKCount = 0;

		// diminuisce di 1 il numero di case che stanno usando il boost
		this.boostCount -= 1;

		// setta stato iniziale
		this.state = PowerBoostState.NOT_INTERESTED;

		// svuota coda di attesa, inviando OK a tutti i presenti
		while((richiesta = deaccodaRichiesta()) != null)
		{
			senderId = richiesta[0];
			senderIp = richiesta[1];
			senderPort = Integer.parseInt(richiesta[2]);

			// risponde OK
			boostMessageSender = new MessageSenderThread(casaId, senderId, senderIp, senderPort, new P2PMessage(casaId, casaBoostPort, senderId, "OK"));
			boostMessageSender.start();

			LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Fine BOOST: mando OK a " + senderId);
		}
	}
}
