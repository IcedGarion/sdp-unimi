package ClientCasa.P2P.Boost;

import ClientCasa.P2P.P2PMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PowerBoostWorkerThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(PowerBoostWorkerThread.class.getName());
	private P2PMessage message;
	private Socket listenSocket;
	private String casaId;
	private PowerBoost powerBoostState;

	public PowerBoostWorkerThread(Socket listenSocket, String casaId, PowerBoost powerBoostState) throws JAXBException
	{
		this.listenSocket = listenSocket;
		this.casaId = casaId;
		this.powerBoostState = powerBoostState;
	}

	public void run()
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		P2PMessage boostMessage;

		try
		{
			jaxbContext = JAXBContext.newInstance(P2PMessage.class);
			unmarshaller = jaxbContext.createUnmarshaller();

			boostMessage = (P2PMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
			listenSocket.close();

			switch(boostMessage.getMessage())
			{
				// TODO: algoritmo mutua esclusione
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error during power boost messages");
			e.printStackTrace();
		}
	}
}
