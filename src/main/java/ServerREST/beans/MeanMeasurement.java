package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="MeanMeasurement")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeanMeasurement
{
	@XmlElement(name = "casaId")
	private String casaId;
	@XmlElement(name = "mean")
	private double mean;
	@XmlElement(name = "beginTimestamp")
	private long beginTimestamp;
	@XmlElement(name = "endTimestamp")
	private long endTimestamp;

	public MeanMeasurement() {}

	public MeanMeasurement(String casaId, double mean, long begin, long end)
	{
		this.casaId = casaId;
		this.mean = mean;
		this.beginTimestamp = begin;
		this.endTimestamp = end;
	}

	public String getCasaId()
	{
		return casaId;
	}

	public double getMean()
	{
		return mean;
	}

	public long getBeginTimestamp()
	{
		return beginTimestamp;
	}

	public long getEndTimestamp()
	{
		return endTimestamp;
	}
}
