package ClientCasa.P2p.Statistics.Election;

import ClientCasa.CasaApp;
import ClientCasa.P2p.MessageSenderThread;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

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

	public Election(String casaId, int casaElectionPort)
	{
		this.casaId = casaId;
		this.casaElectionPort = casaElectionPort;
		setState(ElectionOutcome.NEED_ELECTION);
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
			condominio = CasaApp.getCondominio();

			// manda msg elezione a TUTTI, tranne se stesso (informa tutti che e' entrato: se c'e' un coord gli risponde, se non c'e allora si fa elezione)
			// se e' l'unica casa a entrare qua (da sola), allora si autoproclama coord di se stesso e basta
			for(Casa c: condominio.getCaselist())
			{
				caseTot++;

				if(c.getId().compareTo(casaId) != 0)
				{
					// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getElectionPort(), new ElectionMessage(casaId, casaElectionPort, c.getId(), "ELECTION"));
					electionMessageSender.start();
				}
			}
			// se c'e' solo una casa in rete allora fa subito coord
			if(caseTot == 1)
			{
				// FIXME: remove print
				System.out.println("{ " + casaId + " } [ START ELECTION ] Sono da solo e faccio io il coord");
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
			condominio = CasaApp.getCondominio();

			// manda msg elezione a TUTTI, tranne se stesso (informa tutti che sta per uscire)
			for(Casa c : condominio.getCaselist())
			{
				if(c.getId().compareTo(casaId) != 0)
				{
					// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getElectionPort(), new ElectionMessage(casaId, casaElectionPort, c.getId(), "NEED_REELECTION"));
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
