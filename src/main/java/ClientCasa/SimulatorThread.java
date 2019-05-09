package ClientCasa;


public class SimulatorThread extends Thread
{
	private int delay;

	public SimulatorThread(int delay)
	{
		this.delay = delay;
	}

	public void run()
	{
		try
		{
			while(true)
			{
				System.out.println("lol");



				sleep(delay);
			}

		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}

	}
}
