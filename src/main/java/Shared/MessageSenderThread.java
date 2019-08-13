package Shared;

import ClientCasa.P2P.P2PMessage;
import ServerREST.beans.MeanMeasurement;
import ServerREST.beans.Notifica;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

// Thread per mandare messaggi; per ora fa marshal solo di MeanMeasurement, P2PMessage e Notifica; ma puo' essere riutilizzato per altri msg
public class MessageSenderThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(MessageSenderThread.class.getName());

	private String ip;
	private String senderId;
	private int port;
	private Object message;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;

	private MessageSenderThread(String senderId, String ip, int port)
	{
		this.senderId = senderId;
		this.ip = ip;
		this.port = port;

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public MessageSenderThread(String senderId, String ip, int port, MeanMeasurement message) throws JAXBException
	{
		this(senderId, ip, port);
		this.message = message;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	public MessageSenderThread(String senderId, String ip, int port, P2PMessage message) throws JAXBException
	{
		this(senderId, ip, port);
		this.message = message;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(P2PMessage.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	public MessageSenderThread(String senderId, String ip, int port, Notifica message) throws JAXBException
	{
		this(senderId, ip, port);
		this.message = message;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(Notifica.class);
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

			LOGGER.log(Level.FINE, "{ " + senderId + " } Message sent to " + senderId + " (" + ip + ": " + port + ")");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + senderId + " } Unable to connect to " + ip + ": " + port);
			e.printStackTrace();
		}
	}
}
