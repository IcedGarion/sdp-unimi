package ClientCasa.P2P.GlobalStatistics.Election;

import Shared.MessageSenderThread;
import ClientCasa.P2P.Message.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import Shared.Configuration;
import Shared.Http;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: dividi in multipli lock, uno per electionState e uno per coord

/*	Classe statica con metodi per gestire elezione: chi ha bisogno indice elezione.
    Ha anche metodi sync per gestire stato elezione.

	Gli id casa possono essere stringhe arbitrarie e non per forza solo numeri. Confronto (>) viene fatto da java compare su stringhe
 */
public class Election
{
	private static final Logger LOGGER = Logger.getLogger(Election.class.getName());
	public enum ElectionState { NEED_ELECTION, COORD, NOT_COORD };
	private String casaId;
	private String[] coord;

	// dati condivisi (con relativi metodi per accedere: stato (se sei coord)
	public ElectionState state;

	public Election()
	{
		this.casaId = Configuration.CASA_ID;
		setState(ElectionState.NEED_ELECTION);
		this.coord = new String[3];

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public synchronized void setState(ElectionState state)
	{
		this.state = state;
	}
	public synchronized ElectionState getState()
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
			/* INIZIA ELEZIONE: manda msg ELECTION a ~tutti; poi electionThread se ne occupa	*/
			// lista case coinvolte in elezione
			condominio = Http.getCondominio();

			// manda msg elezione a tutti tranne se stesso (informa tutti che e' entrato: se c'e' un coord gli risponde, se non c'e allora si fa elezione)
			// se e' l'unica casa a entrare qua (da sola), allora si autoproclama coord di se stesso e basta
			for(Casa c: condominio.getCaselist())
			{
				caseTot++;

				if(c.getId().compareTo(casaId) != 0)
				{
					// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getPort(), new P2PMessage(casaId, Configuration.CASA_PORT, "ELECTION", "ELECTION"));
					electionMessageSender.start();
				}
			}
			// se c'e' solo una casa in rete allora fa subito coord
			if(caseTot == 1)
			{
				LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Sono da solo e faccio io il coord");
				setState(ElectionState.COORD);
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

			// manda msg rielezione a TUTTI, tranne se stesso (informa tutti che sta per uscire)
			for(Casa c : condominio.getCaselist())
			{
				if(c.getId().compareTo(casaId) != 0)
				{
					// invia "NEED_REELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getPort(), new P2PMessage(casaId, Configuration.CASA_PORT, "NEED_REELECTION", "ELECTION"));
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

	// Salva le info sul coordinatore: [0] id, [1] ip, [2] port
	public synchronized void setCoord(String coordId, String coordIp, int coordPort)
	{
		this.coord[0] = coordId;
		this.coord[1] = coordIp;
		this.coord[2] = String.valueOf(coordPort);
	}

	public synchronized String[] getCoord()
	{
		return this.coord;
	}
}
