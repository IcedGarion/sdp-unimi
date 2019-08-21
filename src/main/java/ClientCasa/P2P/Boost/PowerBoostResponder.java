package ClientCasa.P2P.Boost;

import ClientCasa.P2P.Message.MessageResponder;
import ClientCasa.P2P.Message.P2PMessage;
import Shared.Configuration;
import Shared.MessageSenderThread;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PowerBoostResponder implements MessageResponder
{
	private static final Logger LOGGER = Logger.getLogger(PowerBoostResponder.class.getName());
	private String casaId;
	private PowerBoost powerBoostObject;

	public PowerBoostResponder(PowerBoost powerBoostState)
	{
		this.casaId = Configuration.CASA_ID;
		this.powerBoostObject = powerBoostState;

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		for (Handler handler : LOGGER.getHandlers()) { LOGGER.removeHandler(handler);}
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	// RICART & AGRAWALA
	public void respond(P2PMessage boostMessage)
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		MessageSenderThread boostMessageSender;
		String senderId, senderIp;
		int senderPort;
		long senderTimestamp, myMessageTimestamp;

		try
		{
			// legge e prepara campi msg ricevuto
			senderId = boostMessage.getSenderId();
			senderIp = boostMessage.getSenderIp();
			senderPort = boostMessage.getSenderPort();
			senderTimestamp = boostMessage.getTimestamp();

			LOGGER.log(Level.INFO, "P2P Message received from " + senderId + ": " + boostMessage.getMessage());

			switch(boostMessage.getMessage())
			{
				// qualcuno ha richesto power boost: 3 casi (stato)
				case "BOOST":
					// anche lui ha richiesto (o SOLO lui): decide se usare BOOST e accodare gli altri, oppure se rinunciare e rispondere OK
					if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.REQUESTED))
					{
						// confronta timestamp della sua richiesta con quella di chi lo ha mandato (usa info salvate)
						myMessageTimestamp = powerBoostObject.getMessageTimestamp();

						// se la mia richiesta esiste (timestamp salvato != -1) ed e' piu vecchia, vado io: accoda l'altra richiesta e auto-invia OK
						if(myMessageTimestamp != -1 && myMessageTimestamp <= senderTimestamp)
						{
							// se l'altra richiesta era la MIA, non la accodo (perche' sto per usare boost: non voglio auto mandarmi OK una volta finito)
							if(! senderId.equals(casaId))
							{
								LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da " + senderId + ": timestamp della mia richiesta e' piu' vecchio, quindi vado io (aspetto OK) e accodo l'altra richiesta");

								// accoda l'altra richiesta... Poi ricevera' l'ok
								powerBoostObject.accodaRichiesta(senderId, senderIp, senderPort);
							}
							// altrimenti non accoda, perche' sono io
							else
							{
								LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da me stesso: non faccio niente e aspetto OK degli altri");
							}
						}
						// se chi ha mandato msg e' piu' vecchia, va l'altro: OK
						else
						{
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da " + senderId + ": timestamp della mia richiesta e' piu' recente, quindi rinuncio e rispondo OK");

							// risponde OK
							boostMessageSender = new MessageSenderThread(casaId, senderIp, senderPort, new P2PMessage(casaId, Configuration.CASA_PORT, "OK", "BOOST"));
							boostMessageSender.start();
						}
					}
					// se sta usando il boost, non risponde e accoda la richiesta
					else if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.USING))
					{
						// se riceve la sua stessa richiesta (in ritardo) quando ha gia' ottenuto boost: la ignora, non accora
						if(senderId.equals(casaId))
						{
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST in ritardo da me stesso: lo ignoro");
						}

						LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da " + senderId + ": sto usando il boost e quindi accodo la richiesta");

						powerBoostObject.accodaRichiesta(senderId, senderIp, senderPort);
					}
					// se non sta usando la risorsa e non e' interessato, manda indietro OK
					else if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.NOT_INTERESTED))
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg BOOST da " + senderId + ": io non sono interessato al boost e quindi rispondo OK");

						// risponde OK
						boostMessageSender = new MessageSenderThread(casaId, senderIp, senderPort, new P2PMessage(casaId, Configuration.CASA_PORT,"OK", "BOOST"));
						boostMessageSender.start();
					}
					break;

				// UNICO CASO IN CUI OTTIENI BOOST: ottieni abbastanza OK da tutti
				case "OK":
					// gli OK sono da considerare soltanto se hai fatto la richiesta (REQUESTED)
					if(powerBoostObject.getState().equals(PowerBoost.PowerBoostState.REQUESTED))
					{
						// aumenta contatore degli OK nell'oggetto condiviso (stato)
						powerBoostObject.incrOKCount();

						// usa il count settato all'inizio da PowerBoost, quando aveva mandato la prima richiesta boost: numero case attive in quel momento
						// si aspetta di ricevere tanti OK quante le case attive quando aveva mandato la richiesta (meno 2: POSSONO USARE IL BOOST IN 2)
						if(powerBoostObject.getOKCount() == powerBoostObject.getCaseAttive()-2)
						{
							// OK DA TUTTI: OTTIENE IL BOOST!
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg OK da " + senderId + ": ottenuti tutti gli OK necessari: USA BOOST!");

							// setta stato, cosi' se riceve altre richieste BOOST nel frattempo, le accodera' (in beginPowerBoost)

							// chiama metodo simulatore per fare effettivamente POWER BOOST
							powerBoostObject.beginPowerBoost();
						}
						else
						{
							// Non ha ancora ricevuto tutti gli OK necessari: non fa piu' niente... aspetta il prossimo
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg OK da " + senderId + ": in attesa di altri OK (" + powerBoostObject.getOKCount() + " / " + (powerBoostObject.getCaseAttive()-2) + ")");
						}
					}
					// ricevi OK ma non hai fatto richiesta (NOT_INTERESTED) o stai gia' usando (USING): lo scarti
					// non ti interessa perche' tanto bastavano gia' gli OK che hai ricevuto (ce n'e' uno in piu' di scarto, dato che 2 case possono usare boost)
					else
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ BOOST ] Ricevuto msg OK da " + senderId + ": ho gia' ricevuto abbastanza OK quindi lo scarto");
					}
					break;
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error during power boost messages");
			e.printStackTrace();
		}
	}
}