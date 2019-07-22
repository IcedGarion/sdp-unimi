import ServerREST.beans.Casa;
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
import java.net.*;

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
		MeanMeasurement computedMeasure = new MeanMeasurement("casa0", 0, 1, 2);
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
			assertEquals(computedMeasure.getMean(), m.getMean(), 0.01);
			assertEquals(computedMeasure.getBeginTimestamp(), m.getBeginTimestamp());
			assertEquals(computedMeasure.getEndTimestamp(), m.getEndTimestamp());
		}
	}

	@Test
	public void simpleNewCasaStatisticheTest() throws IOException, JAXBException
	{
		// registra una nuova casa al condominio
		jaxbContext = JAXBContext.newInstance(Casa.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		Casa newCasa = new Casa("CasaTestStatisticheLocali", "127.0.0.1", 8081);

		url = new URL(URL + "/condominio/add");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		marshaller.marshal(newCasa, conn.getOutputStream());
		assertEquals(conn.getResponseCode(), 201);


		// manda una nuova misurazione per la casa appena creata
		jaxbContext = JAXBContext.newInstance(CasaMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		MeanMeasurement computedMeasure = new MeanMeasurement("CasaTestStatisticheLocali", 1, 1, 1);
		CasaMeasurement retrievedMeasurements;

		url = new URL(URL + "/statisticheLocali/add/CasaTestStatisticheLocali");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);
		marshaller.marshal(computedMeasure, conn.getOutputStream());
		assertEquals(201, conn.getResponseCode());


		// check se, ritornando tutte le statistiche, ritrova oggetto di partenza
		url = new URL(URL + "/statisticheLocali/get/CasaTestStatisticheLocali/1");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		retrievedMeasurements = (CasaMeasurement) jaxbUnmarshaller.unmarshal(conn.getInputStream());
		for(MeanMeasurement m: retrievedMeasurements.getMeasurementList())
		{
			assertEquals(computedMeasure.getMean(), m.getMean(), 0.01);
			assertEquals(computedMeasure.getBeginTimestamp(), m.getBeginTimestamp());
			assertEquals(computedMeasure.getEndTimestamp(), m.getEndTimestamp());
		}

		// aggiunge altre statistiche alla stessa casa
		MeanMeasurement computedMeasure2 = new MeanMeasurement("CasaTestStatisticheLocali", 200, 200, 200);

		url = new URL(URL + "/statisticheLocali/add/CasaTestStatisticheLocali");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);
		marshaller.marshal(computedMeasure2, conn.getOutputStream());
		assertEquals(204, conn.getResponseCode());

		// check se, ritornando tutte le statistiche, ritrova oggetto di partenza
		url = new URL(URL + "/statisticheLocali/get/CasaTestStatisticheLocali/2");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		retrievedMeasurements = (CasaMeasurement) jaxbUnmarshaller.unmarshal(conn.getInputStream());

		assertEquals(2, retrievedMeasurements.getMeasurementList().size());
		assertEquals(computedMeasure.getMean(), retrievedMeasurements.getMeasurementList().get(0).getMean(), 0.01);
		assertEquals(computedMeasure.getBeginTimestamp(), retrievedMeasurements.getMeasurementList().get(0).getBeginTimestamp());
		assertEquals(computedMeasure.getEndTimestamp(), retrievedMeasurements.getMeasurementList().get(0).getEndTimestamp());
		assertEquals(computedMeasure2.getMean(), retrievedMeasurements.getMeasurementList().get(1).getMean(), 0.01);
		assertEquals(computedMeasure2.getBeginTimestamp(), retrievedMeasurements.getMeasurementList().get(1).getBeginTimestamp());
		assertEquals(computedMeasure2.getEndTimestamp(), retrievedMeasurements.getMeasurementList().get(1).getEndTimestamp());
	}

	@Test
	public void EmptyStatsTest() throws IOException, JAXBException
	{
		// chiede stats per una casa che non esiste
		url = new URL(URL + "/statisticheLocali/get/CasaCheNonEsiste/10");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 404);
	}
}
