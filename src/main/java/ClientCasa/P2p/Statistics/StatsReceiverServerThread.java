package ClientCasa.P2p.Statistics;

import javax.xml.bind.JAXBException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	QUESTO È SOLO UN SERVER CONCORRENTE CHE GESTISCE LE RICHIESTE;
	IL VERO LAVORO LO FA IL THREAD LANCIATO, StatsReceiverThread
 */
public class StatsReceiverServerThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(StatsReceiverServerThread.class.getName());

	private String casaId;
	private int statsPort;

	public StatsReceiverServerThread(String casaId, int statsPort) throws JAXBException
	{
		this.casaId = casaId;
		this.statsPort = statsPort;
	}

	// server concorrente
	public void run()
	{
		ServerSocket welcomeSocket;
		Socket connectionSocket;

		try
		{
			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(statsPort);

			// delega a nuovo thread la gestione della connessione in arrivo e si rimette in ascolto
			while(true)
			{
				connectionSocket = welcomeSocket.accept();
				StatsReceiverThread receiver = new StatsReceiverThread(connectionSocket, casaId);
				receiver.start();
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.INFO, "{ " + casaId + " } Error while receiving stats");
			e.printStackTrace();
		}
	}
}
