package ClientCasa.P2p.Statistics;

import ClientCasa.CasaApp;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	UN SERVER CONCORRENTE CHE GESTISCE LE RICHIESTE;
	POI riceve statistiche locali (check che riceve da TUTTE le case presenti), calcola consumo globale e stampa

	IL THREAD LANCIATO, StatsReceiverThread, salva le statistiche ricevute in CondominioStats;
 */
public class StatsReceiverServerThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(StatsReceiverServerThread.class.getName());
	private static final int DELAY = 100;
	private String casaId;
	private int statsPort;

	public StatsReceiverServerThread(String casaId, int statsPort) throws JAXBException
	{
		this.casaId = casaId;
		this.statsPort = statsPort;
	}

	// server concorrente
	public void run()
	{
		ServerSocket welcomeSocket;
		Socket connectionSocket;
		URL url;
		HttpURLConnection conn;
		Condominio condominio;

		// prepara oggetto condiviso (fra lui e i StatsReceiverThread) che contiene le stat ricevute
		CondominioStats condominioStats = new CondominioStats();

		try
		{
			// prepara roba per connettersi e prendere il condominio (check se arrivano stats da TUTTE le case attive)
			JAXBContext jaxbContext = JAXBContext.newInstance(Condominio.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(statsPort);

			// delega a nuovo thread la gestione della connessione in arrivo e si rimette in ascolto
			while(true)
			{
				connectionSocket = welcomeSocket.accept();
				StatsReceiverThread receiver = new StatsReceiverThread(connectionSocket, casaId, condominioStats);
				receiver.start();

				// da il tempo al thread di gestire la richiesta e salvare la statistica ricevuta
				Thread.sleep(DELAY);

				// check se sono arrivate le statistiche da tutte le case
				// scarica condominio
				LOGGER.log(Level.INFO, "{ " + casaId + " } Requesting condominio...");
				condominio = CasaApp.getCondominio();

				// confronta le stat ricevute fin ora con le case registrate (ci sono tutte per questo giro?)
				if(condominio.getCaselist().size() == condominioStats.size())
				{
					boolean ciSonoTutte = true;

					// check se anche gli id corrispondono
					for(Casa casa: condominio.getCaselist())
					{
						if(! condominioStats.contains(casa.getId()))
						{
							ciSonoTutte = false;
							break;
						}
					}

					// se ci sono tutte: calcola consumo complessivo e resetta le stat ricevute fin ora (pronto per prossimo giro di calcolo complessivo)
					if(ciSonoTutte)
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } received all statistics");

						// stampa consumo complessivo
						double tot = 0;
						long timestamp = 0;
						int n = 0;
						for(MeanMeasurement measurement: condominioStats.getMeasurements())
						{
							tot += measurement.getMean();

							if(measurement.getEndTimestamp() > timestamp)
								timestamp = measurement.getEndTimestamp();

							n++;
						}
						System.out.println("Consumo globale condominiale aggiornato a " + new Timestamp(timestamp) + ": " + tot + " (" + n + " misure)");

						// azzera per ricominziare il prossimo giro di calcolo complessivo
						condominioStats.resetStats();



						// TODO: ora.... chi manda sto consumo globale al server? XD
						// elezione per decidere chi manda il consumo al server / l'eletto invia / se non sei eletto non fai niente
					}
				}
				// se manca ancora qualche casa a questo giro di loop, attende il prossimo (non fa niente)

				// FIXME: e se dopo molti giri ancora manca qualche casa? (qua andrebbe avanti all'infinito...)
				// fare qualche prova per vedere se non si intoppa; volendo si puo' aggiungere qua un timeout tipo...
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error while receiving stats");
			e.printStackTrace();
		}
	}
}
