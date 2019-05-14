package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
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

	// riceve una misurazione: deve inserirla in coda alla lista misure sotto la casa giusta
	public synchronized boolean addMeanMeasurement(String casaId, MeanMeasurement m)
	{
		List<MeanMeasurement> l = casaMeasurements.get(casaId);

		// check se non esiste
		if(l != null)
		{
			l.add(m);
			return true;
		}
		else
			return false;
	}

	//public synchronized List<MeanMeasurement> getMeasurelist() {
	//	return new ArrayList<>(casaMeasurements);
	//}

}
