package ServerREST.services;

import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
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
			// esiste gia'
			if(Condominio.getInstance().getByName(c.getId()) != null)
			{
				return Response.status(Response.Status.CONFLICT).build();
			}
			// inserisce
			else
			{
				Condominio.getInstance().add(c);
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
		LOGGER.log(Level.INFO, "POST /condominio/delete/" + c.getId() + "\n");

		synchronized(condominioLock)
		{
			// esiste: puo' rimuovere
			if(Condominio.getInstance().getByName(c.getId()) != null)
			{
				Condominio.getInstance().delete(c);
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
