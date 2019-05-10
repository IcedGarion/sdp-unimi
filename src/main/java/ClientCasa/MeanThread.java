package ClientCasa;

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
		if(buffer.size() >= 24)
		{
			// calcola media
			vedi metodi buffer.get24() e buffer.remove12() per calcolo media

			// rimuove i primi 12



			// chiamate REST per aggiungere statistiche
			// usa casaId e la media appena calcolata
		}

	}
}
