package ServerREST.services;

import ServerREST.beans.Notifica;
import Shared.Configuration;
import Shared.MessageSenderThread;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
	RICEVE NOTIFICHE DALLA RETE P2P E LE INOLTRA A ADMIN (soltanto per notifica boost)
 */
@Path("notifiche")
public class NotificheService
{
	private static final Logger LOGGER = Logger.getLogger(NotificheService.class.getName());

	// POST /notifiche/add: nuova notifica
	// 201 created se ok
	@Path("add")
	@POST
	@Consumes({"application/xml"})
	public Response addCasa(Notifica n) throws URISyntaxException
	{
		MessageSenderThread sender;

		LOGGER.log(Level.INFO, "POST /notifiche/add/" + n.getSenderId() + "\n");

		// inoltra la notifica alla ADMIN APP
		try
		{
			sender = new MessageSenderThread(n.getSenderId(), Configuration.ADMIN_IP, Configuration.ADMIN_NOTIFY_PORT, n);
			sender.start();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.INFO, "Admin app OFFLINE");
		}

		return Response.created(new URI("")).build();
	}
}
