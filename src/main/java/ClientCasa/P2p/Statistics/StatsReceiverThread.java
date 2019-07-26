package ClientCasa.P2p.Statistics;

import ClientCasa.CasaApp;
import ClientCasa.P2p.Statistics.Election.Election;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	UN SERVER CONCORRENTE CHE GESTISCE LE RICHIESTE;
	Riceve statistiche locali (check che riceve da TUTTE le case presenti), calcola consumo globale e stampa

	IL THREAD LANCIATO, StatsReceiverWorkerThread, salva le statistiche ricevute in CondominioStats;
 */
public class StatsReceiverThread extends Thread
{
	// stato elezione lo trova nell'oggetto election condiviso:
	// all'inizio nessun coordinatore eletto -> "NEED_ELECTION"
	// dopo una elezione uno puo' essere -> "COORD" / "NOT_COORD"

	private static final Logger LOGGER = Logger.getLogger(StatsReceiverThread.class.getName());
	private static final int DELAY = 100;
	private String casaId;
	private int statsPort;
	private Election election;

	// riceve anche oggetto Elezione con info su chi e' l'eletto e su come eleggerne un altro
	public StatsReceiverThread(String casaId, int statsPort, Election election)
	{
		this.casaId = casaId;
		this.statsPort = statsPort;
		this.election = election;
	}

	// server concorrente
	public void run()
	{
		ServerSocket welcomeSocket;
		Socket connectionSocket;
		Condominio condominio;
		StatsReceiverWorkerThread receiver;

		// prepara oggetto condiviso (fra lui e i StatsReceiverWorkerThread) che contiene le stat ricevute
		CondominioStats condominioStats = new CondominioStats();


		try
		{
			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(statsPort);

			// delega a nuovo thread la gestione della connessione in arrivo e si rimette in ascolto
			while(true)
			{
				connectionSocket = welcomeSocket.accept();
				receiver = new StatsReceiverWorkerThread(connectionSocket, casaId, condominioStats);
				receiver.start();
				LOGGER.log(Level.FINE, "{ " + casaId + " } Received connection for Statistics: launching worker thread");

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
						LOGGER.log(Level.INFO, "{ " + casaId + " } received all Statistics");

						double globalTot = 0;
						long timestampMin = Long.MAX_VALUE, timestampMax = Long.MIN_VALUE;
						int n = 0;
						MeanMeasurement globalComsumption;

						// calcolo consumo complessivo
						for(MeanMeasurement measurement: condominioStats.getMeasurements())
						{
							globalTot += measurement.getMean();

							if(measurement.getEndTimestamp() > timestampMax)
								timestampMax = measurement.getEndTimestamp();
							if(measurement.getBeginTimestamp() < timestampMin)
								timestampMin = measurement.getBeginTimestamp();

							n++;
						}
						System.out.println("Consumo globale condominiale aggiornato a " + new Timestamp(timestampMax) + ": " + globalTot + " (" + n + " misure)");
						globalComsumption = new MeanMeasurement("Condominio", globalTot, timestampMin, timestampMax);

						// azzera per ricominziare il prossimo giro di calcolo complessivo
						condominioStats.resetStats();



						////////////////////////////////////////////////////////////////////////////////////////////////
						//	ELEZIONE / INVIO STATISTICHE GLOBALI AL SERVER
						// inizialmente non c'e' nessun coordinatore e si indice elezione;
						// se invece, questa casa si e' unita dopo e il coord c'e' gia': fa comunque startElection
						// il suo ElectionThread ricevera' un msg dal coord dicendogli che esiste, e il thread settera' lo stato di election a NOT_COORD
						// elezione parte soltanto se "tutti" sono in NEED_ELECTION, cioè all'inizio, oppure quando esce coord
						if(election.getState().equals(Election.ElectionOutcome.NEED_ELECTION))
						{
							// FIXME: remove print
							System.out.println("{ " + casaId + " } [ STATSRECEIVER ] Serve elezione");

							election.startElection();
						}
						// se c'e gia'/appena stata elezione e sei tu coord, invia le statistiche al server
						else if(election.getState().equals(Election.ElectionOutcome.COORD))
						{
							// FIXME: remove print
							System.out.println("{ " + casaId + " } [ STATSRECEIVER ] Sono io il coord e sto mandando le stat globali al server");

							// Manda la stat globale appena calcolata al server rest tramite metodo in CasaApp (conosce lei info su server)
							CasaApp.sendGlobalStat(globalComsumption);

							// FIXME: remove print
							System.out.println("{ " + casaId + " } [ STATSRECEIVER ] Stat globali inviate");
						}
						// se invece non e' il primo giro (need election) / la casa non si e' appena unita (need election)
						// allora in teoria c'e' gia' un coordinatore, e se non e' lui allora non fa piu' niente
						else
						{
							// FIXME: remove print
							System.out.println("{ " + casaId + " } [ STATSRECEIVER ] Non sono io il coord: non mando info al server");
						}

					}
				}
				// se manca ancora qualche casa a questo giro di loop, attende il prossimo (non fa niente)

				// FIXME: e se dopo molti giri ancora manca qualche casa? (qua andrebbe avanti all'infinito...)
				// non dovrebbe mai succedere perche' tutti mandano in ordine;
				// basterebbe anche solo aumentare il delay di attesa per il thread
				// volendo si puo' aggiungere qua un timeout...

				// check TERMINAZIONE
				if(interrupted())
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping StatsReceiverWorkerThread... ");
					return;
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in global stats connection");
			e.printStackTrace();
		}
	}
}
