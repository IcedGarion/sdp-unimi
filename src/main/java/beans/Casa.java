package beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Casa
{
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
