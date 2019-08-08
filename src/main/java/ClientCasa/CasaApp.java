package ClientCasa;

import ClientCasa.P2P.Boost.PowerBoost;
import ClientCasa.P2P.Boost.PowerBoostThread;
import ClientCasa.P2P.Statistics.Election.Election;
import ClientCasa.P2P.Statistics.Election.ElectionThread;
import ClientCasa.P2P.Statistics.StatsReceiverThread;
import ClientCasa.Statistics.smartMeter.SmartMeterSimulator;
import ClientCasa.Statistics.MeanThread;
import ClientCasa.Statistics.SimulatorBuffer;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CasaApp
{
	public static final String SERVER_URL = "http://localhost:1337";

	private static final String CASA_ID = "casa1";
	private static final String CASA_IP = "localhost";
	private static final int CASA_STATS_PORT = 8081;
	private static final int CASA_ELECTION_PORT = 8091;
	private static final int CASA_BOOST_PORT = 8071;

	private static final int RETRY_TIMEOUT = 250;
	private static final int SIMULATOR_DELAY = 100;
	private static final Logger LOGGER = Logger.getLogger(CasaApp.class.getName());


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Metodi per comunicare con il SERVER REST	//

	/*	RICHIEDE ELENCO CASE	*/
	public static synchronized Condominio getCondominio() throws JAXBException, InterruptedException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(Condominio.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		URL url;
		HttpURLConnection conn;

		Condominio condominio;
		while(true)
		{
			try
			{
				// GET /condominio: si aspetta lista xml vuota
				url = new URL(SERVER_URL + "/condominio");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				condominio = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());

				assert conn.getResponseCode() == 200;
				break;
			}
			catch(Exception e)
			{
				LOGGER.log(Level.WARNING, "Failed to connect to Admin Server ( GET " + SERVER_URL + "/condominio )");
				Thread.sleep(RETRY_TIMEOUT);
			}
		}

		return condominio;
	}

	/*	INVIA STAT GLOBALI */
	public static void sendGlobalStat(MeanMeasurement globalConsumption)
	{
		URL url;
		HttpURLConnection conn;
		JAXBContext jaxbContext;
		Marshaller marshaller;

		try
		{
			jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
			marshaller = jaxbContext.createMarshaller();

			url = new URL(SERVER_URL + "/statisticheGlobali/add");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "application/xml");
			conn.setDoOutput(true);

			// invia casa come xml body
			marshaller.marshal(globalConsumption, conn.getOutputStream());

			assert conn.getResponseCode() == 201 : "CasaApp: Global stat send failed ( " + conn.getResponseCode() + " " + conn.getResponseMessage() + " )";
		}
		catch(Exception e)
		{
			LOGGER.log(Level.WARNING, "Failed to connect to Admin Server ( GET " + SERVER_URL + "/statisticheGlobali )");
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	PROGRAMMA PRINCIPALE CASE	//
	public static void main(String[] args) throws JAXBException, InterruptedException, IOException
	{
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Started Casa Application with ID " + CASA_ID);

		//////////////
		/*	SETUP	*/
		JAXBContext jaxbContext = JAXBContext.newInstance(Condominio.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		URL url;
		HttpURLConnection conn;


		///////////////////////////////////////////////////////////
		/*	AVVIA THREAD SIMULATORE / SMART METER + MEAN THREAD	*/
		SimulatorBuffer myBuffer = new SimulatorBuffer();
		SmartMeterSimulator simulator = new SmartMeterSimulator(myBuffer);
		simulator.start();
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Smart meted launched");

		// avvia thread che invia periodicamente le medie
		MeanThread mean = new MeanThread(myBuffer, CASA_ID, SIMULATOR_DELAY);
		mean.start();
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Local statistic thread launched");


		///////////////////////////////////////////////////
		/*	REGISTRA LA CASA AL SERVER AMMINISTRATORE	*/
		Casa myCasa = new Casa(CASA_ID, CASA_IP, CASA_STATS_PORT, CASA_ELECTION_PORT, CASA_BOOST_PORT);

		// POST /condominio/add: inserisce nuova casa
		// continua a tentare di connettersi al server, se non riesce riprova
		while(true)
		{
			try
			{
				url = new URL(SERVER_URL + "/condominio/add");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("content-type", "application/xml");
				conn.setDoOutput(true);

				// invia casa come xml body
				marshaller.marshal(myCasa, conn.getOutputStream());

				assert conn.getResponseCode() == 201: "CasaApp: Condominio register failed ( " + conn.getResponseCode() + " " + conn.getResponseMessage() + " )";
				LOGGER.log(Level.INFO, "Casa registered to Admin Server with code: " + conn.getResponseCode() + " " + conn.getResponseMessage());
				break;
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "Failed to connect to Admin Server ( POST " + SERVER_URL + "/condominio/add )");
				Thread.sleep(RETRY_TIMEOUT);
			}
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*	PARTE RETE P2P	*/
		// Prepara thread elezione e oggetto condiviso Election da passargli; lo passa anche a statsReceiver perche' deve sapere stato elezione
		// ( per sapere chi e' coord e quindi chi manda le stats)
		Election election = new Election(CASA_ID, CASA_ELECTION_PORT);

		// lancia thread "ascoltatore" elezione bully: riceve msg e risponde a dovere secondo alg BULLY
		ElectionThread electionThread = new ElectionThread(CASA_ID, CASA_ELECTION_PORT, election);
		electionThread.start();

		// lancia thread che riceve le statistiche
		StatsReceiverThread statsReceiver = new StatsReceiverThread(CASA_ID, CASA_STATS_PORT, election);
		statsReceiver.start();

		// lancia thread che riceve richieste di power boost e si coordina
		PowerBoost powerBoostState = new PowerBoost(CASA_ID, CASA_BOOST_PORT);
		PowerBoostThread powerBoostThread = new PowerBoostThread(CASA_ID, CASA_BOOST_PORT, powerBoostState);
		powerBoostThread.start();

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*	INTERFACCIA CLI BOOST + EXIT	*/
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String choice;
		while(true)
		{
			System.out.println("0) POWER BOOST\n1) EXIT\n");
			choice = input.readLine();

			// EXIT
			// termina tutti i thread + informa il server che la casa sta per uscire
			if(choice.equals("1"))
			{
				// post per cancellare la casa
				url = new URL(SERVER_URL + "/condominio/delete");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("content-type", "application/xml");
				conn.setDoOutput(true);
				marshaller.marshal(myCasa, conn.getOutputStream());
				assert conn.getResponseCode()== 204: "Error in removing casa " + CASA_ID;


				// se sta per uscire ed era il coordinatore delle stat globali, dice a tutti che molla (nuova elezione poi)
				if(election.getState().equals(Election.ElectionOutcome.COORD))
				{
					election.coordLeaving();
				}

				// termina i suoi thread
				// TODO: check se va terminato prima un thread di un altro (election / stat receiver? )
				mean.interrupt();
				simulator.interrupt();
				statsReceiver.interrupt();
				electionThread.interrupt();

				LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Stopping...");
				break;
			}
			// POWER BOOST
			else if(choice.equals("0"))
			{
				powerBoostState.requestPowerBoost();
				LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Power boost requested... (Please wait)");
			}
			else
			{
				System.out.println("Inserire 0/1");
			}
		}

		// Terminano anche tutti i thread lanciati: SmartMeterSimulator, MeanThread, e P2pThread
		System.exit(0);
	}
}
