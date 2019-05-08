import beans.Casa;
import beans.Condominio;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CondominioTest
{
	private static final String HOST = "localhost";
	private static final int PORT = 1337;

	private Socket clientSocket;
	private BufferedReader inFromServer;
	private DataOutputStream outToServer;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller jaxbUnmarshaller;
	private URL url;
	private HttpURLConnection conn;

	/*
	private static String post(String url, Casa obj) throws IOException, JAXBException
	{
		HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
		urlConnection.setRequestMethod("POST");
		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

		marshaller.marshal(obj, urlConnection.getOutputStream());
		// System.out.println(urlConnection.getResponseCode());
		System.out.println(urlConnection.getResponseMessage());

		return urlConnection.getResponseMessage();
	}
	*/

	@Test
	public void testCondominio() throws JAXBException, IOException
	{
		try
		{

			// GET /condominio: si aspetta lista xml vuota
			url = new URL("http://localhost:1337/condominio");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// setup marshaller
			jaxbContext = JAXBContext.newInstance(Condominio.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());
			assertEquals(0, c.getCaselist().size());
/*

			// fin qua tutto ok, condominio restituito correttamente... testare caso VUOTO e poi POST








			// POST /condominio/add: inserisce nuova casa
			// crea oggetto da inserire
			Casa newCasa = new Casa("CasaTest");


			//response = post(HOST + ":" + PORT + "/condominio/add", c);

			// UNMARSHAL + CHECK: REST ritorna la casa appena inserita
			outToServer.writeBytes("/condominio" + '\n');
			c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());


			for(Casa casa: c.getCaselist())
			{
				assertEquals(new Casa("test"), casa);
			}

			//Assert.assertEquals(expected, c);

*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
