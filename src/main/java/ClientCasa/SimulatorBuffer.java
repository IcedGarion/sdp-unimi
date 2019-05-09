package ClientCasa;

import ClientCasa.smartMeter.Buffer;
import ClientCasa.smartMeter.Measurement;

import java.util.ArrayList;
import java.util.List;

// unica classe da toccare per gestire le misurazioni
// simulator thread chiama addMeasurement qua sotto;
// questa le aggiunge al BUFFER e poi dialoga col server REST aggiungendo medie...




// da gestire tutto con sync? (vedi Condominio)
// forse non serve sync perche' ogni thread casa hai il suo buffer e nessuno glielo tocca
public class SimulatorBuffer implements Buffer
{
	// struttura dati da gestire con tutti i problemi di sincronizzazione
	// vedi tipo aggiunta nuova casa, in CondominioService
	private List<Measurement> theBuffer;

	public SimulatorBuffer()
	{
		theBuffer = new ArrayList<>();
	}

	// metodo sync, o comunque sync statement ?????
	@Override
	public void addMeasurement(Measurement m)
	{
		theBuffer.add(m);

		System.out.println(m);


		// chiamate REST per aggiungere statistiche

	}
}
