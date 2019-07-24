package ClientCasa.P2p.Statistics.Election;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectionListener extends Thread
{
	private String casaId;
	private int listenPort;
	private String state;
	private static final Logger LOGGER = Logger.getLogger(ElectionListener.class.getName());

	public ElectionListener(String casaId, int listenPort)
	{
		this.casaId = casaId;
		this.listenPort = listenPort;
		this.state = "WAIT_ELECTION";
	}

	public void run()
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(ElectionMessage.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			ElectionMessage electionMessage;

			// crea server socket in ascolto
			ServerSocket welcomeSocket = new ServerSocket(listenPort);
			Socket listenSocket;

			while(true)
			{
				listenSocket = welcomeSocket.accept();

				// ascolta msg
				electionMessage = (ElectionMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
				listenSocket.close();

				// decide cosa fare in base al messaggio:
				switch(electionMessage.getMessage())
				{
					// e' stato contattato perhe' ha ID maggiore di qualcuno: risponde OK e contatta i superiori a sua volta
					case "ELECTION":
						// risponde OK




						// TODO: avanti con bully



						state = "CONTACT_GREATER_ID";
						break;
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error occurred during listening elecion");
			e.printStackTrace();
		}
	}
}
