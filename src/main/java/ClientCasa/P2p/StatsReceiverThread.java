package ClientCasa.P2p;

import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsReceiverThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(StatsReceiverThread.class.getName());

	private MeanMeasurement message;
	private JAXBContext jaxbContext;
	private Unmarshaller unmarshaller;
	private String casaIp, casaId;
	private int statsPort;

	public StatsReceiverThread(String casaId, String casaIp, int statsPort) throws JAXBException
	{
		this.casaId = casaId;
		this.casaIp = casaIp;
		this.statsPort = statsPort;

		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		unmarshaller = jaxbContext.createUnmarshaller();
	}

	public void run()
	{
		// TODO: riceve statistiche locali da TUTTE le case, calcola consumo globale e stampa
		// TODO: deve essere un server concorrente che ascolta
		// ricevere le statistiche locali da ogni casa usando socket + jaxb
		ServerSocket welcomeSocket;
		Socket connectionSocket;

		try
		{
			welcomeSocket = new ServerSocket(statsPort);
			connectionSocket = welcomeSocket.accept();

			message = (MeanMeasurement) unmarshaller.unmarshal(connectionSocket.getInputStream());

			LOGGER.log(Level.INFO, "{ " + casaId + " } Statistic received from " + message.getCasaId() + "\nMean: " + message.getMean());
			System.out.flush();
			System.err.flush();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.INFO, "{ " + casaId + " } Error while receiving stats");
			e.printStackTrace();
		}
	}
}
