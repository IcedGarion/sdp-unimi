package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@XmlRootElement(name="Statistche")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatisticheLocali
{
	// Hashmap ID_CASA: <Lista di misure medie>
	@XmlElement(name = "CasaMeasurements")
	private HashMap<String, List<MeanMeasurement>> casaMeasurements;

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

	public synchronized void addNewCasa(String casaId)
	{
		casaMeasurements.put(casaId, new ArrayList<>());
	}

	// riceve una misurazione: deve inserirla in coda alla lista misure sotto la casa giusta
	public synchronized boolean addMeanMeasurement(String casaId, MeanMeasurement m)
	{
		List<MeanMeasurement> l = casaMeasurements.get(casaId);

		// check se esiste + inserisce
		if(l != null)
		{
			l.add(m);
			return true;
		}
		else
			return false;
	}

	// interfaccia admin: ritorna le ultime n statistiche di una certa casa
	public synchronized List<MeanMeasurement> getLastN(String casaId, int n)
	{
		// crea copia lista inversa e inserisce i primi n in lista ret
		List<MeanMeasurement> ret = new ArrayList<>();
		List<MeanMeasurement> reverseMeasures = new ArrayList<>(casaMeasurements.get(casaId));
		Collections.reverse(reverseMeasures);

		int i = 0;
		for(MeanMeasurement m: reverseMeasures)
		{
			if(i > n)
				break;

			ret.add(m);
			i++;
		}

		return ret;
	}
}
