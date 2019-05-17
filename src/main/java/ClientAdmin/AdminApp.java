package ClientAdmin;

import ServerREST.beans.Casa;
import ServerREST.beans.CasaMeasurement;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

comunque tutta da testare

public class AdminApp
{
	private static final String SERVER_URL = "http://localhost:1337";

	public static void main(String args[]) throws IOException, JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(Condominio.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Unmarshaller  jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		URL url;
		HttpURLConnection conn;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String choice, n, casaId;

		while(true)
		{
			System.out.println("ADMIN INTERFACE\n" +
					"0) Elenco case presenti nella rete\n" +
					"1) Ultime <n> statistiche relative ad una specifica <casa>" +
					"2) Ultime <n> statistiche condominiali" +
					"3) Deviazione standard e media delle ultime <n> statistiche prodotte da una specifica <casa>" +
					"4) Deviazione standard e media delle ultime <n> statistiche complessive condominiali\n> ");

			choice = in.readLine();

			if(choice.equals("0"))
			{
				url = new URL(SERVER_URL + "/condominio");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				Condominio c = (Condominio) jaxbUnmarshaller.unmarshal(conn.getInputStream());

				System.out.println("Elenco case presenti nella rete");
				for(Casa casa: c.getCaselist())
				{
					System.out.println("ID casa: " + casa.getId() + "\nIP casa: " + casa.getIp() + ", PORT: " + casa.getPort() + "\n");
				}
			}
			else if(choice.equals("1"))
			{
				System.out.println("Ultime <n> statistiche relative ad una specifica <casa>\nInserire parametro <n>... ");
				n = in.readLine();
				System.out.println("Inserire parametro <casa> (ID di una casa)");
				casaId = in.readLine();

				url = new URL(SERVER_URL + "/statisticheLocali/get/" + casaId + "/" + n);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				CasaMeasurement misure = (CasaMeasurement) jaxbUnmarshaller.unmarshal(conn.getInputStream());

				System.out.println("Ultime " + n + " statistiche relative alla casa " + casaId);
				for(MeanMeasurement m: misure.getMeasurementList())
				{
					convertire magari i timestamp in cose leggibili umanamente?


					System.out.println("Media: " + m.getMean() + "(da " + m.getBeginTimestamp() + " a " + m.getEndTimestamp() + ")");
				}
			}
			else if(choice.equals("2"))
			{
				// TODO
				System.out.println("To be implemented!");
			}
			else if(choice.equals("3"))
			{
				deviazione std
			}
			else if(choice.equals("4"))
			{
				// TODO
				System.out.println("To be implemented!");
			}
			else
			{
				System.out.println("Inserire 0/1/2/3/4");
			}
		}
	}
}
