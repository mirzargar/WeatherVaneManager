package sci.weathervane.startup;

import java.util.Calendar;

import sci.weathervane.database.DatabaseManager;
import sci.weathervane.downloaders.DownloadManager;
import sci.weathervane.downloaders.Run;
import sci.weathervane.downloaders.Run.FORECAST;
import sci.weathervane.downloaders.Run.RUN;
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
		DatabaseManager.ClearTable("simulation");
		DownloadRuns();
//		DownloadManager.StartDownloadManager();
	}
	
	private static void DownloadRuns()
	{
		for (RUN run : RUN.values())
		{
			Calendar calendar = Calendar.getInstance();
			Run run_obj = new Run(calendar, run, FORECAST.SREF, true);
			run_obj.DownloadAndProcessSimulations();
		}
	}

}
