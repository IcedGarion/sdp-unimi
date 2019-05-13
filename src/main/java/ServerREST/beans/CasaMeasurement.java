package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="CasaMeasurement")
@XmlAccessorType(XmlAccessType.FIELD)
public class CasaMeasurement
{
	@XmlElement(name = "idCasa")
	private String idCasa;

	@XmlElement(name = "measurements")
	private List<MeanMeasurement> measurements;

	public CasaMeasurement() {}

	public CasaMeasurement(String id)
	{
		this.idCasa = id;
		measurements = new ArrayList<>();
	}

	public void addMeanMeasurement(MeanMeasurement m)
	{

	}

	public String getIdCasa()
	{
		return idCasa;
	}

	public List<MeanMeasurement> getMeasurements()
	{
		return measurements;
	}
}
