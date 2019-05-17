package ClientAdmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdminApp
{
	public static void main(String args[]) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String choice;

		while(true)
		{
			System.out.println("ADMIN INTERFACE\n" +
					"0) Elenco case presenti nella rete\n" +
					"1) Ultime <n> statistiche relative ad una specifica <casa>" +
					"2) Ultime <n> statistiche condominiali" +
					"3) Deviazione standard e media delle ultime <n> statistiche prodotte da una specifica <casa>" +
					"4) Deviazione standard e media delle ultime <n> statistiche complessive condominiali");

			choice = in.readLine();

			if(choice.equals("0"))
			{

			}
			else if(choice.equals("1"))
			{

			}
			else if(choice.equals("2"))
			{

			}
			else if(choice.equals("3"))
			{

			}
			else if(choice.equals("4"))
			{

			}
			else
			{
				System.out.println("Inserire 0/1/2/3/4");
			}
		}
	}
}
