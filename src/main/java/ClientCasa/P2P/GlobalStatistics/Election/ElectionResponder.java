package ClientCasa.P2P.GlobalStatistics.Election;

import ClientCasa.P2P.Message.MessageResponder;
import ClientCasa.P2P.Message.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import Shared.Configuration;
import Shared.Http;
import Shared.MessageSenderThread;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectionResponder implements MessageResponder
{
	private static final Logger LOGGER = Logger.getLogger(ElectionResponder.class.getName());
	private String casaId;
	private Election electionObject;

	public ElectionResponder(Election electionObject)
	{
		this.casaId = Configuration.CASA_ID;
		this.electionObject = electionObject;

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		for (Handler handler : LOGGER.getHandlers()) { LOGGER.removeHandler(handler);}
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public void respond(P2PMessage electionMessage)
	{
		MessageSenderThread electionMessageSender;
		Condominio condominio;
		String senderIp, senderId;
		int senderPort;
		String[] coordInfo;

		try
		{
			// gli arriva il messaggio P2P dal dispatcher
			senderId = electionMessage.getSenderId();
			senderIp = electionMessage.getSenderIp();
			senderPort = electionMessage.getSenderPort();

			LOGGER.log(Level.FINE, "P2P Message received from " + senderId + ": " + electionMessage.getMessage());

			// decide cosa fare in base al messaggio:
			switch(electionMessage.getMessage())
			{
				// e' stato contattato perche' ha ID maggiore di qualcuno: contatta i superiori a sua volta / risponde se e' lui il coord
				case "ELECTION":
					// se e' lui il coord vuol dire che e' entrata una nuova casa e vuole sapere chi e' il coord: gli risponde
					if(electionObject.getState().equals(Election.ElectionState.COORD))
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Ricevuto msg ELECTION da " + senderId + ": sono io il coord e glielo dico");

						// risponde ELECTED: informa che e' lui il coord
						electionMessageSender = new MessageSenderThread(casaId, senderIp, senderPort, new P2PMessage(casaId, Configuration.CASA_PORT, "ELECTED", "ELECTION"));
						electionMessageSender.start();

						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Risposto a " + senderId + " che sono io il coord");
					}
					// se invece anche secondo lui non c'e' un coord, allora ci sara' da indire elezione veramente: contatta i superiori
					else if(electionObject.getState().equals(Election.ElectionState.NEED_ELECTION))
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Ricevuto msg ELECTION da " + senderId + ": non c'e ancora coord quindi inidico elezione ai superiori");

						// salta step di rispondere OK, tanto uscite sono controllate: passa direttamente a inviare ELECTION ai suoi superiori
						// lista case
						condominio = Http.getCondominio();

						// se si accorge che non ci sono altre case con ID maggiore, si proclama eletto, altrimenti scrive a loro e basta
						int superiori = 0;
						for(Casa c : condominio.getCaselist())
						{
							// qua invece non rimanda il msg anche a se stesso senò va in loop; e poi questa e' la parte vera di elezione, non l'inizio elezione (vedi startElection, codice simile)
							if(c.getId().compareTo(casaId) > 0)
							{
								LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Invio msg elezione al superiore " + c.getId());

								// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
								electionMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getPort(), new P2PMessage(casaId, Configuration.CASA_PORT, "ELECTION", "ELECTION"));
								electionMessageSender.start();
								superiori++;
							}
						}

						// se non c'e' nesusno con ID maggiore del suo, si elegge coordinatore
						if(superiori == 0)
						{
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] sono io quello con id maggiore di tutti: mi proclamo COORD e avviso gli altri");


							// setta lo stato in modo che poi StatsReceiver sa come comportarsi (inviare al server la stat globale (o no))
							electionObject.setState(Election.ElectionState.COORD);

							// manda a tutti il messaggio che e' lui il coord
							condominio = Http.getCondominio();
							for(Casa c : condominio.getCaselist())
							{
								// non lo deve mandare anche a se stesso seno' quando lo riceve si mete NOT_COORD!
								if(c.getId().compareTo(casaId) != 0)
								{
									LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Invio ELECTED a " + c.getId());

									// invia "ELECTED"
									electionMessageSender = new MessageSenderThread(casaId, c.getIp(), c.getPort(), new P2PMessage(casaId, Configuration.CASA_PORT, "ELECTED", "ELECTION"));
									electionMessageSender.start();
								}
							}
						}
						// altrimenti ha finito, non sara' mai il coord
						else
						{
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] non sono il quello con id maggiore e quindi ho finito qua");
						}
					}
					// ultimo caso: esiste gia' un coord ma non e' lui: non fa niente. Il coord rispondera' al nuovo arrivato informandolo
					// OPPURE e' stata indetta una nuova elezione.
					else
					{
						// prova a contattare coord per sapere se e' vivo o serve nuova elezione
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Non sono io il coord e quindi non rispondo a msg di election; coord esiste e gli rispondera' - Provo a contattare coordinatore");

						try
						{
							coordInfo = electionObject.getCoord();

							// invia "COORD_ALIVE" al coordinatore salvato
							electionMessageSender = new MessageSenderThread(casaId, coordInfo[1], Integer.parseInt(coordInfo[2]), new P2PMessage(casaId, Configuration.CASA_PORT, "COORD_ALIVE", "ELECTION"));
							electionMessageSender.start();
						}
						catch(Exception e)
						{
							LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Il coord non e' piu' attivo: invio a tutti rielezione");
							electionObject.coordLeaving();
						}

						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Coord sembra essere online");
					}
					break;

				// caso in cui un coordinatore e' appena stato eletto e avvisa tutti che e' lui
				// OPPURE una nuova casa e' entrata e vuole sapere chi e' il coord (e gli viene risposto).
				// in ogni caso c'e il coord che ti avvisa che e' stato eletto: salvati la info
				case "ELECTED":
					LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Ricevuto msg ELECTED da " + senderId + ": e' lui il coord e quindi mi proclamo NOT_COORD");

					// setta lo stato in modo che poi StatsReceiver sa come comportarsi (inviare al server la stat globale o NO)
					electionObject.setState(Election.ElectionState.NOT_COORD);

					// salva anche ID del coordinatore appena eletto, per poi pingarlo se serve
					electionObject.setCoord(senderId, senderIp, senderPort);

					break;

				// caso in cui coordinatore esce dalla rete: dice a tutti che se ne va e tutti quanti faranno poi nuova elezione
				case "NEED_REELECTION":
					LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Ricevuto msg NEED_REELECTION da " + senderId + ": il vecchio coord e' uscito e quindi serve nuova elezione: mi proclamo NEED_ELECTION");

					// setta lo stato in modo che poi StatsReceiver sa come comportarsi (servira' indire nuova elezione)
					electionObject.setState(Election.ElectionState.NEED_ELECTION);
					break;
				// un not coord vuole sapere se coord e' vivo: se lo e', manda OK; se non lo e' ci sara' nuova elezione
				case "COORD_ALIVE":
					// sono io coord: rispondo OK (verra' ignorato: viene controllato solo se la socket e' aperta
					if(electionObject.getState().equals(Election.ElectionState.COORD))
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Ricevuto ALIVE da " + senderId + ": sono io il coord e sono vivo, mando OK");

						electionMessageSender = new MessageSenderThread(casaId, senderIp, senderPort, new P2PMessage(casaId, Configuration.CASA_PORT, "OK", "ELECTION"));
						electionMessageSender.start();
					}
					// se non sono coord ma mi arriva alive, qualcosa e' andato storto (qualcuno crede che sia coord chi non lo e'): rifa elezioine
					else
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } [ ELECTION ] Ricevuto ALIVE da " + senderId + ": non sono il coord ma qualcuno crede che io lo sia: meglio rifare election");
						electionObject.coordLeaving();
					}
					break;
				// caso in cui ricevi OK da coord dopo che hai chiesto ALIVE: ignora il messaggio
				default:
					break;
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error during election");
			e.printStackTrace();
		}
	}
}
