package ServerREST.services;

import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.Notifica;
import Shared.Configuration;
import Shared.MessageSenderThread;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("condominio")
public class CondominioService
{
	private static final Logger LOGGER = Logger.getLogger(CondominioService.class.getName());

	// LOCKS
	public static Object condominioLock = new Object();


	// Restituisce elenco di tutte le case
	@GET
	@Produces({"application/xml"})
	public Response getCaseList()
	{
		LOGGER.log(Level.INFO, "GET /condominio\n");

		synchronized(condominioLock)
		{
			return Response.ok(Condominio.getInstance()).build();
		}
	}

	// Aggiunge una nuova casa (solo se non e' gia' presente)
	// 409 conflict se esiste gia'; 201 created se ok
	@Path("add")
	@POST
	@Consumes({"application/xml"})
	public Response addCasa(Casa c) throws URISyntaxException
	{
		LOGGER.log(Level.INFO, "POST /condominio/add/" + c.getId() + "\n");

		synchronized(condominioLock)
		{
			MessageSenderThread sender;
			Notifica notifica;

			// esiste gia'
			if(Condominio.getInstance().getByName(c.getId()) != null)
			{
				return Response.status(Response.Status.CONFLICT).build();
			}
			// inserisce
			else
			{
				Condominio.getInstance().add(c);

				// inoltra la notifica alla ADMIN APP (nuova casa)
				try
				{
					notifica = new Notifica(c.getId(), "[ NEW ] Nuova casa aggiunta al condominio: " + c.getId(), new Date().getTime());
					sender = new MessageSenderThread(c.getId(), Configuration.ADMIN_IP, Configuration.ADMIN_NOTIFY_PORT, notifica);
					sender.start();
				}
				catch(Exception e)
				{
					LOGGER.log(Level.INFO, "Admin app OFFLINE");
				}

				return Response.created(new URI("")).build();
			}
		}
	}

	// Aggiunge una nuova casa (solo se non e' gia' presente)
	// 404 not found se non esiste; 204 no content se ok
	@Path("delete")
	@POST
	@Consumes({"application/xml"})
	public Response removeCasa(Casa c)
	{
		MessageSenderThread sender;
		Notifica notifica;

		LOGGER.log(Level.INFO, "POST /condominio/delete/" + c.getId() + "\n");

		synchronized(condominioLock)
		{
			// esiste: puo' rimuovere
			if(Condominio.getInstance().getByName(c.getId()) != null)
			{
				Condominio.getInstance().delete(c);

				// inoltra la notifica alla ADMIN APP (nuova casa)
				try
				{
					notifica = new Notifica(c.getId(), "[ EXIT ] Una casa e' uscita dal condominio: " + c.getId(), new Date().getTime());
					sender = new MessageSenderThread(c.getId(), Configuration.ADMIN_IP, Configuration.ADMIN_NOTIFY_PORT, notifica);
					sender.start();
				}
				catch(Exception e)
				{
					LOGGER.log(Level.INFO, "Admin app OFFLINE");
				}

				return Response.noContent().build();
			}
			// non esiste: errore
			else
			{
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}
	}
}
