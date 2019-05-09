package ServerREST.beans;

import ClientCasa.smartMeter.Measurement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="Statistche")
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistiche
{

	// strutture dati condivise per le statistiche
	// contiene dati delle statistiche da inviare; gestisce il loro accesso condiviso sync + accesso REST
	// identico a condominio


	// contiene tipo Array<Measurement>
	@XmlElement(name = "Measure")
	private List<Measurement> measureList;
	private static Statistiche instance;

	public Statistiche()
	{
		measureList = new ArrayList<>();
	}

	//singleton
	public synchronized static Statistiche getInstance()
	{
		if(instance == null)
			instance = new Statistiche();
		return instance;
	}

	public synchronized void add(Measurement m)
	{
		measureList.add(m);
	}

	// boh altro....
	public synchronized List<Measurement> getMeasurelist() {
		return new ArrayList<>(measureList);
	}

}
