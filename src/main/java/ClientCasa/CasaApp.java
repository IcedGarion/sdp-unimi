package ClientCasa;

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
		SimulatorThread simulator = new SimulatorThread(SIMULATOR_DELAY);
		simulator.start();



		// si registra al server amministratore

		// chiede elenco case

		// parte rete p2p

		// interfaccia cli per power boost
	}
}
