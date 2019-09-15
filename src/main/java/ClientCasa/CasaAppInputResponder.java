package ClientCasa;

import ClientCasa.P2P.Boost.PowerBoost;

// thread he gestisce le richieste input utente
// (gestisce soltanto la richiesta di boost, perche' l'uscita invece la gestisce il thread main )
public class CasaAppInputResponder extends Thread
{
	private String action;
	private PowerBoost powerBoostObj;

	public CasaAppInputResponder(String choice, PowerBoost powerBoostObj)
	{
		this.action = choice;
		this.powerBoostObj = powerBoostObj;
	}

	public void run()
	{
		try
		{
			if(action.equals("REQUEST_BOOST"))
			{
				powerBoostObj.requestPowerBoost(false);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
