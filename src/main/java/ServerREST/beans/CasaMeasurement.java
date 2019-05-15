package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// e' solo una classe contenitore di lista<MeanMeasurement>
// serviva per unmarshal StatisticheLocali (Al posto di HashMap<String, List<MeanMeasurement>>, usa HashMap<String, CasaMeasurement>)

// non serve synchronized perche' ci si accede solo da StatisticheLocali, gia' sync

@XmlRootElement(name="CasaMeasurement")
@XmlAccessorType(XmlAccessType.FIELD)
public class CasaMeasurement
{
	@XmlElement(name = "measures")
	private List<MeanMeasurement> measurementList;

	public CasaMeasurement()
	{
		measurementList = new ArrayList<>();
	}

	// costruttore usato da getLastN per creare CasaMeasurement passando le misure
	public CasaMeasurement(ArrayList<MeanMeasurement> measures)
	{
		measurementList = new ArrayList<>(measures);
	}

	public void add(MeanMeasurement m)
	{
		measurementList.add(m);
	}

	public CasaMeasurement getLastN(int n)
	{
		// crea copia lista inversa e inserisce i primi n in lista ret
		// crea nuovo oggetto CasaMeasurement con la lista creata (perche' non puo ritornare tutto se stesso)
		ArrayList<MeanMeasurement> ret = new ArrayList<>();
		List<MeanMeasurement> reverseMeasures = new ArrayList<>(measurementList);
		Collections.reverse(reverseMeasures);

		int i = 0;
		for(MeanMeasurement m: reverseMeasures)
		{
			if(i > n)
				break;

			ret.add(m);
			i++;
		}

		return new CasaMeasurement(ret);
	}

	public List<MeanMeasurement> getMeasurementList()
	{
		return measurementList;
	}
}
