package ClientCasa.P2p.Statistics;

import ClientCasa.CasaApp;
import ServerREST.beans.Condominio;

import java.util.logging.Level;
import java.util.logging.Logger;

/*	Classe statica con metodi per gestire elezione.
	Chi ha bisogno indice elezione e vede ritornarsi uno status: eletto coordinatore oppure no. Poi decide cosa fare in base a quello status
 */
public class Election
{
	public enum ElectionStatus { NEED_ELECTION, COORD, NOT_COORD };
	public static String coordinator;
	private static final Logger LOGGER = Logger.getLogger(Election.class.getName());

	// gli id casa possono essere stringhe arbitrarie e non per forza solo numeri. Confronto viene fatto da java compare su stringhe
	public static ElectionStatus elect(String casaId)
	{
		Condominio condominio;
		ElectionStatus ret = ElectionStatus.NOT_COORD;

		try
		{
			// lista case coinvolte in elezione
			condominio = CasaApp.getCondominio();

		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred during elecion");
			e.printStackTrace();
		}

		return ret;
	}
}
