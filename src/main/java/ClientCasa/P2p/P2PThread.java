package ClientCasa.P2p;

import ClientCasa.P2p.Statistics.Election.ElectionListener;
import ClientCasa.P2p.Statistics.StatsReceiverServerThread;

import java.util.logging.Level;
import java.util.logging.Logger;


/*	POTREBBE BENISSIMO ESSERE TOLTO: METTI LE ISTRUZIONI DELLA RUN DIRETTAMENTE IN CASAAPP...
 	per ora e' abbastanza inutile isolarle	*/
public class P2PThread extends Thread
{
	private String casaIp, casaId;
	private int casaStatsPort;
	private int casaElectionPort;
	private static final String SERVER_URL = "http://localhost:1337";
	private static final Logger LOGGER = Logger.getLogger(P2PThread.class.getName());
	private static final int RETRY_TIMEOUT = 250;

	public P2PThread(String id, String ip, int statsPort, int electionPort)
	{
		this.casaId = id;
		this.casaIp = ip;
		this.casaStatsPort = statsPort;
		this.casaElectionPort = electionPort;
	}

	public void run()
	{
		try
		{
			// lancia thread che riceve le statistiche
			StatsReceiverServerThread statsReceiver = new StatsReceiverServerThread(casaId, casaStatsPort);
			statsReceiver.start();


			// lancia thread "ascoltatore" elezione bully: riceve msg e risponde a dovere secondo alg BULLY
			ElectionListener listenerThread = new ElectionListener(casaId, casaElectionPort);
			listenerThread.start();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error");
			e.printStackTrace();
		}
	}
}