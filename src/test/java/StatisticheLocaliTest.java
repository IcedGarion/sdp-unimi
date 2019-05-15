import ServerREST.beans.CasaMeasurement;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class StatisticheLocaliTest
{
	private static final String URL = "http://localhost:1337";

	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller jaxbUnmarshaller;
	private URL url;
	private HttpURLConnection conn;

	@Before
	public void connections() throws JAXBException
	{
		// setup marshaller
		jaxbContext = JAXBContext.newInstance(CasaMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	}

	@Test
	public void simpleEmptyStatisticheTest() throws IOException, JAXBException
	{
		// manda una nuova misurazione per una casa che non esiste ancora
		MeanMeasurement computedMeasure = new MeanMeasurement(0, 1, 2);
		MeanMeasurement retrievedMeasure;
		CasaMeasurement retrievedMeasurements;

		url = new URL(URL + "/statisticheLocali/add/0");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		marshaller.marshal(computedMeasure, conn.getOutputStream());
		assertEquals(201, conn.getResponseCode());

		// check se, ritornando tutte le statistiche, ritrova oggetto di partenza
		url = new URL(URL + "/statisticheLocali/get/0/10");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		retrievedMeasurements = (CasaMeasurement) jaxbUnmarshaller.unmarshal(conn.getInputStream());
		for(MeanMeasurement m: retrievedMeasurements.getMeasurementList())
		{
			assertEquals(computedMeasure.getMean(), m.getMean());
			assertEquals(computedMeasure.getBeginTimestamp(), m.getBeginTimestamp());
			assertEquals(computedMeasure.getEndTimestamp(), m.getEndTimestamp());
		}
	}


	public void simpleNewCasaStatisticheTest() throws IOException, JAXBException
	{
		// registra una nuova casa al condominio




		// manda una nuova misurazione per la casa appena creata
		MeanMeasurement computedMeasure = new MeanMeasurement(0, 1, 2);
		MeanMeasurement retrievedMeasure;

		url = new URL(URL + "/statisticheLocali/add/0");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		marshaller.marshal(computedMeasure, conn.getOutputStream());
		assertEquals(201, conn.getResponseCode());

		// check se, ritornando tutte le statistiche, ritrova oggetto di partenza
		url = new URL(URL + "/statisticheLocali");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		retrievedMeasure = (MeanMeasurement) jaxbUnmarshaller.unmarshal(conn.getInputStream());
		assertEquals(computedMeasure.getMean(), retrievedMeasure.getMean());
		assertEquals(computedMeasure.getBeginTimestamp(), retrievedMeasure.getBeginTimestamp());
		assertEquals(computedMeasure.getEndTimestamp(), retrievedMeasure.getEndTimestamp());
	}
}
