package ServerREST.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Casa")
@XmlAccessorType(XmlAccessType.FIELD)
public class Notifica
{
	@XmlElement(name = "senderId")
	private String senderId;

	@XmlElement(name = "message")
	private String message;

	@XmlElement(name = "timestamp")
	private long timestamp;

	public Notifica() {}

	public Notifica(String senderId, String message, long timestamp)
	{
		this.senderId = senderId;
		this.message = message;
		this.timestamp = timestamp;
	}

	public String getSenderId() { return this.senderId; }

	public String getMessage() { return this.message; }

	public long getTimestamp() { return this.timestamp; }
}
