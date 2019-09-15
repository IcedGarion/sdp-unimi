package ClientCasa.P2P.Boost;

import ClientCasa.LocalStatistics.smartMeter.SmartMeterSimulator;

/*
	UN THREAD CHE CHIAMA IL BOOST SIMULATORE, ASPETTA, E RICHIAMA endPowerBoost
 */
public class PowerBoostWaiterThread extends Thread
{
	private PowerBoost powerBoostObj;
	private SmartMeterSimulator simulator;
	private int timeout;
	private boolean boostMode;

	public PowerBoostWaiterThread(PowerBoost powerBoostObj, int timeout)
	{
		this.powerBoostObj = powerBoostObj;
		this.timeout = timeout;
		this.boostMode = false;
	}

	public PowerBoostWaiterThread(PowerBoost powerBoostObj, SmartMeterSimulator simulator)
	{
		this.powerBoostObj = powerBoostObj;
		this.simulator = simulator;
		this.boostMode = true;
	}

	public void run()
	{
		// usa boost e aspetta la fine
		if(boostMode)
		{
			try
			{
				// chiama il power boost vero del simulatore (continene SLEEP)
				simulator.boost();

				// DEBUG
				// sleep(10000);

				// richiama metodo di PowerBoost endBoost, che risistema tutto (rida' l'esecuzione dove doveva essere, dopo aver atteso)
				powerBoostObj.endPowerBoost();
			}
			catch(Exception e)
			{
				System.out.println("Interrotto durante il power boost! Se stai uscendo, Casa mandera' gli OK per il rilascio...");
				// e.printStackTrace();
			}
		}
		// aspetta timeout e chiede di nuovo boost
		// ogni "step" secondi check se il boost e' stato ottenuto; se si, smette; se no, aspetta ancora
		// finito timeout chiede di nuovo request boost e finisce
		else
		{
			int time = 0, step = 1000;

			try
			{
				while(time < timeout)
				{
					System.out.println("TIMEOUT BOOST: provo a richiedere di nuovo BOOST fra " + step + " millisecondi ");

					Thread.sleep(step);
					time += step;

					// se alla fine sei riuscito a ottenere boost a questo giro, fine
					if(powerBoostObj.getObtained() || interrupted())
					{
						System.out.println("Stavo continuando a riprovare ma intanto il power boost e' stato ottenuto.");
						break;
					}

					// chiedi di nuovo solo se non lo stai gia' usando
					if(! powerBoostObj.getState().equals(PowerBoost.PowerBoostState.USING))
					{
						powerBoostObj.requestPowerBoost(true);
					}
				}
			}
			catch(Exception e)
			{
				// ti hanno fermato perche' ottenuto boost.
			}
		}
	}
}
