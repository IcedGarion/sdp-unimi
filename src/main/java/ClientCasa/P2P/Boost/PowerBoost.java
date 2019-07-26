package ClientCasa.P2P.Boost;

// oggetto condiviso con dati riguardanti lo stato del power boost ("sto usando"/no; coda di chi ha richiesto....)
// Condiviso fra PowerBoostThread e PowerBoostWorker
public class PowerBoost
{
	public enum PowerBoostState { REQUESTED, USING, NOT_INTERESTED };

	private String casaId;
	private PowerBoostState state;

	public PowerBoost(String casaId)
	{
		this.casaId = casaId;
		this.state = PowerBoostState.NOT_INTERESTED;
	}

	public synchronized void setState(PowerBoostState state)
	{
		this.state = state;
	}

	public synchronized PowerBoostState getState()
	{
		return this.state;
	}

	// simile a Election.startElection(): avvisa tutti che serve boost; poi il thread rispondera' e gestira' lui
	public void requestPowerBoost()
	{
		// TODO: inizio boost
		//manda msg a tutte le case che vuole fare power boost;
		//setta stato (setState) dicendo che hai richiesto...
		//poi ci pensa il thread
	}

}
