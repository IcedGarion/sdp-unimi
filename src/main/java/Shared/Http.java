package Shared;

import ClientCasa.CasaApp;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Http
{
	private static final Logger LOGGER = Logger.getLogger(Http.class.getName());

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Metodi per comunicare con il SERVER REST, condivisi anche con AdminApp	//

	/*	RICHIEDE ELENCO CASE	*/
	public static synchronized Condominio getCondominio() throws JAXBException, InterruptedException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(Condominio.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		URL url;
		HttpURLConnection conn;

		Condominio condominio = null;

		try
		{
			// GET /condominio: si aspetta lista xml vuota
			url = new URL(Configuration.SERVER_URL + "/condominio");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			condominio = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());

			assert conn.getResponseCode() == 200;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to connect to Admin Server ( GET " + Configuration.SERVER_URL + "/condominio )");
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

			url = new URL(Configuration.SERVER_URL + "/statisticheGlobali/add");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "application/xml");
			conn.setDoOutput(true);

			// invia MeanMeasurement come xml body
			marshaller.marshal(globalConsumption, conn.getOutputStream());

			assert conn.getResponseCode() == 201 : "GlobalStatistics send failed ( " + conn.getResponseCode() + " " + conn.getResponseMessage() + " )";
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to connect to Admin Server ( GET " + Configuration.SERVER_URL + "/statisticheGlobali )");
			e.printStackTrace();
		}
	}

	/* INVIA STAT LOCALE (identico a stat globale ma URL REST diverso) */
	public static void sendLocalStat(MeanMeasurement localStat, String casaId)
	{
		URL url;
		HttpURLConnection conn;
		JAXBContext jaxbContext;
		Marshaller marshaller;

		try
		{
			jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
			marshaller = jaxbContext.createMarshaller();

			url = new URL(Configuration.SERVER_URL + "/statisticheLocali/add/" + casaId);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "application/xml");
			conn.setDoOutput(true);

			// invia MeanMeasurement come xml body
			marshaller.marshal(localStat, conn.getOutputStream());

			assert conn.getResponseCode() == 201 || conn.getResponseCode() == 204: "LocalStatistics send failed ( " + conn.getResponseCode() + " " + conn.getResponseMessage() + " )";
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed to connect to Admin Server ( GET " + Configuration.SERVER_URL + "/statisticheLocali )");
			e.printStackTrace();
		}
	}

}
