package ClientCasa.P2P.Boost;

import ClientCasa.CasaApp;
import ClientCasa.P2P.MessageSenderThread;
import ClientCasa.P2P.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
	private PowerBoostState state;
	// numero case che stanno usando il boost (0-2)
	private int boostCount;
	// coda di case che hanno richiesto boost mentre lo stavi usando
	private List<String> queue;
	// numero case in gioco quando manda la prima richiesta boost
	private int caseAttive;
	// timestamp del messaggio di richiesta appena inviato
	private long messageTimestamp;

	public PowerBoost(String casaId, int casaBoostPort)
	{
		this.casaId = casaId;
		this.casaBoostPort = casaBoostPort;
		this.state = PowerBoostState.NOT_INTERESTED;
		this.boostCount = 0;
		this.caseAttive = 1;
		this.queue = new ArrayList<>();
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

	// aggiungi / rimuovi casa dalla coda delle richieste
	public synchronized String rimuoviRichiesta()
	{
		// prende elemento in testa, rimuove e ritorna
		String ret = queue.get(0);
		queue.remove(0);
		return ret;
	}

	public synchronized void accodaRichiesta(String casaId)
	{
		// inserisce in coda
		queue.add(casaId);
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// INIZIO BOOST
	// simile a Election.startElection(): avvisa tutti che serve boost; poi il thread rispondera' e gestira' lui
	public void requestPowerBoost()
	{
		Condominio condominio;
		MessageSenderThread boostMessageSender;
		long timestamp;

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

			for(Casa c : condominio.getCaselist())
			{
				boostMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getBoostPort(), new P2PMessage(casaId, casaBoostPort, c.getId(), "BOOST", getMessageTimestamp()));
				boostMessageSender.start();
			}

			// FIXME: remove print
			System.out.println("{ " + casaId + " } [ BOOST ] Inviato msg BOOST a tutte le " + getCaseAttive() + " case");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred while requesting power boost");
			e.printStackTrace();
		}
	}

	// finito il power boost, rilascia la risorsa
	public void endedPowerBoost()
	{
		// TODO
	}
}
