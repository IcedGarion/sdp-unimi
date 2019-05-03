package services;

import beans.Casa;
import beans.Condominio;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("condominio")
public class CondominioService
{
	// LOCKS
	public static Object addLock = new Object();

	// altre eventuali variabili per i lock
	// public static volatile int someCount = 0;


	// Restituisce elenco di tutte le case
	@GET
	@Produces({"application/json", "application/xml"})
	public Response getCaseList()
	{
		return Response.ok(Condominio.getInstance()).build();
	}

	// Aggiunge una nuova casa (solo se non e' gia' presente)
	@Path("add")
	@POST
	@Consumes({"application/json", "application/xml"})
	public Response addCasa(Casa c)
	{
		synchronized(addLock)
		{
			// esiste gia'
			if(Condominio.getInstance().getByName(c.getName()) != null)
			{
				return Response.status(Response.Status.CONFLICT).build();
			}
			// inserisce
			else
			{
				Condominio.getInstance().add(c);
				return Response.ok().build();
			}
		}
	}
}
