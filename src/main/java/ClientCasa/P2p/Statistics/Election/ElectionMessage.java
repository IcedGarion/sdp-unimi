package ClientCasa.P2p.Statistics.Election;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ElectionMessage")
@XmlAccessorType(XmlAccessType.FIELD)
public class ElectionMessage
{
	@XmlElement(name = "senderId")
	private String senderId;

	@XmlElement(name = "receiverId")
	private String receiverId;

	@XmlElement(name = "message")
	private String message;

	public ElectionMessage() {}

	public ElectionMessage(String senderId, String receiverId, String message)
	{
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.message = message;
	}

	public String getSenderId() { return senderId; }
	public String getReceiverId() { return receiverId; }
	public String getMessage() { return message; }
}
