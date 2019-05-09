package beans;

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

	@XmlElement(name = "port")
	private int port;

	public Casa() {}

	public Casa(String id, String ip, int port)
	{
		this.id = id;
		this.ip = ip;
		this.port = port;
	}

	public String getId()
	{
		return id;
	}
	public String getIp()
	{
		return ip;
	}
	public int getPort()
	{
		return port;
	}


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
