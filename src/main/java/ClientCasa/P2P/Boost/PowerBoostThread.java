package ClientCasa.P2P.Boost;

import ClientCasa.CasaApp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	Server concorrente che lancia thread che gestiscono le richieste power boost p2p
 */
public class PowerBoostThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(PowerBoostThread.class.getName());
	private String casaId;
	private int boostPort;
	private PowerBoost powerBoostState;

	public PowerBoostThread(String casaId, int boostPort, PowerBoost powerBoostState)
	{
		this.casaId = casaId;
		this.boostPort = boostPort;
		this.powerBoostState = powerBoostState;
	}

	public void run()
	{
		ServerSocket welcomeSocket;
		Socket connectionSocket;
		PowerBoostWorkerThread powerWorker;

		try
		{
			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(boostPort);

			while(true)
			{
				connectionSocket = welcomeSocket.accept();
				powerWorker = new PowerBoostWorkerThread(connectionSocket, casaId, boostPort, powerBoostState);
				powerWorker.start();
				LOGGER.log(Level.FINE, "{ " + casaId + " } Received connection for Power Boost: launching worker thread");


				// check TERMINAZIONE
				if(interrupted())
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping PowerBoostThread... ");
					return;
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in power boost connection");
			e.printStackTrace();
		}
	}
}
