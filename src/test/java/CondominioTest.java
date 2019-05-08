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
	private boolean containsExactlyInAnyOrder(List<Casa> expected, List<Casa> actual)
	{
		try
		{
			// check size
			assertEquals(expected.size(), actual.size());
			// check size elementi unici
			assertEquals(uniqueElements(expected).size(), uniqueElements(actual).size());

			// check elemento x elemento
			int i = 0;
			for(Casa c : actual)
			{
				if(expected.contains(c))
					i++;
				else
					return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	//mini-funzione per check array uguali
	private boolean containsInAnyOrder(List<Casa> expected, List<Casa> actual)
	{
		try
		{
			// check elemento x elemento
			int i = 0;
			for(Casa c : actual)
			{
				if(expected.contains(c))
					i++;
				else
					return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}



	@Test
	public void testCondominioEmpty() throws JAXBException, IOException
	{
		try
		{
			// GET /condominio: si aspetta lista xml vuota
			url = new URL(URL + "/condominio");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());

			assertEquals(0, c.getCaselist().size());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testCondominioPost1() throws IOException, JAXBException
	{
		Casa newCasa = new Casa("CasaTestPost");

		// POST /condominio/add: inserisce nuova casa
		url = new URL(URL + "/condominio/add");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		// invia casa come xml body
		marshaller.marshal(newCasa, conn.getOutputStream());

		// GET /condominio: si aspetta 1 Casa test appena inserita
		url = new URL(URL + "/condominio");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());

		assertTrue(containsInAnyOrder(new ArrayList<Casa>(){{add(newCasa);}}, c.getCaselist()));
	}
}
