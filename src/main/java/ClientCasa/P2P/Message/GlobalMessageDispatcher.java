package ClientCasa.P2P.Message;

import ClientCasa.P2P.Boost.PowerBoostWorkerThread;
import ClientCasa.P2P.GlobalStatistics.Election.ElectionWorkerThread;
import ClientCasa.P2P.GlobalStatistics.StatsReceiverThread;
import Shared.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalMessageDispatcher extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(GlobalMessageDispatcher.class.getName());
	private Socket listenSocket;
	private StatsReceiverThread statsReceiverComponent;
	private ElectionWorkerThread electionComponent;
	private PowerBoostWorkerThread powerBoostComponent;

	public GlobalMessageDispatcher(Socket connectionSocket, StatsReceiverThread statsReceiverComponent, ElectionWorkerThread electionComponent, PowerBoostWorkerThread powerBoostComponent)
	{
		this.listenSocket = connectionSocket;
		this.statsReceiverComponent = statsReceiverComponent;
		this.electionComponent = electionComponent;
		this.powerBoostComponent = powerBoostComponent;
	}

	public void run()
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		P2PMessage message;

		try
		{
			jaxbContext = JAXBContext.newInstance(P2PMessage.class);
			unmarshaller = jaxbContext.createUnmarshaller();

			// estrae msg e setta IP sender (serve a qualcuno)
			message = (P2PMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
			message.setSenderIp(listenSocket.getInetAddress().getHostAddress());
			listenSocket.close();

			// decide quale metodo chiamare, fra i vari componenti del sistema, a seconda del messaggio
			switch(message.getDispatchto())
			{
				case "BOOST":
					powerBoostComponent.run(message);
					break;
				case "ELECTION":
					electionComponent.run(message);
					break;
				case "STATS":
					statsReceiverComponent.run(message);
					break;
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + Configuration.CASA_ID + " } Error while reading P2PMessage");
			e.printStackTrace();
		}
	}
}
