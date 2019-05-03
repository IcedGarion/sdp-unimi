package services;

import beans.Casa;
import beans.Case;
import beans.User;
import beans.Users;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("case")
public class CaseService
{
	// Restituisce elenco di tutte le case
	@GET
	@Produces({"application/xml"})
	public Response getCaseList()
	{
		return Response.ok(Case.getInstance()).build();
	}

	// Aggiunge una nuova casa (solo se non e' gia' presente)
	@Path("add")
	@POST
	@Consumes({"application/json", "application/xml"})
	public Response addCasa(Casa c)
	{







		// da controllare se non esiste gia, con un sync statement (tutto in uno)


		Case.getInstance().add(c);
		return Response.ok().build();
	}
}
