package ClientCasa.P2p.Statistics.Election;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	SERVER CONCORRENTE (tipo StatReceiver): RICEVE MESSAGGI DI ELEZIONE E LANCIA THREAD CHE SE NE OCCUPANO
 */
public class ElectionThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(ElectionThread.class.getName());

	private String casaId;
	private int casaElectionPort;
	private Election electionObject;

	public ElectionThread(String casaId, int listenPort, Election electionObject)
	{
		this.casaId = casaId;
		this.casaElectionPort = listenPort;
		this.electionObject = electionObject;
	}

	public void run()
	{
		try
		{
			ElectionWorkerThread electionWorker;

			// crea server socket in ascolto
			ServerSocket welcomeSocket = new ServerSocket(casaElectionPort);
			Socket listenSocket;

			while(true)
			{
				// ascolta msg
				listenSocket = welcomeSocket.accept();
				electionWorker = new ElectionWorkerThread(listenSocket, casaId, casaElectionPort, electionObject);
				electionWorker.start();

				// check TERMINAZIONE
				if(interrupted())
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping ElectionThread... ");
					return;
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in election connection");
			e.printStackTrace();
		}
	}
}
