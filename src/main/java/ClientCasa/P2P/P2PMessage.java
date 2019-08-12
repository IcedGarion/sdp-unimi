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

	@XmlElement(name = "message")
	private String message;

	@XmlElement(name = "timestamp")
	private long timestamp;

	public P2PMessage() {}

	public P2PMessage(String senderId, int senderPort, String message)
	{
		this(senderId, senderPort, message, 0);
	}

	public P2PMessage(String senderId, int senderPort, String message, long timestamp)
	{
		this.senderId = senderId;
		this.senderPort = senderPort;
		this.message = message;
		this.timestamp = timestamp;
	}

	public String getSenderId() { return senderId; }
	public int getSenderPort() { return senderPort; }
	public String getMessage() { return message; }
	public long getTimestamp() { return timestamp; }
}
