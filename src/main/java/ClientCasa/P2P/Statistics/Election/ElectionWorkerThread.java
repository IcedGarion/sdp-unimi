package ClientCasa.P2P.Statistics.Election;

import ClientCasa.CasaApp;
import ClientCasa.P2P.MessageSenderThread;
import ClientCasa.P2P.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectionWorkerThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(ElectionWorkerThread.class.getName());
	private Socket listenSocket;
	private String casaId;
	private int casaElectionPort;
	private Election electionObject;

	public ElectionWorkerThread(Socket listenSocket, String casaId, int casaElectionPort, Election electionObject)
	{
		this.listenSocket = listenSocket;
		this.casaId = casaId;
		this.casaElectionPort = casaElectionPort;
		this.electionObject = electionObject;
	}

	public void run()
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		P2PMessage electionMessage;
		MessageSenderThread electionMessageSender;
		Condominio condominio;
		String senderIp, senderId;
		int senderPort;

		try
		{
			jaxbContext = JAXBContext.newInstance(P2PMessage.class);
			unmarshaller = jaxbContext.createUnmarshaller();


			electionMessage = (P2PMessage) unmarshaller.unmarshal(listenSocket.getInputStream());
			senderId = electionMessage.getSenderId();
			senderIp = listenSocket.getInetAddress().getHostAddress();
			senderPort = electionMessage.getSenderPort();
			listenSocket.close();

			// decide cosa fare in base al messaggio:
			switch(electionMessage.getMessage())
			{
				// e' stato contattato perhe' ha ID maggiore di qualcuno: contatta i superiori a sua volta / risponde se e' lui il coord
				case "ELECTION":
					// se e' lui il coord vuol dire che e' entrata una nuova casa e vuole sapere chi e' il coord: gli risponde
					if(electionObject.getState().equals(Election.ElectionOutcome.COORD))
					{
						// FIXME: remove print
						System.out.println("{ " + casaId + " } [ ELECTION ] Ricevuto msg ELECTION da " + senderId + ": sono io il coord e glielo dico");


						// risponde ELECTED: informa che e' lui il coord
						electionMessageSender = new MessageSenderThread(casaId, senderId, senderIp, senderPort, new P2PMessage(casaId, casaElectionPort, senderId, "ELECTED"));
						electionMessageSender.start();


						// FIXME: remove print
						System.out.println("{ " + casaId + " } [ ELECTION ] Risposto a " + senderId + " che sono io il coord");
					}
					// se invece anche secondo lui non c'e' un coord, allora ci sara' da indire elezione veramente: contatta i superiori
					else if(electionObject.getState().equals(Election.ElectionOutcome.NEED_ELECTION))
					{
						// FIXME: remove print
						System.out.println("{ " + casaId + " } [ ELECTION ] Ricevuto msg ELECTION da " + senderId + ": non c'e ancora coord quindi inidico elezione ai superiori");


						// salta step di rispondere OK, tanto uscite sono controllate: passa direttamente a inviare ELECTION ai suoi superiori
						// lista case
						condominio = CasaApp.getCondominio();

						// se si accorge che non ci sono altre case con ID maggiore, si proclama eletto, altrimenti scrive a loro e basta
						int superiori = 0;
						for(Casa c : condominio.getCaselist())
						{
							// qua invece non rimanda il msg anche a se stesso senò va in loop; e poi questa e' la parte vera di elezione, non l'inizio elezione (vedi startElection, codice simile)
							if(c.getId().compareTo(casaId) > 0)
							{
								// FIXME: remove print
								System.out.println("{ " + casaId + " } [ ELECTION ] Invio msg elezione al superiore " + c.getId());


								// invia "ELECTION": chiede ai superiori di prendersi carico coordinatore
								electionMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getElectionPort(), new P2PMessage(casaId, casaElectionPort, c.getId(), "ELECTION"));
								electionMessageSender.start();
								superiori++;
							}
						}

						// se non c'e' nesusno con ID maggiore del suo, si elegge coordinatore
						if(superiori == 0)
						{
							// FIXME: remove print
							System.out.println("{ " + casaId + " } [ ELECTION ] sono io quello con id maggiore di tutti: mi proclamo COORD e avviso gli altri");


							// setta lo stato in modo che poi StatsReceiver sa come comportarsi (inviare al server la stat globale (o no))
							electionObject.setState(Election.ElectionOutcome.COORD);

							// manda a tutti il messaggio che e' lui il coord
							condominio = CasaApp.getCondominio();
							for(Casa c : condominio.getCaselist())
							{
								// non lo deve mandare anche a se stesso seno' quando lo riceve si mete NOT_COORD!
								if(c.getId().compareTo(casaId) != 0)
								{
									// FIXME: remove print
									System.out.println("{ " + casaId + " } [ ELECTION ] Invio ELECTED a " + c.getId());


									// invia "ELECTED"
									electionMessageSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getElectionPort(), new P2PMessage(casaId, casaElectionPort, c.getId(), "ELECTED"));
									electionMessageSender.start();
								}
							}
						}
						// altrimenti ha finito, non sara' mai il coord
						else
						{
							// FIXME: remove print
							System.out.println("{ " + casaId + " } [ ELECTION ] non sono il quello con id maggiore e quindi ho finito qua");
						}
					}
					// ultimo caso: esiste gia' un coord ma non e' lui: non fa niente. Il coord rispondera' al nuovo arrivato informandolo
					// OPPURE e' stata indetta una nuova elezione.
					else
					{
						System.out.println("{ " + casaId + " } [ ELECTION ] Non sono io il coord e quindi non rispondo a msg di election; coord esiste gia' e gli rispondera'");
					}
					break;

				// caso in cui un coordinatore e' appena stato eletto e avvisa tutti che e' lui
				// OPPURE una nuova casa e' entrata e vuole sapere chi e' il coord (e gli viene risposto).
				// in ogni caso c'e il coord che ti avvisa che e' stato eletto: salvati la info
				case "ELECTED":
					// FIXME: remove print
					System.out.println("{ " + casaId + " } Ricevuto msg ELECTED da " + senderId + ": e' lui il coord e quindi mi proclamo NOT_COORD");


					// setta lo stato in modo che poi StatsReceiver sa come comportarsi (inviare al server la stat globale o NO)
					electionObject.setState(Election.ElectionOutcome.NOT_COORD);
					break;

				// caso in cui coordinatore esce dalla rete: dice a tutti che se ne va e tutti quanti faranno poi nuova elezione
				case "NEED_REELECTION":
					// FIXME: remove print
					System.out.println("{ " + casaId + " } Ricevuto msg NEED_REELECTION da " + senderId + ": il vecchio coord e' uscito e quindi serve nuova elezione: mi proclamo NEED_ELECTION");


					// setta lo stato in modo che poi StatsReceiver sa come comportarsi (servira' indire nuova elezione)
					electionObject.setState(Election.ElectionOutcome.NEED_ELECTION);
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
