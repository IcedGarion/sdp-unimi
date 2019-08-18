package ClientCasa;

import ClientCasa.LocalStatistics.MeanThread;
import ClientCasa.LocalStatistics.SimulatorBuffer;
import ClientCasa.LocalStatistics.smartMeter.SmartMeterSimulator;
import ClientCasa.P2P.Boost.PowerBoost;
import ClientCasa.P2P.Boost.PowerBoostResponder;
import ClientCasa.P2P.GlobalStatistics.Election.Election;
import ClientCasa.P2P.GlobalStatistics.Election.ElectionResponder;
import ClientCasa.P2P.GlobalStatistics.StatsReceiverResponder;
import ClientCasa.P2P.Message.GlobalMessageServer;
import ServerREST.beans.Casa;
import Shared.Configuration;
import Shared.Http;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CasaApp
{
	private static final Logger LOGGER = Logger.getLogger(CasaApp.class.getName());

	// chiamato quando avvengono altre stampe, per riportare il menu' davanti
	public static void refreshMenu()
	{
		System.out.println("\nMENU'\n======================================\n\n0) POWER BOOST\n\n1) EXIT\n\n======================================\n");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	PROGRAMMA PRINCIPALE CASE	//
	public static void main(String[] args) throws JAXBException
	{
		//////////////
		/*	SETUP	*/
		Level loggerLevel = null;
		String casaId = "", casaIp = "";
		int casaPort = 0;

		// CONFIGURATIONS setup
		try
		{
			Configuration.loadProperties();
			loggerLevel = Configuration.LOGGER_LEVEL;
			casaId = Configuration.CASA_ID;
			casaIp = Configuration.CASA_IP;
			casaPort = Configuration.CASA_PORT;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error in reading configuration file: " + Configuration.CONFIGURATION_FILE);
			System.exit(1);
		}

		// LOGGER setup
		LOGGER.setLevel(loggerLevel);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(loggerLevel);
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);

		LOGGER.log(Level.INFO, "{ " + casaId + " } Started Casa Application with ID " + casaId);



		/*	AVVIA THREAD SIMULATORE / SMART METER */
		SimulatorBuffer myBuffer = new SimulatorBuffer();
		SmartMeterSimulator simulator = new SmartMeterSimulator(myBuffer);
		simulator.start();
		LOGGER.log(Level.FINE, "{ " + casaId + " } Smart meted launched");



		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*	PARTE RETE P2P	*/
		// Prepara gestore elezione e oggetto condiviso Election da passargli; lo passa anche a statsReceiver perche' deve sapere stato elezione
		// ( per sapere chi e' coord e quindi chi manda le stats)
		Election election = new Election();

		// crea gestore elezione bully: riceve msg e risponde a dovere secondo alg BULLY
		ElectionResponder electionWorker = new ElectionResponder(election);

		// crea gestore messaggi p2p che riceve le statistiche
		StatsReceiverResponder statsReceiver = new StatsReceiverResponder(election);

		// crea gestore messaggi p2p che riceve richieste di power boost e si coordina
		PowerBoost powerBoost = new PowerBoost(simulator);
		PowerBoostResponder powerBoostWorker = new PowerBoostResponder(powerBoost);


		/*	AVVIA SERVER GLOBALE RICEZIONE MESSAGGI P2P		*/
		// "attiva" i metodi di questi 3 componenti, che richiedono i messaggi P2P
		GlobalMessageServer messageServer = new GlobalMessageServer(statsReceiver, electionWorker, powerBoostWorker);
		messageServer.start();
		LOGGER.log(Level.FINE, "{ " + casaId + " } Global message receiver serer launched");



		///////////////////////////////////////////////////
		/*	REGISTRA LA CASA AL SERVER AMMINISTRATORE	*/
		Casa myCasa = new Casa(casaId, casaIp, casaPort);
		Http.registerCasa(myCasa);
		LOGGER.log(Level.INFO, "{ " + casaId + " } Casa registered to Admin Server");




		///////////////////////////////////////////////////////////
		// avvia thread che invia periodicamente le medie
		MeanThread mean = new MeanThread(myBuffer);
		mean.start();
		LOGGER.log(Level.FINE, "{ " + casaId+ " } Local statistic (MeanThread) thread launched");





		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*	INTERFACCIA CLI BOOST + EXIT	*/
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String choice;
		while(true)
		{
			try
			{
				refreshMenu();
				choice = input.readLine();

				// EXIT
				// termina tutti i thread + informa il server che la casa sta per uscire
				if(choice.equals("1"))
				{
					// cancella casa dal condominio rest
					Http.deleteCasa(myCasa);

					// se sta per uscire ed era il coordinatore delle stat globali, dice a tutti che molla (nuova elezione poi)
					if(election.getState().equals(Election.ElectionOutcome.COORD))
					{
						election.coordLeaving();
					}

					// termina i suoi thread
					messageServer.interrupt();
					mean.interrupt();
					simulator.interrupt();

					LOGGER.log(Level.INFO, "{ " + casaId + " } Stopping CasaApp...");
					break;
				}
				// POWER BOOST
				else if(choice.equals("0"))
				{
					LOGGER.log(Level.INFO, "{ " + casaId + " } Power boost requested... (Please wait)");
					powerBoost.requestPowerBoost();
				} else
				{
					System.out.println("Inserire 0/1");
				}
			}
			catch(Exception e)
			{
				LOGGER.log(Level.INFO, "{ " + casaId + " } Error in choice...");
				e.printStackTrace();
			}
		}

		// Terminano anche tutti i thread lanciati: SmartMeterSimulator, MeanThread, e P2pThread
		System.exit(0);
	}
}
