package ClientCasa;

import ClientCasa.smartMeter.Measurement;
import ServerREST.beans.MeanMeasurement;
import ServerREST.beans.StatisticheLocali;

import java.util.ArrayList;
import java.util.List;

public class MeanThread extends Thread
{
	private SimulatorBuffer buffer;

	// id della casa che ha creato il buffer: serve per poi inserire in rest
	private String casaId;

	public MeanThread(SimulatorBuffer buffer, String casaId)
	{
		this.buffer = buffer;
		this.casaId = casaId;
	}

	@Override
	public void run()
	{
		List<Measurement> sensorData;
		Measurement element;
		MeanMeasurement computedMeasure;
		double mean;
		long timestampMin, timestampMax;

		while(true)
		{
			// all'inizio aspetta finche' il buffer si riempie
			if(buffer.size() >= 24)
			{
				// prende i primi 24
				sensorData = buffer.getTopBuffer();

				element = sensorData.get(0);
				timestampMax = timestampMin = element.getTimestamp();
				mean = element.getValue();

				// calcola media e intanto prende il timestamp MIN + MAX
				for(Measurement m: sensorData)
				{
					mean += m.getValue();

					if(m.getTimestamp() < timestampMin)
					{
						timestampMin = m.getTimestamp();
					}
					else if(m.getTimestamp() > timestampMax)
					{
						timestampMax = m.getTimestamp();
					}
				}

				// crea oggetto da mandare a REST
				mean = mean / sensorData.size();
				computedMeasure = new MeanMeasurement(mean, timestampMin, timestampMax);

				//chiamata REST a StatisticheService passando ID_CASA + MeanMeasurement
				POST("http://StatisticheLocali", computedMeasure);
			}
		}
	}
}
