package ClientCasa;

import ClientCasa.smartMeter.SmartMeterSimulator;

import javax.xml.bind.JAXBException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CasaApp
{
	private static final String SERVER_IP = "localhost";
	private static final int SERVER_PORT = 1337;

	private static final String CASA_ID = "CasaApp1";
	private static final String CASA_IP = "localhost";
	private static final int CASA_PORT = 8081;

	private static final int SIMULATOR_DELAY = 500;
	private static final Logger LOGGER = Logger.getLogger(CasaApp.class.getName());


	public static void main(String args[]) throws JAXBException
	{
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Started Casa Application with ID " + CASA_ID);

		// avvia thread simulatore smart meter
		SimulatorBuffer myBuffer = new SimulatorBuffer();
		SmartMeterSimulator simulator = new SmartMeterSimulator(myBuffer);
		simulator.start();
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Smart meted launched");

		// avvia thread che invia periodicamente le medie
		MeanThread mean = new MeanThread(myBuffer, CASA_ID);
		mean.start();
		LOGGER.log(Level.INFO, "{ " + CASA_ID + " } Local statistic thread launched");

		// si registra al server amministratore

		// chiede elenco case

		// parte rete p2p

		// interfaccia cli per power boost
	}
}
