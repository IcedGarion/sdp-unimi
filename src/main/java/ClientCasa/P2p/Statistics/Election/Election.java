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
	public String casaId;

	// dati condivisi (con relativi metodi per accedere: stato (se sei coord)
	public ElectionOutcome state;

	public Election(String casaId)
	{
		this.casaId = casaId;
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


	// Fa elezione: ci sono 2 thread che interagiscono: uno manda per primo il msg, l'altro risponde
	// ma entrambi fanno sia client che server
	// solo che il "listener" rimane perennemente attivo in ascolto.. invece questo viene chiamato solo quando serve elezione
	public void startElection()
	{
		Condominio condominio;
		MessageSenderThread electionMessageSender;

		try
		{
			// Thread ascolto elezione e' gia' attivato... E' lì che aspetta di rispondere a questo thread che invierà msg di inizio


			/* INIZIA ELEZIONE: manda msg ELECTION a ~tutti; poi electionThread se ne occupa*/
			// lista case coinvolte in elezione
			condominio = CasaApp.getCondominio();

			// manda msg elezione a tutti quelli con ID maggiore
			for(Casa c: condominio.getCaselist())
			{
				if(c.getId().compareTo(casaId) > 0)
				{
					// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
					electionMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getElectionPort(), new ElectionMessage(casaId, c.getId(), "ELECTION"));
					electionMessageSender.start();
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred during elecion");
			e.printStackTrace();
		}
	}
}
