package ClientCasa.P2p.Statistics.Election;

import ClientCasa.CasaApp;
import ClientCasa.P2p.MessageSenderThread;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectionThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(ElectionThread.class.getName());

	private String casaId;
	private int listenPort;
	private Election electionObject;

	public ElectionThread(String casaId, int listenPort, Election electionObject)
	{
		this.casaId = casaId;
		this.listenPort = listenPort;
		this.electionObject = electionObject;
	}

	public void run()
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(ElectionMessage.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			ElectionMessage electionMessage;
			MessageSenderThread electionMessageSender;
			Condominio condominio;

			// crea server socket in ascolto
			ServerSocket welcomeSocket = new ServerSocket(listenPort);
			Socket listenSocket;

			while(true)
			{
				// ascolta msg
				listenSocket = welcomeSocket.accept();
				electionMessage = (ElectionMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
				listenSocket.close();

				// decide cosa fare in base al messaggio:
				switch(electionMessage.getMessage())
				{
					// e' stato contattato perhe' ha ID maggiore di qualcuno: risponde OK e contatta i superiori a sua volta
					case "ELECTION":
						// salta step di rispondere OK, tanto uscite sono controllate: passa direttamente a inviare ELECTION ai suoi superiori
						// lista case
						condominio = CasaApp.getCondominio();

						for(Casa c: condominio.getCaselist())
						{
							if(c.getId().compareTo(casaId) > 0)
							{
								// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
								electionMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getElectionPort(), new ElectionMessage(casaId, c.getId(), "ELECTION"));
								electionMessageSender.start();
							}
						}

						// TODO: avanti con bully

						break;

					// caso in cui coordinatore esce dalla rete: dice a tutti che se ne va e tutti quanti fanno nuova elezione
					case "NEED_REELECTION":
						electionObject.setState(Election.ElectionOutcome.NEED_ELECTION);
						break;
				}

				// check TERMINAZIONE
				if(interrupted())
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping ElectionThread... ");
					return;
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
