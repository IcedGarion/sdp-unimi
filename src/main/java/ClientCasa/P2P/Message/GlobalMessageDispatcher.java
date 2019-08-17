package ClientCasa.P2P.Message;

import ClientCasa.P2P.Boost.PowerBoostResponder;
import ClientCasa.P2P.GlobalStatistics.Election.ElectionResponder;
import ClientCasa.P2P.GlobalStatistics.StatsReceiverResponder;
import Shared.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 	RICEVE SOCKET DA SERVER CONCORRENTE: legge messaggio e decide, in base a un campo, a chi girarlo
 * 	Ha mappa "dispatchTo" (campo msg) -> MessageResponder da chiamare
 */
public class GlobalMessageDispatcher extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(GlobalMessageDispatcher.class.getName());
	private Socket listenSocket;
	private HashMap<String, MessageResponder> dispatchMap;

	public GlobalMessageDispatcher(Socket connectionSocket, StatsReceiverResponder statsReceiverComponent, ElectionResponder electionComponent, PowerBoostResponder powerBoostComponent)
	{
		this.listenSocket = connectionSocket;

		dispatchMap = new HashMap<>();
		dispatchMap.put("STATS", statsReceiverComponent);
		dispatchMap.put("ELECTION", electionComponent);
		dispatchMap.put("BOOST", powerBoostComponent);
	}

	public void run()
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		P2PMessage message;
		MessageResponder responder;

		try
		{
			jaxbContext = JAXBContext.newInstance(P2PMessage.class);
			unmarshaller = jaxbContext.createUnmarshaller();

			// estrae msg e setta IP sender (serve a qualcuno)
			message = (P2PMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
			message.setSenderIp(listenSocket.getInetAddress().getHostAddress());
			listenSocket.close();

			// LOGGER.log(Level.INFO, "P2P Message received from " + message.getSenderId() + ": [ " + message.getDispatchto() + " ] " + message.getMessage());

			// decide quale metodo chiamare, fra i vari componenti del sistema, a seconda del messaggio
			responder = dispatchMap.get(message.getDispatchto());
			responder.respond(message);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + Configuration.CASA_ID + " } Error while reading P2PMessage");
			e.printStackTrace();
		}
	}
}
