package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="Condominio")
@XmlAccessorType(XmlAccessType.FIELD)
public class Condominio
{

	@XmlElement(name = "Casa")
	private List<Casa> caseList;
	private static Condominio instance;

	public Condominio()
	{
		caseList = new ArrayList<>();
	}

	//singleton
	public synchronized static Condominio getInstance()
	{
		if(instance == null)
			instance = new Condominio();
		return instance;
	}

	public synchronized void add(Casa c)
	{
		caseList.add(c);
	}

	public synchronized void delete(Casa c)
	{
		caseList.remove(c);
	}

	public synchronized int size() { return caseList.size(); }

	// serve a getByName
	public synchronized List<Casa> getCaselist() {
		return new ArrayList<>(caseList);
	}

	// restituisce, dato il nome, una casa
	// serve per check se esiste gia'
	public Casa getByName(String name)
	{
		List<Casa> caseCopy = getCaselist();

		for(Casa c: caseCopy)
			if(c.getId().toLowerCase().equals(name.toLowerCase()))
				return c;
		return null;
	}

	// test?
	@Override
	public String toString()
	{
		return "Condominio{" +
				"caseList=" + caseList +
				'}';
	}
}
