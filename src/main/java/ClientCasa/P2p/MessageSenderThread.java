package ClientCasa.P2p;

import ClientCasa.P2p.Statistics.Election.Election;
import ClientCasa.P2p.Statistics.Election.ElectionMessage;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

// Thread per mandare messaggi; per ora fa marshal solo di MeanMeasurement e ElectionMessage; ma puo' essere riutilizzato per altri msg
public class MessageSenderThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(MessageSenderThread.class.getName());

	private String ip;
	private String senderId;
	private String destId;
	private int port;
	private Object message;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;

	private void MessageSenderThread(String senderId, String destId, String ip, int port)
	{
		this.senderId = senderId;
		this.destId = destId;
		this.ip = ip;
		this.port = port;
	}

	public MessageSenderThread(String senderId, String destId, String ip, int port, MeanMeasurement message) throws JAXBException
	{
		MessageSenderThread(senderId, destId, ip, port);
		this.message = message;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	public MessageSenderThread(String senderId, String destId, String ip, int port, ElectionMessage message) throws JAXBException
	{
		MessageSenderThread(senderId, destId, ip, port);
		this.message = message;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(ElectionMessage.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	@Override
	public void run()
	{
		// invia meanMeasurement con socket + jaxb
		Socket socket;

		try
		{
			socket = new Socket(ip, port);
			marshaller.marshal(message, socket.getOutputStream());
			socket.close();

			LOGGER.log(Level.INFO, "{ " + senderId + " } Statistic sent to " + senderId + " (" + ip + ": " + port + ")");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + senderId + " } Unable to connect to " + ip + ": " + port);
			e.printStackTrace();
		}
	}
}
