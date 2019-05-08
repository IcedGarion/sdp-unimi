package services;

import beans.Casa;
import beans.Condominio;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("condominio")
public class CondominioService
{
	// LOCKS
	public static Object addLock = new Object();

	// altre eventuali variabili per i lock
	// public static volatile int someCount = 0;


	// Restituisce elenco di tutte le case
	@GET
	@Produces({"application/xml"})
	public Response getCaseList()
	{
		return Response.ok(Condominio.getInstance()).build();
	}

	// Aggiunge una nuova casa (solo se non e' gia' presente)
	// 409 conflict se esiste gia'; 201 created se ok
	@Path("add")
	@POST
	@Consumes({"application/xml"})
	public Response addCasa(Casa c) throws URISyntaxException
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
		synchronized(addLock)
		{
			// esiste: puo' rimuovere
			if(Condominio.getInstance().getByName(c.getName()) != null)
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
