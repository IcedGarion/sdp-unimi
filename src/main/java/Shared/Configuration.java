package Shared;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;

/*
	CARICA DA FILE LE COSTANTI E LE RENDE ACCESSIBILI A TUTTI
 */
public class Configuration
{
	public static final String CONFIGURATION_FILE = "resources/properties.config";

	public static String SERVER_URL;
	public static String ADMIN_IP;
	public static int ADMIN_NOTIFY_PORT;
	public static String CASA_ID;
	public static String CASA_IP;
	public static int CASA_STATS_PORT;
	public static int CASA_ELECTION_PORT;
	public static int CASA_BOOST_PORT;
	public static Level LOGGER_LEVEL;

	public static void loadProperties()
	{
		Properties properties = new Properties();

		try(FileInputStream input = new FileInputStream(CONFIGURATION_FILE))
		{
			properties.load(input);

			SERVER_URL = properties.getProperty("SERVER_URL");
			ADMIN_IP = properties.getProperty("ADMIN_IP");
			ADMIN_NOTIFY_PORT = Integer.parseInt(properties.getProperty("ADMIN_NOTIFY_PORT"));
			CASA_ID = properties.getProperty("CASA_ID");
			CASA_IP = properties.getProperty("CASA_IP");
			CASA_STATS_PORT = Integer.parseInt(properties.getProperty("CASA_STATS_PORT"));
			CASA_ELECTION_PORT = Integer.parseInt(properties.getProperty("CASA_ELECTION_PORT"));
			CASA_BOOST_PORT = Integer.parseInt(properties.getProperty("CASA_BOOST_PORT"));

			// FINE (tutto, tracing) - INFO (start/stop thread + election / boost) - SEVERE (solo errori)
			String loggerLevel = properties.getProperty("LOGGER_LEVEL");
			switch(loggerLevel)
			{
				case "ALL":
					LOGGER_LEVEL = Level.ALL;
					break;
				case "FINE":
					LOGGER_LEVEL = Level.FINE;
					break;
				case "INFO":
					LOGGER_LEVEL = Level.INFO;
					break;
				case "SEVERE":
					LOGGER_LEVEL = Level.SEVERE;
					break;
				default:
					System.err.println("Error loading configuration file: expected 'LOGGER_LEVEL' to be 'ALL'/'FINE'/'INFO'/'SEVERE' but got " + loggerLevel + ". Setting INFO level");
					LOGGER_LEVEL = Level.INFO;
					break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
}
