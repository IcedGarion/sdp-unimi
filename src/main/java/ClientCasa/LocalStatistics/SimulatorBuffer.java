package ClientCasa.LocalStatistics;

import ClientCasa.LocalStatistics.smartMeter.Buffer;
import ClientCasa.LocalStatistics.smartMeter.Measurement;

import java.util.ArrayList;
import java.util.List;

public class SimulatorBuffer implements Buffer
{
	// struttura dati da gestire con tutti i problemi di sincronizzazione
	// vedi tipo aggiunta nuova casa, in CondominioService
	private List<Measurement> theBuffer;
	private final int WINDOW_SIZE = 24;
	private final double OVERLAPPING = WINDOW_SIZE*0.5;

	public SimulatorBuffer()
	{
		theBuffer = new ArrayList<>();
	}

	public synchronized int size()
	{
		return theBuffer.size();
	}

	// ritorna i primi 24 (calcolo media)
	public synchronized List<Measurement> getTopBuffer()
	{
		List<Measurement> ret = new ArrayList<>();

		for(int i=0; i < WINDOW_SIZE && i < theBuffer.size(); i++)
		{
			ret.add(theBuffer.get(i));
		}

		// dopo aver preso i primi 24, cancella i primi 12
		removeTopBuffer();

		return ret;
	}

	// rimuove i primi 12 (calcolo media)
	private synchronized void removeTopBuffer()
	{
		for(int i = 0; i < OVERLAPPING && i < theBuffer.size(); i++)
		{
			theBuffer.remove(0);
		}
	}

	// chiamato dal simulatore smart meter vero e proprio: aggiunge sempre al buffer e basta
	@Override
	public synchronized void addMeasurement(Measurement m)
	{
		theBuffer.add(m);
	}
}
