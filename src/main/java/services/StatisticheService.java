package services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

// aggiunta nuove statistiche da parte delle singole case (o a livello condominio)
// locali / globali
// + interfaccia admin per leggere ultime n statistiche

@Path("statistiche")
public class StatisticheService
{
	@GET
	@Produces({"application/xml"})
	public Response getStatistiche()
	{
		return Response.ok("statistiche").build();
	}

}
