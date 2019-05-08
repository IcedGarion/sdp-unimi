import beans.Casa;
import beans.Condominio;

import static org.junit.Assert.*;

import org.junit.Before;
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
	private static final String URL = "http://localhost:1337";

	private Socket clientSocket;
	private BufferedReader inFromServer;
	private DataOutputStream outToServer;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private Unmarshaller jaxbUnmarshaller;
	private URL url;
	private HttpURLConnection conn;

	@Before
	public void connections() throws JAXBException
	{
		// setup marshaller
		jaxbContext = JAXBContext.newInstance(Condominio.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	}


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

	// serve per containsInAnyOrder
	private ArrayList<Casa> uniqueElements(List<Casa> arg)
	{
		ArrayList<Casa> ret = new ArrayList<>();

		for(Casa c: arg)
		{
			if(!ret.contains(c))
				ret.add(c);
		}

		return ret;
	}

	//mini-funzione per check array uguali
	private boolean containsInAnyOrder(List<Casa> expected, List<Casa> actual)
	{
		// check size
		assertEquals(expected.size(), actual.size());
		// check size elementi unici
		assertEquals(uniqueElements(expected).size(), uniqueElements(actual).size());

		// check elemento x elemento
		int i = 0;
		for(Casa c: actual)
		{
			if(expected.contains(c))
				i++;
			else
				return false;
		}

		return true;
	}


	@Test
	public void testCondominio() throws JAXBException, IOException
	{
		try
		{
/*
			// GET /condominio: si aspetta lista xml vuota
			url = new URL(URL + "/condominio");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());
			assertEquals(0, c.getCaselist().size());



			HttpURLConnection urlConnection = (HttpURLConnection) new URL(URL + "/condominio/add").openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("content-type", "application/xml");
			marshaller.marshal(new Casa("CasaTest2"), urlConnection.getOutputStream());

*/
			System.out.println("asdasdasdad");

			// POST /condominio/add: inserisce nuova casa
			url = new URL(URL + "/condominio/add");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("content-type", "application/xml");
			conn.setDoOutput(true);


			marshaller.marshal(new Casa("CasaTest2"), conn.getOutputStream());

			System.out.println(conn.getResponseMessage());
			System.out.println(conn.getResponseCode());
/*
			// GET /condominio: si aspetta 1 Casa test appena inserita
			url = new URL(URL + "/condominio");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());
			System.out.println(c);
			assertTrue(containsInAnyOrder(new ArrayList<Casa>(){{add(new Casa("test"));}}, c.getCaselist()));
*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
