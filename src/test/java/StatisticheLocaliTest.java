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
import java.net.Socket;
import java.net.URL;

public class StatisticheLocaliTest
{
	private static final String URL = "http://localhost:1337";

	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private URL url;
	private HttpURLConnection conn;

	@Before
	public void connections() throws JAXBException
	{
		// setup marshaller
		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}

	@Test
	public void simpleStatisticheTest() throws IOException, JAXBException
	{
		MeanMeasurement computedMeasure = new MeanMeasurement(0, 1, 2);

		url = new URL(URL + "/statisticheLocali/add/0");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		// invia casa come xml body
		marshaller.marshal(computedMeasure, conn.getOutputStream());

		System.out.println(conn.getResponseMessage() + "\n" + conn.getResponseCode());
	}
}
