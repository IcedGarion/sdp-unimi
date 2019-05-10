package ClientCasa;

import ClientCasa.smartMeter.SmartMeterSimulator;

public class CasaApp
{
	private static final String SERVER_IP = "localhost";
	private static final int SERVER_PORT = 1337;

	private static final String CASA_ID = "CasaApp1";
	private static final String CASA_IP = "localhost";
	private static final int CASA_PORT = 8081;

	private static final int SIMULATOR_DELAY = 500;


	public static void main(String args[])
	{
		// avvia thread simulatore smart meter
		SimulatorBuffer myBuffer = new SimulatorBuffer();
		SmartMeterSimulator simulator = new SmartMeterSimulator(myBuffer);
		simulator.start();

		// avvia thread che invia periodicamente le medie
		MeanThread mean = new MeanThread(myBuffer, CASA_ID);
		mean.start();

		// si registra al server amministratore

		// chiede elenco case

		// parte rete p2p

		// interfaccia cli per power boost
	}
}
