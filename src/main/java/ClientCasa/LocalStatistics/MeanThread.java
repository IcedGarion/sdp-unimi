package ClientCasa.LocalStatistics;

import ClientCasa.CasaApp;
import ClientCasa.LocalStatistics.smartMeter.Measurement;
import ClientCasa.P2P.MessageSenderThread;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeanThread extends Thread
{
	private SimulatorBuffer buffer;
	private static final Logger LOGGER = Logger.getLogger(MeanThread.class.getName());

	// id della casa che ha creato il buffer: serve per poi inserire in rest
	private String casaId;
	private JAXBContext jaxbContext;
	private Marshaller marshaller;
	private int delay;

	public MeanThread(SimulatorBuffer buffer, String casaId, int delay) throws JAXBException
	{
		this.buffer = buffer;
		this.casaId = casaId;
		this.delay = delay;

		// setup marshaller per invio statistiche
		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		// logger levels
		LOGGER.setLevel(CasaApp.LOGGER_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(CasaApp.LOGGER_LEVEL);
		LOGGER.addHandler(handler);
	}

	@Override
	public void run()
	{
		List<Measurement> sensorData;
		Measurement element;
		MeanMeasurement computedMeasure;
		double mean;
		long timestampMin, timestampMax;

		while(true)
		{
			try
			{
				// all'inizio aspetta finche' il buffer si riempie
				if(buffer.size() >= 24)
				{
					LOGGER.log(Level.FINE, "{ " + casaId + " } Receiving sensor data...");

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
					computedMeasure = new MeanMeasurement(casaId, mean, timestampMin, timestampMax);


					/*	AGGIUNGE STATISICA LOCALE AL SERVER	*/
					// chiamata REST a StatisticheService passando ID_CASA + MeanMeasurement
					// POST /statisticheLocali/add: inserisce nuova statistica
					LOGGER.log(Level.FINE, "{ " + casaId + " } Sending computed statistic to Server...");
					CasaApp.sendLocalStat(computedMeasure);
					LOGGER.log(Level.FINE, "{ " + casaId + " } Statistic sent to server");


					/*	MANDA STATISTICA LOCALE ALLE ALTRE CASE	(~BROADCAST: invia anche a se stessa così ognuno ha elenco completo uguale)*/
					// Per ogni casa nel condiminio, lancia thread che invia
					MessageSenderThread localStatSender;

					// scarica condominio
					LOGGER.log(Level.FINE, "{ " + casaId + " } Requesting condominio...");
					Condominio condominio = CasaApp.getCondominio();

					// BROADCAST: crea e lancia thread che invia messaggio statistica a ogni casa
					for(Casa c : condominio.getCaselist())
					{
						localStatSender = new MessageSenderThread(casaId, c.getId(), c.getIp(), c.getStatsPort(), computedMeasure);
						localStatSender.start();
					}

					// check TERMINAZIONE
					if(interrupted())
					{
						LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping MeanThread... ");
						return;
					}
					sleep(delay);
				}
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "{ " + casaId + " } Lost connection with server!");
			}
		}
	}
}
