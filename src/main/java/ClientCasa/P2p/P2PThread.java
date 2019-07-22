package ClientCasa.P2p;

import ClientCasa.CasaApp;
import ServerREST.beans.Condominio;

import java.util.logging.Logger;

public class P2PThread extends Thread
{
	private String casaIp, casaId;
	private int casaStatsPort;
	private static final String SERVER_URL = "http://localhost:1337";
	private static final Logger LOGGER = Logger.getLogger(P2PThread.class.getName());
	private static final int RETRY_TIMEOUT = 250;

	public P2PThread(String id, String ip, int statsPort)
	{
		this.casaId = id;
		this.casaIp = ip;
		this.casaStatsPort = statsPort;
	}



	public void run()
	{
		try
		{
			///////////////////////////////
			/*	RICHIEDE ELENCO CASE	*/
			Condominio condominio = CasaApp.getCondominio();

			// lancia thread che riceve le statistiche
			StatsReceiverThread statsReceiver = new StatsReceiverThread(casaId, casaIp, casaStatsPort);
			statsReceiver.start();


			// lancia thread elezione bully: fa elezione e, se non eletto termina; se eletto invia statistiche

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}