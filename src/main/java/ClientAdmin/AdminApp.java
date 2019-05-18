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
import java.sql.Timestamp;
import java.util.List;

public class AdminApp
{
	private static final String SERVER_URL = "http://localhost:1337";

	// funzioni per calcolo media / deviazione standard
	private static final double getMean(List<MeanMeasurement> misure)
	{
		double sum = 0;

		for(MeanMeasurement m: misure)
		{
			sum += m.getMean();
		}

		return sum / misure.size();
	}

	// deviazione standard
	private static double getStdev(List<MeanMeasurement> misure, double mean)
	{
		double acc = 0;

		for(MeanMeasurement m: misure)
		{
			acc += Math.pow(m.getMean() - mean, 2);
		}

		return acc / misure.size();
	}

	// MAIN interfaccia admin
	public static void main(String args[]) throws IOException, JAXBException
	{
		JAXBContext jaxbContextCondominio = JAXBContext.newInstance(Condominio.class);
		Unmarshaller jaxbUnmarshallerCondominio = jaxbContextCondominio.createUnmarshaller();
		JAXBContext jaxbContextStatLocali = JAXBContext.newInstance(CasaMeasurement.class);
		Unmarshaller jaxbUnmarshallerStatLocali = jaxbContextStatLocali.createUnmarshaller();
		URL url;
		HttpURLConnection conn;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String choice, n, casaId;
		double mean;

		while(true)
		{
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/*	MENU'	*/
			System.out.println("\n===================================================================================\n" +
					"ADMIN INTERFACE\n" +
					"0) Elenco case presenti nella rete\n" +
					"1) Ultime <n> statistiche relative ad una specifica <casa>\n" +
					"2) Ultime <n> statistiche condominiali\n" +
					"3) Deviazione standard e media delle ultime <n> statistiche prodotte da una specifica <casa>\n" +
					"4) Deviazione standard e media delle ultime <n> statistiche complessive condominiali\n");

			choice = in.readLine();

			// Elenco case
			if(choice.equals("0"))
			{
				url = new URL(SERVER_URL + "/condominio");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				Condominio c = (Condominio) jaxbUnmarshallerCondominio.unmarshal(conn.getInputStream());

				System.out.println("Elenco case presenti nella rete");
				for(Casa casa: c.getCaselist())
				{
					System.out.println("ID casa: " + casa.getId() + "\nIP casa: " + casa.getIp() + ", PORT: " + casa.getPort() + "\n");
				}
			}
			// Ultime n statistiche casa
			else if(choice.equals("1"))
			{
				System.out.println("Ultime <n> statistiche relative ad una specifica <casa>\nInserire parametro <n>... ");
				n = in.readLine();
				System.out.println("Inserire parametro <casa> (ID di una casa)");
				casaId = in.readLine();

				url = new URL(SERVER_URL + "/statisticheLocali/get/" + casaId + "/" + n);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				CasaMeasurement misure = (CasaMeasurement) jaxbUnmarshallerStatLocali.unmarshal(conn.getInputStream());

				System.out.println("Ultime " + n + " statistiche relative alla casa " + casaId);
				for(MeanMeasurement m: misure.getMeasurementList())
				{
					System.out.println("Media: " + m.getMean() + " (da " + new Timestamp(m.getBeginTimestamp()) + " a " + new Timestamp(m.getEndTimestamp()) + ")");
				}
			}
			// ultime n statistiche condominiali
			else if(choice.equals("2"))
			{
				// TODO
				System.out.println("To be implemented!");
			}
			// deviazione e media casa
			else if(choice.equals("3"))
			{
				System.out.println("Deviazione standard e media delle ultime <n> statistiche prodotte da una specifica <casa>\nInserire parametro <n>... ");
				n = in.readLine();
				System.out.println("Inserire parametro <casa> (ID di una casa)");
				casaId = in.readLine();

				url = new URL(SERVER_URL + "/statisticheLocali/get/" + casaId + "/" + n);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				CasaMeasurement misure = (CasaMeasurement) jaxbUnmarshallerStatLocali.unmarshal(conn.getInputStream());

				mean = getMean(misure.getMeasurementList());
				System.out.println("Media: " + mean + ", Deviazione std: " + getStdev(misure.getMeasurementList(), mean));
			}
			// deviazione e media condominiali
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
