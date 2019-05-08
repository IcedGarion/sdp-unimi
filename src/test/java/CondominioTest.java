import beans.Casa;
import beans.Condominio;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
			for(Casa c : actual)
			{
				if(!expected.contains(c))
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
	private boolean containsInAnyOrder(List<Casa> bigList, List<Casa> testList)
	{
		try
		{
			// check elemento x elemento
			for(Casa c : testList)
			{
				if(!bigList.contains(c))
					return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}


	/* ======================================== TEST ======================================================= */
	/* VANNO ESEGUITI TUTTI AD AVVIO SERVER (CIOE', UNA VOLTA ESEGUITO QUESTO FILE, NON SI PUO' ESEGUIRE DI
	   NUOVO UN'ALTRA VOLTA SENZA AVER RIAVVIATO IL SERVER, ALTRIMENTI FALLISCONO (perche' rimarrebbero case)


	    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
	    Garantisce che i test vengono eseguiti in ordine di nome (testA..., testB..., testC)

	*/

	@Test
	public void testA_SimpleGetEmpty() throws JAXBException, IOException
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
	public void testB_SimplePost() throws IOException, JAXBException
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
		assertEquals(conn.getResponseCode(), 201);

		// GET /condominio: si aspetta 1 Casa test appena inserita
		url = new URL(URL + "/condominio");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());

		assertTrue(containsInAnyOrder(c.getCaselist(), new ArrayList<Casa>(){{add(newCasa);}}));


		System.out.println(conn.getResponseCode() + "\n" + conn.getResponseMessage());


		// aggiunge altra casa
		// POST /condominio/add: inserisce nuova casa
		Casa newCasa2 = new Casa("CasaTestPost2");
		url = new URL(URL + "/condominio/add");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		// invia casa come xml body
		marshaller.marshal(newCasa2, conn.getOutputStream());
		assertEquals(conn.getResponseCode(), 201);

		// GET
		url = new URL(URL + "/condominio");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		// check 2 case di prima
		c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());
		assertTrue(containsInAnyOrder(c.getCaselist(), new ArrayList<Casa>(){{add(newCasa); add(newCasa2);}}));
	}


	@Test
	public void testC_SimpleConflictPost() throws IOException, JAXBException
	{
		Casa newCasa = new Casa("CasaConflictTest");

		// POST /condominio/add: inserisce nuova casa
		url = new URL(URL + "/condominio/add");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		// invia casa come xml body
		marshaller.marshal(newCasa, conn.getOutputStream());
		assertEquals(conn.getResponseCode(), 201);

		// identica post casa
		// POST /condominio/add: inserisce nuova casa
		url = new URL(URL + "/condominio/add");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		// invia casa come xml body
		marshaller.marshal(newCasa, conn.getOutputStream());
		assertEquals(conn.getResponseCode(), 409);

		// GET /condominio: si aspetta 1 Casa test appena inserita
		url = new URL(URL + "/condominio");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);

		Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());
		assertEquals(3, c.getCaselist().size());
		assertTrue(containsInAnyOrder(c.getCaselist(), new ArrayList<Casa>(){{add(newCasa);}}));
	}

	@Test
	public void testD_SimpleDelete() throws IOException, JAXBException
	{
		// rimuove una di quelle appena aggiunte dal test su ^
		Casa newCasa = new Casa("CasaConflictTest");

		// POST /condominio/delete
		url = new URL(URL + "/condominio/delete");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);
		// invia casa come xml body
		marshaller.marshal(newCasa, conn.getOutputStream());
		assertEquals(conn.getResponseCode(), 204);


		// GET solita per verificare che lo ha rimosso
		url = new URL(URL + "/condominio");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		assertEquals(conn.getResponseCode(), 200);
		// check 2 case di prima cancellazione
		Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());
		assertEquals(2, c.getCaselist().size());
		assertFalse(containsInAnyOrder(c.getCaselist(), new ArrayList<Casa>(){{add(newCasa);}}));


		// rimuove casa che non esiste piu'
		url = new URL(URL + "/condominio/delete");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type", "application/xml");
		conn.setDoOutput(true);

		// invia casa come xml body
		marshaller.marshal(newCasa, conn.getOutputStream());
		assertEquals(conn.getResponseCode(), 404);
	}
}
