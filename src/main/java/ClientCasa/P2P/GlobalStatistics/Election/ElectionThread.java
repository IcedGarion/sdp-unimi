package ClientCasa.P2P.GlobalStatistics.Election;

import ClientCasa.CasaApp;
import Shared.Configuration;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
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

	public ElectionThread(int listenPort, Election electionObject)
	{
		this.casaId = Configuration.CASA_ID;
		this.casaElectionPort = listenPort;
		this.electionObject = electionObject;

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public void run()
	{
		ElectionWorkerThread electionWorker;
		Socket listenSocket;
		ServerSocket welcomeSocket;

		try
		{
			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(casaElectionPort);

			while(true)
			{
				try
				{
					// ascolta msg
					listenSocket = welcomeSocket.accept();
					electionWorker = new ElectionWorkerThread(listenSocket, casaElectionPort, electionObject);
					electionWorker.start();

					// check TERMINAZIONE
					if(interrupted())
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping ElectionThread... ");
						return;
					}
				}
				catch(Exception e)
				{
					LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in serving election request");
					e.printStackTrace();
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
