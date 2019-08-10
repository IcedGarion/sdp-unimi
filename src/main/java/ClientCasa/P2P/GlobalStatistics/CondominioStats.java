package ClientCasa.P2P.GlobalStatistics;

import ServerREST.beans.MeanMeasurement;

import java.util.ArrayList;
import java.util.HashMap;

/*
	Soltanto un wrapper per hashmap (synchronized)
	Condiviso fra: StatsReceiverThread e StatsReceiverWorkerThread
	StatsReceiverWorkerThread mette qua le statistiche ricevute dalle altre case;
	StatsReceiverThread legge da qua per calcolare il consumo globale e azzerare la mappa per ricominiciare
	(quando riceve stats da tutte le case attive, azzera e ricomincia)
 */
public class CondominioStats
{
	private HashMap<String, MeanMeasurement> caseStatsMap;

	public CondominioStats()
	{
		caseStatsMap = new HashMap<>();
	}

	// TODO: se arriva una statistica aggiornata (ennesima stat da casa x anche se casa y ancora non ha mandato la sua):
	// va avanti e sovrascrive con quella piu' recente...
	// spazio in questo metodo per cambiare policy: si potrebbe anche ignorare se esiste gia' chaive x
	public synchronized void addCasaStat(MeanMeasurement measure)
	{
		caseStatsMap.put(measure.getCasaId(), measure);
	}

	public synchronized void resetStats()
	{
		caseStatsMap = new HashMap<>();
	}

	public synchronized int size()
	{
		return caseStatsMap.size();
	}

	public synchronized boolean contains(String casaId)
	{
		return caseStatsMap.containsKey(casaId);
	}

	public synchronized ArrayList<MeanMeasurement> getMeasurements()
	{
		return new ArrayList<>(caseStatsMap.values());
	}
}
