import beans.Casa;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.*;

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


	private String post(String url, Casa obj) throws IOException, JAXBException
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

	@Before
	public void connect() throws IOException, JAXBException
	{
		// socket
		clientSocket = new Socket(HOST, PORT);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		// setup marshaller
		jaxbContext = JAXBContext.newInstance(Casa.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();

	}

	@Test
	public void testAddCasa() throws JAXBException, IOException
	{
		String response;

		// crea oggetto da inserire
		Casa c = new Casa("CasaTest");

		// GET /condominio: si aspetta lista xml vuota
		outToServer.writeBytes("/condominio" + '\n');
		System.out.println("GET");





		// si blocca qua




		response = inFromServer.readLine();
		Assert.assertEquals("", response);

		// POST /condominio/add: inserisce nuova casa
		response = post(HOST + ":" + PORT + "/condominio/add", c);

		// UNMARSHAL + CHECK: REST ritorna la casa appena inserita
		outToServer.writeBytes("/condominio" + '\n');
		Casa expected = (Casa) jaxbUnmarshaller.unmarshal(clientSocket.getInputStream());
		Assert.assertEquals(expected, c);
	}
}
