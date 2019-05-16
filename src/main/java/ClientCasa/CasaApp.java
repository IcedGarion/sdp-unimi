package ClientCasa;

import ClientCasa.smartMeter.SmartMeterSimulator;
import ServerREST.beans.Casa;
import ServerREST.beans.CasaMeasurement;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CasaApp
{
	private static final String SERVER_URL = "http://localhost:1337";

	private static final String CASA_ID = "CASA_ID_123123";
	private static final String CASA_IP = "localhost";
	private static final int CASA_PORT = 8081;

	private static final int RETRY_TIMEOUT = 250;
	private static final int SIMULATOR_DELAY = 500;
	private static final Logger LOGGER = Logger.getLogger(CasaApp.class.getName());


	public static void main(String args[]) throws JAXBException, InterruptedException, IOException
	{
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Started Casa Application with ID " + CASA_ID);

		// setup
		JAXBContext jaxbContext = JAXBContext.newInstance(Condominio.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		URL url;
		HttpURLConnection conn;


		// avvia thread simulatore smart meter
		SimulatorBuffer myBuffer = new SimulatorBuffer();
		SmartMeterSimulator simulator = new SmartMeterSimulator(myBuffer);
		simulator.start();
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Smart meted launched");


		// si registra al server amministratore
		Casa newCasa = new Casa(CASA_ID, CASA_IP, CASA_PORT);

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
				marshaller.marshal(newCasa, conn.getOutputStream());

				LOGGER.log(Level.INFO, "Casa registered to Admin Server with code: " + conn.getResponseCode() + " " + conn.getResponseMessage());
				assert conn.getResponseCode() == 201: "CasaApp: Condominio register failed ( " + conn.getResponseCode() + " " + conn.getResponseMessage() + " )";
				break;
			}
			catch(Exception e)
			{
				LOGGER.log(Level.WARNING, "Failed to connect to Admin Server ( POST " + SERVER_URL + "/condominio/add )");
				Thread.sleep(RETRY_TIMEOUT);
			}
		}


		// Richiede elenco case
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


		// avvia thread che invia periodicamente le medie
		MeanThread mean = new MeanThread(myBuffer, CASA_ID);
		mean.start();
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Local statistic thread launched");

		

		// lancia thread che aspetta input per eventuale uscita casa


		// parte rete p2p

		// interfaccia cli per power boost
	}
}
