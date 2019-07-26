package ServerREST.services;

import ServerREST.beans.CasaMeasurement;
import ServerREST.beans.MeanMeasurement;
import ServerREST.beans.StatisticheGlobali;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("statisticheGlobali")
public class StatisticheGlobaliService
{
	private static final Logger LOGGER = Logger.getLogger(StatisticheGlobaliService.class.getName());

	// Aggiunta stat globale da parte del coord; Id casa sara' "Condominio"
	// 201 CREATED
	@Path("add")
	@POST
	@Consumes({"application/xml"})
	public synchronized Response addStatisticaGlobale(MeanMeasurement m) throws URISyntaxException
	{
		LOGGER.log(Level.INFO, "POST statisticheGlobali/add/\n");

		StatisticheGlobali.getInstance().addMeanMeasurement(m);
		return Response.created(new URI("")).build();
	}


	// Lettura statistiche globali da parte dell'admin (ultime n stat)
	// 200 OK (anche se vuoto)
	@Path("get/{n}")
	@GET
	@Produces({"application/xml"})
	public synchronized Response getNStatistiche(@PathParam("n") String n)
	{
		LOGGER.log(Level.INFO, "GET statisticheGlobali/get/" + n + "\n");
		CasaMeasurement globalStats;

		globalStats = StatisticheGlobali.getInstance().getLastN(n);

		return Response.ok(globalStats).build();
	}
}
