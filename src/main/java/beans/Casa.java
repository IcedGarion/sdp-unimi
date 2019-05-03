package beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Casa
{
	private String name;

	public Casa()
	{
		name = "default";
	}

	public Casa(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
