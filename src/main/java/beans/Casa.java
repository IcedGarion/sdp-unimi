package beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Casa")
@XmlAccessorType(XmlAccessType.FIELD)
public class Casa
{
	@XmlElement(name = "name")
	private String name;

	public Casa() {}

	public Casa(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
