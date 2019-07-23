package ClientAdmin;

import ServerREST.beans.Casa;
import ServerREST.beans.CasaMeasurement;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminApp
{
	private static final String SERVER_URL = "http://localhost:1337";
	private static final int RETRY_TIMEOUT = 250;
	private static final Logger LOGGER = Logger.getLogger(AdminApp.class.getName());

	// funzioni per calcolo media / deviazione standard
	private static double getMean(List<MeanMeasurement> misure)
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
	public static void main(String[] args) throws IOException, JAXBException, InterruptedException
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
		Condominio c = new Condominio();
		CasaMeasurement misure = new CasaMeasurement();

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

			switch(choice)
			{
				// Elenco case
				case "0":
				{
					try
					{
						url = new URL(SERVER_URL + "/condominio");
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						c = (Condominio) jaxbUnmarshallerCondominio.unmarshal(conn.getInputStream());
					}
					catch(Exception e)
					{
						LOGGER.log(Level.WARNING, "Failed to connect to Admin Server ( GET " + SERVER_URL + "/condominio )");
						break;
					}

					if(c.getCaselist().size() == 0)
					{
						System.out.println("Nessuna casa registrata!");
						break;
					}

					System.out.println("Elenco case presenti nella rete:");
					for(Casa casa : c.getCaselist())
					{
						System.out.println("ID casa: " + casa.getId() + "\nIP casa: " + casa.getIp() + ", PORT: " + casa.getPort() + "\n");
					}

					break;
				}
				// Ultime n statistiche casa
				case "1":
				{
					System.out.println("Ultime <n> statistiche relative ad una specifica <casa>");
					while(true)
					{
						try
						{
							System.out.println("Inserire parametro <n>... ");
							n = in.readLine();
							Integer.parseInt(n);
							break;
						}
						catch(Exception e)
						{
							System.out.println("Inserire un intero!");
						}
					}

					System.out.println("Inserire parametro <casa> (ID di una casa)");
					casaId = in.readLine();

					try
					{
						url = new URL(SERVER_URL + "/statisticheLocali/get/" + casaId + "/" + n);
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						misure = (CasaMeasurement) jaxbUnmarshallerStatLocali.unmarshal(conn.getInputStream());
					}
					catch(Exception e)
					{
						System.out.println("La casa '" + casaId + "' non esiste!");
						break;
					}

					System.out.println("Ultime " + n + " statistiche relative alla casa " + casaId);
					for(MeanMeasurement m : misure.getMeasurementList())
					{
						System.out.println("Media: " + m.getMean() + " (da " + new Timestamp(m.getBeginTimestamp()) + " a " + new Timestamp(m.getEndTimestamp()) + ")");
					}

					break;
				}
				// ultime n statistiche condominiali
				case "2":
				{
					// TODO
					System.out.println("To be implemented!");

					break;
				}
				// deviazione e media casa
				case "3":
				{
					System.out.println("Deviazione standard e media delle ultime <n> statistiche prodotte da una specifica <casa>");
					while(true)
					{
						try
						{
							System.out.println("Inserire parametro <n>... ");
							n = in.readLine();
							Integer.parseInt(n);
							break;
						}
						catch(Exception e)
						{
							System.out.println("Inserire un intero!");
						}
					}

					System.out.println("Inserire parametro <casa> (ID di una casa)");
					casaId = in.readLine();

					try
					{
						url = new URL(SERVER_URL + "/statisticheLocali/get/" + casaId + "/" + n);
						conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						misure = (CasaMeasurement) jaxbUnmarshallerStatLocali.unmarshal(conn.getInputStream());
					}
					catch(Exception e)
					{
						System.out.println("La casa '" + casaId + "' non esiste!");
						break;
					}

					mean = getMean(misure.getMeasurementList());
					System.out.println("Media: " + mean + ", Deviazione std: " + getStdev(misure.getMeasurementList(), mean));

					break;
				}
				// deviazione e media condominiali
				case "4":
				{
					// TODO
					System.out.println("To be implemented!");

					break;
				}
				default:
				{
					System.out.println("Inserire 0/1/2/3/4");

					break;
				}
			}
		}
	}
}
