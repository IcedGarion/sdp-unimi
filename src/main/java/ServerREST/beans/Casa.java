package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name="Casa")
@XmlAccessorType(XmlAccessType.FIELD)
public class Casa
{
	@XmlElement(name = "id")
	private String id;

	@XmlElement(name = "ip")
	private String ip;

	@XmlElement(name = "statsPort")
	private int statsPort;

	@XmlElement(name = "electionPort")
	private int electionPort;

	public Casa() {}

	public Casa(String id, String ip, int statsPort, int electionPort)
	{
		this.id = id;
		this.ip = ip;
		this.statsPort = statsPort;
		this.electionPort = electionPort;
	}

	public String getId()
	{
		return id;
	}
	public String getIp()
	{
		return ip;
	}
	public int getStatsPort()
	{
		return statsPort;
	}
	public int getElectionPort() { return electionPort; }


	// due case uguali solo se hanno stesso ID (il resto puo' cambiare)
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		Casa casa = (Casa) o;
		return Objects.equals(id, casa.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
