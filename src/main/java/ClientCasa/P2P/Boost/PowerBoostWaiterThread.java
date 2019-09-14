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
					Thread.sleep(step);
					time += step;

					if(powerBoostObj.getObtained() || interrupted())
					{
						break;
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
