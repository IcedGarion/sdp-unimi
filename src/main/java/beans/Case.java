package beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Case
{
	@XmlElement(name="case")
	private List<Casa> caseList;
	private static Case instance;

	public Case()
	{
		caseList = new ArrayList<>();
		caseList.add(new Casa("lol"));
	}

	//singleton
	public synchronized static Case getInstance(){
		if(instance==null)
			instance = new Case();
		return instance;
	}


}
