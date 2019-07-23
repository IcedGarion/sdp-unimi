package ClientCasa.P2p.Statistics;

import ServerREST.beans.MeanMeasurement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsReceiverThread extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(StatsReceiverThread.class.getName());
	private MeanMeasurement message;
	private JAXBContext jaxbContext;
	private Unmarshaller unmarshaller;
	private Socket listenSocket;
	private String casaId;

	public StatsReceiverThread(Socket listenSocket, String casaId) throws JAXBException
	{
		this.listenSocket = listenSocket;
		this.casaId = casaId;

		jaxbContext = JAXBContext.newInstance(MeanMeasurement.class);
		unmarshaller = jaxbContext.createUnmarshaller();
	}

	public void run()
	{
		// TODO: riceve statistiche locali da TUTTE le case, calcola consumo globale e stampa
		// ricevere le statistiche locali da ogni casa usando socket + jaxb

		try
		{
			message = (MeanMeasurement) unmarshaller.unmarshal(listenSocket.getInputStream());
			listenSocket.close();
			LOGGER.log(Level.INFO, "{ " + casaId + " } Statistic received from " + message.getCasaId() + "\nMean: " + message.getMean());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.INFO, "{ " + casaId + " } Error while receiving stats");
			e.printStackTrace();
		}



	}
}
