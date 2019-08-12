package ClientCasa.P2P.Boost;

import ClientCasa.LocalStatistics.smartMeter.SmartMeterSimulator;

/*
	UN THREAD CHE CHIAMA IL BOOST SIMULATORE, ASPETTA, E RICHIAMA endPowerBoost
 */
public class PowerBoostWaiterThread extends Thread
{
	private PowerBoost powerBoostObj;
	private SmartMeterSimulator simulator;

	public PowerBoostWaiterThread(PowerBoost powerBoostObj, SmartMeterSimulator simulator)
	{
		this.powerBoostObj = powerBoostObj;
		this.simulator = simulator;
	}

	public void run()
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
			e.printStackTrace();
		}
	}
}
