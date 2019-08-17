package ClientCasa.P2P.Message;

import ClientCasa.P2P.Boost.PowerBoostResponder;
import ClientCasa.P2P.GlobalStatistics.Election.ElectionResponder;
import ClientCasa.P2P.GlobalStatistics.StatsReceiverResponder;
import Shared.Configuration;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 	SERVER CONCORRENTE GLOBALE: tutti i messaggi scambati fra la rete P2P passano da qua: riceve msg e lancia thread che lo gestisce
 * 	Il thread legge un campo del msg e decide quale metodo chiamare del resto del sistema
 */
public class GlobalMessageServer extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(GlobalMessageServer.class.getName());

	private int listenPort;
	private StatsReceiverResponder statsReceiverComponent;
	private ElectionResponder electionComponent;
	private PowerBoostResponder powerBoostComponent;

	// deve conoscere i 3 componenti a cui distribuire i messaggi che riceve
	public GlobalMessageServer(StatsReceiverResponder statsReceiver, ElectionResponder electionWorker, PowerBoostResponder powerBoostWorker)
	{
		this.listenPort = Configuration.CASA_PORT;
		this.statsReceiverComponent = statsReceiver;
		this.electionComponent = electionWorker;
		this.powerBoostComponent = powerBoostWorker;

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public void run()
	{
		ServerSocket welcomeSocket;
		Socket connectionSocket;
		GlobalMessageDispatcher dispatcherThread;
		String casaId = "";

		try
		{
			// recupera config
			casaId = Configuration.CASA_ID;
			listenPort = Configuration.CASA_PORT;

			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(listenPort);

			while(true)
			{
				try
				{
					connectionSocket = welcomeSocket.accept();
					dispatcherThread = new GlobalMessageDispatcher(connectionSocket, statsReceiverComponent, electionComponent, powerBoostComponent);
					dispatcherThread.start();

					LOGGER.log(Level.FINE, "{ " + casaId + " } Received P2PMessage");


					// check TERMINAZIONE
					if(interrupted())
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping GlobalMessageDispatcher... ");
						return;
					}
				}
				catch(Exception e)
				{
					LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in server message accept");
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in server message listen");
			e.printStackTrace();
		}
	}
}
