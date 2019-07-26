package ServerREST.beans;

// Contiene la lista di misurazioni del condominio: riutilizza il bean CasaMeasurement (arraylist di MeanMeasurement)

// Praticamente uguale a StatisticheLocali ma non esiste nessu casaId: e' sottointeso id del condominio (quindi nessun check su id mai; nessuna hashmap)
// deve essere comunque una classe separata per tenere i LOCK separati
public class StatisticheGlobali
{
	private CasaMeasurement condominioMeasurements;
	private static StatisticheGlobali instance;

	public StatisticheGlobali()
	{
		condominioMeasurements = new CasaMeasurement();
	}

	//singleton
	public synchronized static StatisticheGlobali getInstance()
	{
		if(instance == null)
			instance = new StatisticheGlobali();
		return instance;
	}

	// riceve una misurazione: deve inserirla in coda alla lista misure globali
	public synchronized void addMeanMeasurement(MeanMeasurement m)
	{
		condominioMeasurements.add(m);
	}

	// interfaccia admin: ritorna le ultime n statistiche globali
	public synchronized CasaMeasurement getLastN(String n)
	{
		return condominioMeasurements.getLastN(Integer.parseInt(n));
	}
}
