package ClientCasa;

import ClientCasa.smartMeter.Measurement;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeanThread extends Thread
{
	private SimulatorBuffer buffer;
	private static final String URL = "http://localhost:1337";
	private static final Logger LOGGER = Logger.getLogger(MeanThread.class.getName());

	// id della casa che ha creato il buffer: serve per poi inserire in rest
	private String casaId;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;

	public MeanThread(SimulatorBuffer buffer, String casaId) throws JAXBException
	{
		this.buffer = buffer;
		this.casaId = casaId;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	}

	@Override
	public void run()
	{
		List<Measurement> sensorData;
		Measurement element;
		MeanMeasurement computedMeasure;
		double mean;
		long timestampMin, timestampMax;
		URL url;
		HttpURLConnection conn;

		LOGGER.log(Level.INFO, "{ " + casaId + " } MeanThread running...");

		try
		{
			while(true)
			{
				// all'inizio aspetta finche' il buffer si riempie
				if(buffer.size() >= 24)
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } Receiving sensor data...");

					// prende i primi 24
					sensorData = buffer.getTopBuffer();

					element = sensorData.get(0);
					timestampMax = timestampMin = element.getTimestamp();
					mean = element.getValue();

					// calcola media e intanto prende il timestamp MIN + MAX
					for(Measurement m : sensorData)
					{
						mean += m.getValue();

						if(m.getTimestamp() < timestampMin)
						{
							timestampMin = m.getTimestamp();
						} else if(m.getTimestamp() > timestampMax)
						{
							timestampMax = m.getTimestamp();
						}
					}

					// crea oggetto da mandare a REST
					mean = mean / sensorData.size();
					computedMeasure = new MeanMeasurement(mean, timestampMin, timestampMax);


					//chiamata REST a StatisticheService passando ID_CASA + MeanMeasurement
					// POST /condominio/add: inserisce nuova casa
					LOGGER.log(Level.INFO, "{ "  +casaId + " } Sending computed statistic to Server...");

					url = new URL(URL + "/statisticheLocali/add/" + casaId);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("content-type", "application/xml");
					conn.setDoOutput(true);

					// invia casa come xml body
					marshaller.marshal(computedMeasure, conn.getOutputStream());
					LOGGER.log(Level.INFO, "{ " + casaId + " } Statistic sent");

					BufferedReader t = new BufferedReader(new InputStreamReader(System.in));
					t.readLine();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
