package ClientAdmin.Notify;

import ClientAdmin.AdminApp;
import ServerREST.beans.Notifica;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;

/*
	RICEVE MESSAGGI DA SERVER ADMIN E LI STAMPA
 */
public class NotifyWorkerThread extends Thread
{
	private Socket listenSocket;

	public NotifyWorkerThread(Socket listenSocket)
	{
		this.listenSocket = listenSocket;
	}

	public void run()
	{
		JAXBContext jaxbContext;
		Unmarshaller unmarshaller;
		Notifica notifyMessage;
		String message;

		try
		{
			// legge e prepara campi msg ricevuto
			jaxbContext = JAXBContext.newInstance(Notifica.class);
			unmarshaller = jaxbContext.createUnmarshaller();

			notifyMessage = (Notifica) unmarshaller.unmarshal(listenSocket.getInputStream());
			message = notifyMessage.getMessage();
			listenSocket.close();

			System.out.println("NUOVA NOTIFICA!\n=============================\n" + message);
			AdminApp.refreshMenu();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
