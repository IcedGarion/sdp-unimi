package ClientCasa.P2p;

import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSenderThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(MessageSenderThread.class.getName());

	private String ip;
	private String senderId;
	private String destId;
	private int port;
	private MeanMeasurement message;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;

	public MessageSenderThread(String senderId, String destId, String ip, int port, MeanMeasurement message) throws JAXBException
	{
		this.senderId = senderId;
		this.destId = destId;
		this.ip = ip;
		this.port = port;
		this.message = message;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
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

			// assert socket.getResponseCode() == 200 : "MessageSender: Send statistics failed ( " + conn.getResponseCode() + " " + conn.getResponseMessage() + " )";

			LOGGER.log(Level.INFO, "{ " + senderId + " } Statistic sent to " + senderId + " (" + ip + ": " + port + ")");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + senderId + " } Unable to connect to " + ip + ": " + port);
			e.printStackTrace();
		}



	}
}
