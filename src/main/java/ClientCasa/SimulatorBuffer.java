package ClientCasa;

import ClientCasa.smartMeter.Buffer;
import ClientCasa.smartMeter.Measurement;

import java.util.ArrayList;
import java.util.List;

public class SimulatorBuffer implements Buffer
{
	// struttura dati da gestire con tutti i problemi di sincronizzazione
	// vedi tipo aggiunta nuova casa, in CondominioService
	private List<Measurement> theBuffer;

	public SimulatorBuffer()
	{
		theBuffer = new ArrayList<>();
	}

	public synchronized int size()
	{
		return theBuffer.size();
	}

	// ritorna i primi 24 (calcolo media)
	public synchronized List<Measurement> get24()
	{
		List<Measurement> ret = new ArrayList<>();
		int i = 0;

		for(Measurement m: theBuffer)
		{
			if(i >= 24 || i >= theBuffer.size())
				break;

			ret.add(m);
			i++;
		}

		return ret	;
	}

	// rimuove i primi 12 (calcolo media)
	public synchronized void remove12()
	{
		for(int i = 0; i < 12 && i < theBuffer.size(); i++)
			theBuffer.remove(0);
	}

	// chiamato dal simulatore smart meter vero e proprio: aggiunge sempre al buffer e basta
	@Override
	public synchronized void addMeasurement(Measurement m)
	{
		theBuffer.add(m);
	}
}
