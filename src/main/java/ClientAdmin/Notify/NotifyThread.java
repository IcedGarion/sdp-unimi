package ClientAdmin.Notify;

import java.net.ServerSocket;
import java.net.Socket;


/*
	SERVER CONCORRENTE CHE RICEVE MESSAGGI (notifica) E LANCIA I THREAD CHE LI GESTISCONO
 */
public class NotifyThread extends Thread
{
	private int notifyPort;

	public NotifyThread(int notifyPort)
	{
		this.notifyPort = notifyPort;
	}

	public void run()
	{
		ServerSocket welcomeSocket;
		Socket connectionSocket;
		NotifyWorkerThread notifyWorker;

		try
		{
			// crea server socket in ascolto
			welcomeSocket = new ServerSocket(notifyPort);

			while(true)
			{
				try
				{
					connectionSocket = welcomeSocket.accept();
					notifyWorker = new NotifyWorkerThread(connectionSocket);
					notifyWorker.start();

					// check TERMINAZIONE
					if(interrupted())
					{
						return;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
