package beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Case
{

	@XmlElement(name="condominio")
	private List<Casa> caseList;
	private static Case instance;

	public Case()
	{
		caseList = new ArrayList<>();
		caseList.add(new Casa());
		caseList.add(new Casa("lol"));
	}

	//singleton
	public synchronized static Case getInstance(){
		if(instance==null)
			instance = new Case();
		return instance;
	}

	public synchronized void  add(Casa c){
		caseList.add(c);
	}
}
