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

	// Admin interface: ritorna le statistiche
	@GET
	@Produces({"application/xml"})
	public Response getStatistiche()
	{
		return Response.ok(StatisticheLocali.getInstance()).build();
	}


	// Aggiunta nuova statistica locale media: riceve ID_CASA + MeanMeasurement da MeanThread;
	// chiama Statistiche_Locali.addMeanMeasurement
	// 404 NOT FOUND se la casa non si e' ancora registrata; 201 CREATED se ok
	@Path("add/{casaId}")
	@POST
	@Consumes({"application/xml"})
	public Response addStatistica(@PathParam("casaId") String casaId, MeanMeasurement m) throws URISyntaxException
	{
		LOGGER.log(Level.INFO, "statistiche/locali/add/" + casaId);

		System.out.println(casaId);
		System.out.println(m);
		System.out.flush();


		non stampa!

		synchronized(addLock)
		{
			// Casa esiste gia: inserisce
			if(StatisticheLocali.getInstance().addMeanMeasurement(casaId, m))
			{
				return Response.created(new URI("")).build();
			}
			// Casa non esiste
			else
			{
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}
	}
}
