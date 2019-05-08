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


	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		Casa casa = (Casa) o;
		return Objects.equals(name, casa.name);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name);
	}
}
