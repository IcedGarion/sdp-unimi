package ClientCasa.P2P;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


// Serve per fare marshalling / unmarshalling di messaggi election
@XmlRootElement(name="P2PMessage")
@XmlAccessorType(XmlAccessType.FIELD)
public class P2PMessage
{
	@XmlElement(name = "senderId")
	private String senderId;

	@XmlElement(name = "senderPort")
	private int senderPort;

	@XmlElement(name = "receiverId")
	private String receiverId;

	@XmlElement(name = "message")
	private String message;

	public P2PMessage() {}

	public P2PMessage(String senderId, int senderPort, String receiverId, String message)
	{
		this.senderId = senderId;
		this.senderPort = senderPort;
		this.receiverId = receiverId;
		this.message = message;
	}

	public String getSenderId() { return senderId; }
	public int getSenderPort() { return senderPort; }
	public String getReceiverId() { return receiverId; }
	public String getMessage() { return message; }
}
