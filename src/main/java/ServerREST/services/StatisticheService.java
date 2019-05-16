package ServerREST.services;

import ServerREST.beans.CasaMeasurement;
import ServerREST.beans.MeanMeasurement;
import ServerREST.beans.StatisticheLocali;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

// aggiunta nuove statistiche da parte delle singole case (locali)
// + interfaccia admin per leggere ultime n statistiche

@Path("statisticheLocali")
public class StatisticheService
{
	private static final Logger LOGGER = Logger.getLogger(StatisticheService.class.getName());
	public static Object localStatLock = new Object();

	// Ritorna le ultime n statistiche relative ad una casaId (CasaMeasurement, una lista di MeanMeasurement)
	// 404 NOT FOUND se non esistono ancora statistiche associate a casaID; 200 OK altrimenti
	@Path("get/{casaId}/{n}")
	@GET
	@Produces({"application/xml"})
	public Response getNStatistiche(@PathParam("casaId") String casaId, @PathParam("n") String n)
	{
		LOGGER.log(Level.INFO, "GET statisticheLocali/get/" + casaId + "/" + n + "\n");
		CasaMeasurement stats;

		synchronized(localStatLock)
		{
			stats = StatisticheLocali.getInstance().getLastN(casaId, n);
			if(stats == null)
			{
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			else
			{
				return Response.ok(stats).build();
			}
		}
	}


	// Aggiunta nuova statistica locale media: riceve ID_CASA + MeanMeasurement da MeanThread;
	// chiama Statistiche_Locali.addMeanMeasurement
	// 201 CREATED se la casa non si era ancora registrata (crea lista vuota); 204 NO CONTENT se ok
	@Path("add/{casaId}")
	@POST
	@Consumes({"application/xml"})
	public Response addStatistica(@PathParam("casaId") String casaId, MeanMeasurement m) throws URISyntaxException
	{
		LOGGER.log(Level.INFO, "POST statisticheLocali/add/" + casaId + "\n");

		synchronized(localStatLock)
		{
			// Casa esiste gia: inserisce aggiornando la lista delle misurazioni per quella casa
			if(StatisticheLocali.getInstance().addMeanMeasurement(casaId, m))
			{
				return Response.noContent().build();
			}
			// Casa non esiste: messaggio di risposta differente
			else
			{
				return Response.created(new URI("")).build();
			}
		}
	}

}
