package ClientCasa.P2P.Message;

import ServerREST.beans.MeanMeasurement;

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

	@XmlElement(name = "senderIp")
	private String senderIp;

	@XmlElement(name = "senderPort")
	private int senderPort;

	@XmlElement(name = "message")
	private String message;

	@XmlElement(name = "timestamp")
	private long timestamp;

	@XmlElement(name = "measure")
	private MeanMeasurement measure;

	@XmlElement(name = "dispatchTo")
	private String dispatchto;

	public P2PMessage() {}

	public P2PMessage(String senderId, int senderPort, String message, String dispatchTo)
	{
		this(senderId, senderPort, message, 0, dispatchTo);
	}

	public P2PMessage(String senderId, int senderPort, MeanMeasurement measure, String dispatchTo)
	{
		this.senderId = senderId;
		this.senderPort = senderPort;
		this.message = "";
		this.measure = measure;
		this.timestamp = 0;
		this.dispatchto = dispatchTo;
	}

	public P2PMessage(String senderId, int senderPort, String message, long timestamp, String dispatchTo)
	{
		this.senderId = senderId;
		this.senderPort = senderPort;
		this.message = message;
		this.timestamp = timestamp;
		this.dispatchto = dispatchTo;
	}

	public void setSenderIp(String senderIp) { this.senderIp = senderIp; }

	public MeanMeasurement getMeasure() { return measure; }
	public String getSenderId() { return senderId; }
	public int getSenderPort() { return senderPort; }
	public String getMessage() { return message; }
	public long getTimestamp() { return timestamp; }
	public String getDispatchto() { return dispatchto; }
	public String getSenderIp() { return senderIp; }
}
