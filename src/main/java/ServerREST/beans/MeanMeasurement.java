package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="MeanMeasurement")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeanMeasurement
{
	@XmlElement(name = "mean")
	private double mean;
	@XmlElement(name = "beginTimestamp")
	private long beginTimestamp;
	@XmlElement(name = "endTimestamp")
	private long endTimestamp;

	public MeanMeasurement() {}

	public MeanMeasurement(double mean, long begin, long end)
	{
		this.mean = mean;
		this.beginTimestamp = begin;
		this.endTimestamp = end;
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
