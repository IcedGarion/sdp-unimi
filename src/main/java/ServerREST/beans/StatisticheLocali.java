package ServerREST.beans;

import java.util.ArrayList;
import java.util.HashMap;


// Contiene lista di misurazioni per ogni casa; condivisa perche' piu' thread inseriscono e admin le legge
// Corrispondenza fra casaId e la sua lista di misure (HashMap)

// Simile a condominio ma senza annotazioni perche' non viene mai ritornato intero come oggetto da REST (/jaxb)
// invece si accede solo al metodo getLastN, che torna CasaMeasurement: e' quello l'oggetto da annotare

public class StatisticheLocali
{
	// Hashmap ID_CASA: <Lista di misure medie>
	private HashMap<String, CasaMeasurement> casaMeasurements;

	private static StatisticheLocali instance;

	public StatisticheLocali()
	{
		casaMeasurements = new HashMap<>();
	}

	//singleton
	public synchronized static StatisticheLocali getInstance()
	{
		if(instance == null)
			instance = new StatisticheLocali();
		return instance;
	}

	// riceve una misurazione: deve inserirla in coda alla lista misure sotto la casa giusta
	public synchronized boolean addMeanMeasurement(String casaId, MeanMeasurement m)
	{
		CasaMeasurement l = casaMeasurements.get(casaId);

		// check se esiste + inserisce
		if(l != null)
		{
			l.add(m);
			return true;
		}
		// se non esiste prima crea vuoto e poi inserisce
		else
		{
			casaMeasurements.put(casaId, new CasaMeasurement(new ArrayList<MeanMeasurement>(){{ add(m);}}));
			return false;
		}
	}

	// interfaccia admin: ritorna le ultime n statistiche di una certa casa
	public synchronized CasaMeasurement getLastN(String casaId, String n)
	{
		CasaMeasurement casa = casaMeasurements.get(casaId);

		// prende l'elemento corrispondente a casaId e aggiunge in coda (chiama metodo di CasaMeasurement) (= list)
		if(casa == null)
		{
			return null;
		}
		else
		{
			return casaMeasurements.get(casaId).getLastN(Integer.parseInt(n));
		}
	}
}
