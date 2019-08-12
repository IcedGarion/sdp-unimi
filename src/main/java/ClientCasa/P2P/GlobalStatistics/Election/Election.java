package ClientCasa.P2P.GlobalStatistics.Election;

import ClientCasa.CasaApp;
import ClientCasa.P2P.MessageSenderThread;
import ClientCasa.P2P.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import Shared.Configuration;
import Shared.Http;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/*	Classe statica con metodi per gestire elezione.
	Chi ha bisogno indice elezione e vede ritornarsi uno status: eletto coordinatore oppure no. Poi decide cosa fare in base a quello status

	Gli id casa possono essere stringhe arbitrarie e non per forza solo numeri. Confronto viene fatto da java compare su stringhe
 */
public class Election
{
	private static final Logger LOGGER = Logger.getLogger(Election.class.getName());
	public enum ElectionOutcome	{ NEED_ELECTION, COORD, NOT_COORD };
	private String casaId;
	private int casaElectionPort;


	// dati condivisi (con relativi metodi per accedere: stato (se sei coord)
	public ElectionOutcome state;

	public Election(int casaElectionPort)
	{
		this.casaId = Configuration.CASA_ID;
		this.casaElectionPort = casaElectionPort;
		setState(ElectionOutcome.NEED_ELECTION);

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public synchronized void setState(ElectionOutcome state)
	{
		this.state = state;
	}
	public synchronized ElectionOutcome getState()
	{
		return this.state;
	}


	// Fa partire elezione: manda msg al thread vero di election per mettere in moto tutto;
	// ElectionThread vede msg ELECTION e parla con gli altri ElectionThread
	public void startElection()
	{
		Condominio condominio;
		MessageSenderThread electionMessageSender;
		int caseTot = 0;

		try
		{
			// Thread vero elezione e' gia' attivato... E' lì che aspetta di rispondere a questo thread che invierà msg di inizio


			/* INIZIA ELEZIONE: manda msg ELECTION a ~tutti; poi electionThread se ne occupa*/
			// lista case coinvolte in elezione
			condominio = Http.getCondominio();

			// manda msg elezione a TUTTI, tranne se stesso (informa tutti che e' entrato: se c'e' un coord gli risponde, se non c'e allora si fa elezione)
			// se e' l'unica casa a entrare qua (da sola), allora si autoproclama coord di se stesso e basta
			for(Casa c: condominio.getCaselist())
			{
				caseTot++;

				if(c.getId().compareTo(casaId) != 0)
				{
					// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getElectionPort(), new P2PMessage(casaId, casaElectionPort, "ELECTION"));
					electionMessageSender.start();
				}
			}
			// se c'e' solo una casa in rete allora fa subito coord
			if(caseTot == 1)
			{
				LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Sono da solo e faccio io il coord");
				setState(ElectionOutcome.COORD);
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred while starting elecion");
			e.printStackTrace();
		}
	}

	// Il coord esce dalla rete: avvisa tutti che dovranno eleggerne uno nuovo
	public void coordLeaving()
	{
		Condominio condominio;
		MessageSenderThread electionMessageSender;

		try
		{
			condominio = Http.getCondominio();

			// manda msg elezione a TUTTI, tranne se stesso (informa tutti che sta per uscire)
			for(Casa c : condominio.getCaselist())
			{
				if(c.getId().compareTo(casaId) != 0)
				{
					// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getElectionPort(), new P2PMessage(casaId, casaElectionPort, "NEED_REELECTION"));
					electionMessageSender.start();
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred in coord leaving");
			e.printStackTrace();
		}
	}
}
