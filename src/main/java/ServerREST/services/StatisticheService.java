package ServerREST.services;

import ClientCasa.MeanThread;
import ServerREST.beans.Casa;
import ServerREST.beans.Condominio;
import ServerREST.beans.MeanMeasurement;
import ServerREST.beans.StatisticheLocali;

import javax.ws.rs.*;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

// aggiunta nuove statistiche da parte delle singole case (o a livello condominio)
// locali / globali
// + interfaccia admin per leggere ultime n statistiche

@Path("statisticheLocali")
public class StatisticheService
{
	private static final Logger LOGGER = Logger.getLogger(StatisticheService.class.getName());
	public static Object addLock = new Object();

	// Admin interface: ritorna le ultime n statistiche relative ad una casaId
	// Ritorna CasaMeasurement, una lista di MeanMeasurement
	@Path("get/{casaId}")
	@GET
	@Produces({"application/xml"})
	public Response getNStatistiche(@PathParam("casaId") String casaId)
	{
		return Response.ok(StatisticheLocali.getInstance().getLastN(casaId, 10)).build();
	}


	// Aggiunta nuova statistica locale media: riceve ID_CASA + MeanMeasurement da MeanThread;
	// chiama Statistiche_Locali.addMeanMeasurement
	// 201 CREATED se la casa non si era ancora registrata (crea lista vuota); 204 NO CONTENT se ok
	@Path("add/{casaId}")
	@POST
	@Consumes({"application/xml"})
	public Response addStatistica(@PathParam("casaId") String casaId, MeanMeasurement m) throws URISyntaxException
	{
		LOGGER.log(Level.INFO, "POST statistiche/locali/add/" + casaId);

		synchronized(addLock)
		{
			// Casa esiste gia: inserisce aggiornando la lista delle misurazioni per quella casa
			if(StatisticheLocali.getInstance().addMeanMeasurement(casaId, m))
			{
				return Response.noContent().build();
			}
			// Casa non esiste: crea lista vuota corrispondente al suo ID
			else
			{
				StatisticheLocali.getInstance().addNewCasa(casaId);
				return Response.created(new URI("")).build();
			}
		}
	}

}
