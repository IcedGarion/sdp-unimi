package ClientCasa;

import ClientCasa.P2p.P2PThread;
import ClientCasa.smartMeter.SmartMeterSimulator;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

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
	private static final String SERVER_URL = "http://localhost:1337";

	private static final String CASA_ID = "casa2";
	private static final String CASA_IP = "localhost";
	private static final int CASA_STATS_PORT = 8081;

	private static final int RETRY_TIMEOUT = 250;
	private static final int SIMULATOR_DELAY = 250;
	private static final Logger LOGGER = Logger.getLogger(CasaApp.class.getName());


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

				LOGGER.log(Level.INFO, "Requested Condominio (Case list) from server with code: " + conn.getResponseCode() + " " + conn.getResponseMessage());
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
		Casa myCasa = new Casa(CASA_ID, CASA_IP, CASA_STATS_PORT);

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
				LOGGER.log(Level.WARNING, "Failed to connect to Admin Server ( POST " + SERVER_URL + "/condominio/add )");
				Thread.sleep(RETRY_TIMEOUT);
			}
		}


		// parte rete p2p
		// lancia nuovo thread che si occupa delle statistiche globali
		P2PThread p2p = new P2PThread(CASA_ID, CASA_IP, CASA_STATS_PORT);
		p2p.start();



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

				// termina i suoi thread
				mean.interrupt();
				simulator.interrupt();
				break;
			}
			else if(choice.equals("0"))
			{
				// TODO: to be implemented (power boost)
				System.out.println("To be implemented");
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
