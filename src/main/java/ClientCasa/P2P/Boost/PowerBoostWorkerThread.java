package ClientCasa.P2P.Boost;

import ClientCasa.P2P.MessageSenderThread;
import ClientCasa.P2P.P2PMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PowerBoostWorkerThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(PowerBoostWorkerThread.class.getName());
	private Socket listenSocket;
	private String casaId;
	private int casaBoostPort;
	private PowerBoost powerBoostObject;

	public PowerBoostWorkerThread(Socket listenSocket, String casaId, int casaBoostPort, PowerBoost powerBoostState)
	{
		this.listenSocket = listenSocket;
		this.casaId = casaId;
		this.casaBoostPort = casaBoostPort;
		this.powerBoostObject = powerBoostState;
	}

	// RICART & AGRAWALA
	public void run()
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		P2PMessage boostMessage;
		MessageSenderThread boostMessageSender;
		String senderId, senderIp;
		int senderPort;
		long senderTimestamp, myMessageTimestamp;

		try
		{
			// legge e prepara campi msg ricevuto
			jaxbContext = JAXBContext.newInstance(P2PMessage.class);
			unmarshaller = jaxbContext.createUnmarshaller();

			boostMessage = (P2PMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
			senderId = boostMessage.getSenderId();
			senderIp = listenSocket.getInetAddress().getHostAddress();
			senderPort = boostMessage.getSenderPort();
			senderTimestamp = boostMessage.getTimestamp();

			listenSocket.close();

			switch(boostMessage.getMessage())
			{
				// qualcuno ha richesto power boost: 3 casi (stato)
				case "BOOST":
					// anche lui ha richiesto (o SOLO lui): decide se usare BOOST e accodare gli altri, oppure se rinunciare e rispondere OK
					if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.REQUESTED))
					{
						// confronta timestamp della sua richiesta con quella di chi lo ha mandato (usa info salvate)
						myMessageTimestamp = powerBoostObject.getMessageTimestamp();
						// se la sua richiesta e' piu vecchia, va lui: accoda
						if(myMessageTimestamp < senderTimestamp)
						{

							// TODO: accoda

						}
						// se chi ha mandato msg e' piu' vecchia, va l'altro: OK
						else
						{

							// TODO: rispondi OK

						}

						// TODO: ora aspetta gli OK! non puoi farlo qua ma lo fai in un altro CASE "OK": setti nuovo stato "WAIT_OK" tipo, e poi gestisci sotto
						// usa il count settato prima da PowerBoost, quando aveva mandato la prima richiesta boost
						// if (numero di OK ricevuti == powerBoostObject.getCaseAttive) allora OK

					}
					// se sta usando il boost, non risponde e accoda la richiesta
					else if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.USING))
					{
						// FIXME: remove print
						System.out.println("{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da " + senderId + ": sto usando il boost e quindi accodo la richiesta");

						powerBoostObject.accodaRichiesta(senderId);
					}
					// se non sta usando la risorsa e non e' interessato, manda indietro OK
					else if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.NOT_INTERESTED))
					{
						// FIXME: remove print
						System.out.println("{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da " + senderId + ": io non sono interessato al boost e quindi rispondo OK");

						// risponde OK
						boostMessageSender = new MessageSenderThread(casaId, senderId, senderIp, senderPort, new P2PMessage(casaId, casaBoostPort, senderId, "OK"));
						boostMessageSender.start();
					}
					break;

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
