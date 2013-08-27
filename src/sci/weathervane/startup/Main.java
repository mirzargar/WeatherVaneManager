package sci.weathervane.startup;

import java.util.Calendar;

import sci.weathervane.database.DatabaseManager;
import sci.weathervane.downloaders.DownloadManager;
import sci.weathervane.downloaders.Simulation;
import sci.weathervane.downloaders.DownloadableFactory;
import sci.weathervane.services.*;

public class Main 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
//		DatabaseManager.ClearTable("simulation");
//		AddAutomaticSimulations();
		DownloadManager.StartDownloadManager();
	}
	
	private static void AddAutomaticSimulations()
	{
		for (Simulation.RUN run : Simulation.RUN.values())
		{
			Calendar calendar = Calendar.getInstance();
			for (Simulation.SIMULATE_MODEL model : Simulation.SIMULATE_MODEL.values())
			{
				for (Simulation.RESOLUTION resolution : Simulation.RESOLUTION.values())
				{
					for (Simulation.PERTURBATION perturbation : Simulation.PERTURBATION.values())
					{
						for (Simulation.FORCAST_HOUR forcast_hour : Simulation.FORCAST_HOUR.values())
						{
							
							Simulation simulation = new Simulation(calendar, run, model, resolution, perturbation, forcast_hour, false, true);
							DatabaseManager.InsertSimulation(simulation); // insert the simulation into the database
						}
					}
				}
			}
		}
	}

}
