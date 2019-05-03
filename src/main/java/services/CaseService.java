package services;

import beans.Case;
import beans.Users;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("case")
public class CaseService
{
	@GET
	@Produces({"application/xml"})
	public Response getCaseList()
	{
		return Response.ok(Case.getInstance()).build();
	}
}
