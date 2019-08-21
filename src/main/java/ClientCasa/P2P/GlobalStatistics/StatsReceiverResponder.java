package ClientCasa.P2P.GlobalStatistics;

import ClientCasa.CasaApp;
import ClientCasa.P2P.GlobalStatistics.Election.Election;
import ClientCasa.P2P.Message.MessageResponder;
import ClientCasa.P2P.Message.P2PMessage;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;
import Shared.Configuration;
import Shared.Http;

import java.sql.Timestamp;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	Riceve statistiche locali (check che riceve da TUTTE le case presenti), salva le statistiche ricevute in CondominioStats
	e calcola consumo globale e stampa
 */
public class StatsReceiverResponder implements MessageResponder
{
	// stato elezione lo trova nell'oggetto election condiviso:
	// all'inizio nessun coordinatore eletto -> "NEED_ELECTION"
	// dopo una elezione uno puo' essere -> "COORD" / "NOT_COORD"

	private static final Logger LOGGER = Logger.getLogger(StatsReceiverResponder.class.getName());
	private static final int DELAY = 100;
	private String casaId;
	private Election election;
	private CondominioStats condominioStats;

	// riceve anche oggetto Elezione con info su chi e' l'eletto e su come eleggerne un altro
	public StatsReceiverResponder(Election election)
	{
		this.casaId = Configuration.CASA_ID;
		this.election = election;

		// prepara oggetto condiviso (fra lui e gli altri thread uguali) che contiene le stat ricevute
		condominioStats = new CondominioStats();

		// logger levels
		LOGGER.setLevel(Configuration.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Configuration.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}

	public void respond(P2PMessage measureMessage)
	{
		Condominio condominio;

		try
		{
			// Gli arriva un messaggio dal dispatcher contenente una MeanMeasure
			LOGGER.log(Level.FINE, "{ " + casaId + " } Statistic received from " + measureMessage.getSenderId());

			// salva la statistica appena ricevuta in un oggetto condiviso, così StatsReceiverResponder poi le legge
			condominioStats.addCasaStat(measureMessage.getMeasure());


			// check se sono arrivate le statistiche da tutte le case
			// scarica condominio
			condominio = Http.getCondominio();

			// confronta le stat ricevute fin ora con le case registrate (ci sono tutte per questo giro?)
			if(condominio.getCaselist().size() <= condominioStats.size())
			{
				boolean ciSonoTutte = true;

				// check se anche gli id corrispondono
				for(Casa casa : condominio.getCaselist())
				{
					if(!condominioStats.contains(casa.getId()))
					{
						ciSonoTutte = false;
						break;
					}
				}

				// se ci sono tutte: calcola consumo complessivo e resetta le stat ricevute fin ora (pronto per prossimo giro di calcolo complessivo)
				if(ciSonoTutte)
				{
					LOGGER.log(Level.FINE, "{ " + casaId + " } received all GlobalStatistics");

					double globalTot = 0;
					long timestampMin = Long.MAX_VALUE, timestampMax = Long.MIN_VALUE;
					int n = 0, tot = condominio.getCaselist().size();
					MeanMeasurement globalComsumption;

					// calcolo consumo complessivo
					for(MeanMeasurement measurement : condominioStats.getMeasurements())
					{
						if(n >= tot)
							break;

						globalTot += measurement.getMean();

						if(measurement.getEndTimestamp() > timestampMax)
							timestampMax = measurement.getEndTimestamp();
						if(measurement.getBeginTimestamp() < timestampMin)
							timestampMin = measurement.getBeginTimestamp();

						n++;
					}
					System.out.println("Consumo globale condominiale aggiornato a " + new Timestamp(timestampMax) + ": " + globalTot + " (" + n + " misure)");
					CasaApp.refreshMenu();

					globalComsumption = new MeanMeasurement("Condominio", globalTot, timestampMin, timestampMax);

					// azzera per ricominziare il prossimo giro di calcolo complessivo
					condominioStats.resetStats();


					////////////////////////////////////////////////////////////////////////////////////////////////
					//	ELEZIONE / INVIO STATISTICHE GLOBALI AL SERVER
					// inizialmente non c'e' nessun coordinatore e si indice elezione;
					// se invece, questa casa si e' unita dopo e il coord c'e' gia': fa comunque startElection
					// il suo ElectionResponder ricevera' un msg dal coord dicendogli che esiste, e il thread settera' lo stato di election a NOT_COORD
					// elezione parte soltanto se "tutti" sono in NEED_ELECTION, cioè all'inizio, oppure quando esce coord
					if(election.getState().equals(Election.ElectionState.NEED_ELECTION))
					{
						LOGGER.log(Level.FINE, "{ " + casaId + " } Serve elezione per inviare la stat globale");

						election.startElection();
					}
					// se c'e gia'/appena stata elezione e sei tu coord, invia le statistiche al server
					else if(election.getState().equals(Election.ElectionState.COORD))
					{
						LOGGER.log(Level.FINE, "{ " + casaId + " } Sono io il coord e sto mandando le stat globali al server");

						// Manda la stat globale appena calcolata al server rest tramite metodo in CasaApp (conosce lei info su server)
						Http.sendGlobalStat(globalComsumption);

						LOGGER.log(Level.FINE, "{ " + casaId + " } Stat globali inviate");
					}
					// se invece non e' il primo giro (need election) / la casa non si e' appena unita (need election)
					// allora in teoria c'e' gia' un coordinatore, e se non e' lui allora non fa piu' niente
					else
					{
						LOGGER.log(Level.FINE, "{ " + casaId + " } Non sono io il coord: non mando stat globale al server");
					}

				}
			}
			// se manca ancora qualche casa a questo giro di loop, attende il prossimo (non fa niente)

			// TODO: e se dopo molti giri ancora manca qualche casa? (qua andrebbe avanti all'infinito...)
			// non dovrebbe mai succedere perche' tutti mandano in ordine;
			// basterebbe anche solo aumentare il delay di attesa per il thread
			// volendo si puo' aggiungere qua un timeout...
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "{ " + casaId + " } Error in receiving global stats / election");
			e.printStackTrace();
		}
	}
}
