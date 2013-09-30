package sci.weathervane.downloaders;

//import java.io.BufferedReader;
import java.io.File;
//import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.ArrayList;

import sci.weathervane.downloaders.Simulation.FIELD;
import sci.weathervane.downloaders.Simulation.HEIGHT;
import sci.weathervane.downloaders.Simulation.RESOLUTION;

//import sci.weathervane.database.DatabaseManager;

public class Run 
{
	private Calendar m_date;
	private RUN m_run;
	private FORECAST m_forecast;
	private ArrayList<Simulation> m_simulations;
	private Integer m_simulation_download_index;
	
	private static String s_root_application_path;
	
	public enum FORECAST
	{
		GFS ("GFS"),
		SREF ("SREF");
		
		private final String forecast;
		FORECAST(String forecast) { this.forecast = forecast; }
	    public String getValue() { return forecast; }
	}
	
	public enum RUN
	{
		RUN_03 ("03"),
		RUN_09 ("09"),
		RUN_15 ("15"),
		RUN_21 ("21");
		
		private final String run;
		RUN(String run) { this.run = run; }
	    public String getValue() { return run; }
	}
	
//	public enum HEIGHT
//	{
//		SURFACE ("surface"),
//		MB_500 ("500 mb"),
//		MB_750 ("750 mb"),
//		M_2_ABOVE_GROUND ("2 m above ground"),
//		M_10_ABOVE_GROUND ("10 m above ground");
//		
//		private final String m_height;
//		HEIGHT(String height) { this.m_height = height; }
//	    public String getValue() { return m_height; }
//	    public static String getEnumDatabaseString()
//	    {
//	    	String enum_string = "";
//	    	for (HEIGHT height : HEIGHT.values())
//	    	{
//	    		enum_string += "'" + height.getValue() + "',";
//	    	}
//	    	enum_string = (enum_string.length() > 0) ? enum_string.substring(0, enum_string.lastIndexOf(',')) : ""; // remove the last ','
//	    	return "ENUM(" + enum_string + ")";
//	    }
//	}
	
	public enum VAR
	{
		APCP ("APCP"),
		HGT ("HGT"),
		TMP ("TMP"),
		RH ("RH"),
		UGRD ("UGRD"),
		VGRD ("VGRD");
		
		private final String m_var;
		VAR(String var) { this.m_var = var; }
	    public String getValue() { return m_var; }
	    public static String getEnumDatabaseString()
	    {
	    	String enum_string = "";
	    	for (VAR var : VAR.values())
	    	{
	    		enum_string += "'" + var.getValue() + "',";
	    	}
	    	enum_string = (enum_string.length() > 0) ? enum_string.substring(0, enum_string.lastIndexOf(',')) : ""; // remove the last ','
	    	return "ENUM(" + enum_string + ")";
	    }
	    
	    public String GetFieldType()
		{
			String field_name = "";
			switch (this)
			{
			case APCP:
				field_name = "INT";
				break;
			case HGT:
				field_name = "DOUBLE";
				break;
			case RH:
				field_name = "INT";
				break;
			case TMP:
				field_name = "DOUBLE";
				break;
			case UGRD:
				field_name = "DOUBLE";
				break;
			case VGRD:
				field_name = "DOUBLE";
				break;
			}
			return field_name;
		}
	}
	
	public Run(Calendar _date, RUN _run, FORECAST _forecast)
	{
		m_simulation_download_index = 0;
		m_date = _date;
		m_run = _run;
		m_forecast = _forecast;
		m_simulations = new ArrayList<Simulation>();
		m_date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m_run.getValue()));
		m_date.set(Calendar.MINUTE, 0);
		m_date.set(Calendar.SECOND, 0);
		m_date.set(Calendar.MILLISECOND, 0);
//		DatabaseManager.DropRunTable(this);
//		DatabaseManager.CreateRunTable(this);
		
		for (Simulation.SIMULATE_MODEL model : Simulation.SIMULATE_MODEL.values())
		{
//			for (Simulation.RESOLUTION resolution : Simulation.RESOLUTION.values())
			{
				RESOLUTION resolution = RESOLUTION.RESOLUTION_212; // this is hard coded, later we will want to remove this
				for (Simulation.PERTURBATION perturbation : Simulation.PERTURBATION.values())
				{
					for (Simulation.FORCAST_HOUR forcast_hour : Simulation.FORCAST_HOUR.values())
					{
						m_simulations.add(new Simulation(this, model, resolution, perturbation, forcast_hour, false, true));
					}
				}
			}
		}
	}
	
	public String GetTableName()
	{
		return "run_" +  this.GetDateString() + "_" + this.GetRunValue();
	}
	
	public Calendar GetCalendar()
	{
		return m_date;
	}
	
	public String GetDateString()
	{
		String month_string = (m_date.get(Calendar.MONTH) + 1 < 10) ? "0" + Integer.toString(m_date.get(Calendar.MONTH) + 1) : Integer.toString(m_date.get(Calendar.MONTH) + 1);
		String day_string = (m_date.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + Integer.toString(m_date.get(Calendar.DAY_OF_MONTH)) : Integer.toString(m_date.get(Calendar.DAY_OF_MONTH));
		return Integer.toString(m_date.get(Calendar.YEAR)) + month_string + day_string;
	}
	
	public boolean DownloadAndProcessSimulations()
	{
		for (int i = 0; i < 5; ++i)
		{
			DownloadSimulationThread t1 = new DownloadSimulationThread();
			t1.start();
		}
		
		WaitForDownload();
		return true;
	}
	
	private void WaitForDownload() 
	{
		try
		{
			while(true)
			{
				synchronized (m_simulation_download_index) 
				{
					if (m_simulation_download_index >= m_simulations.size()) { return; }
				}
				Thread.sleep(500);
			}
		}
		catch (Exception exc) {}
	}
	
	private class DownloadSimulationThread extends Thread
	{
		public void run()
		{
			int tmp_download_index = 0;
			while (true)
			{
				synchronized (m_simulation_download_index) 
				{
					tmp_download_index = m_simulation_download_index;
					if (tmp_download_index >= m_simulations.size()) { break; } // make sure we don't get out of range
					++m_simulation_download_index;
				}
				Simulation sim = m_simulations.get(tmp_download_index);
				sim.Download();
				sim.ProcessToCSV();
//				sim.ExtractAndAddToDatabase();
			}
		}
	}
	
	public static void SetRootApplicationPath(String path)
	{
		s_root_application_path = path;
	}
	
	public static String GetRootApplicationPath()
	{
		if (s_root_application_path != null)
		{
			return s_root_application_path;
		}
		String os = System.getProperty("os.name").toLowerCase();
		String path = "";
		if (os.indexOf("win") >= 0)
		{
			path = "C:\\Users\\Public\\weathervane\\";
		}
		else if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) // is mac or windows
		{
			path = "/Users/dillonl/Documents/weathervane/";
		}
		File path_file = new File(path);
		if (!path_file.exists())
		{
			path_file.mkdirs();
		}
		return path;
	}
	
	public String PathToDownloadedFiles()
	{
		return GetRootApplicationPath() + "downloaded/" + this.GetDateString() + "/" + this.GetRunValue() + "/";
	}

	public String PathToProcessedFiles()
	{
		return GetRootApplicationPath() + "processed/" + this.GetDateString() + "/" + this.GetRunValue() + "/";
	}
	
	public String PathToSVGFiles()
	{
		return GetRootApplicationPath() + "svg/" + this.GetDateString() + "/" + this.GetRunValue() + "/";
	}
	
	
	/*
	 * 
	 * 	500MB, TMP, levelset = 248 (-25C)
		500MB, TMP, levelset = 253 (-20C)
		500MB, TMP, levelset = 258 (-15C)
	
		700MB, TMP, levelset = 283 (+10C)
		700MB, TMP, levelset = 288 (+15C)
		
		500MB, HGT, levelset = 5460
		500MB, HGT, levelset = 5580
		500MB, HGT, levelset = 5700
		500MB, HGT, levelset = 5820
	 */
	private int CalculateLevelSet(Simulation.FIELD field, Simulation.HEIGHT height)
	{
		int level_set = 240;
		if (field == FIELD.TMP)
		{
			if (height == HEIGHT.mb500)
			{
				level_set = 253;
			}
			else if (height == HEIGHT.mb700)
			{
				level_set = 288;
			}
		}
		else if (field == FIELD.HGT)
		{
			level_set = 5580;
		}
		return level_set;
	}
	
	public void ProcessRun()
	{
		for (Simulation.FIELD field : Simulation.FIELD.values())
		{
			for (Simulation.HEIGHT height : Simulation.HEIGHT.values())
			{
				int level_set = CalculateLevelSet(field, height);
				String field_height_path = field.getValue().replace(" ", "") + "/" + height.getValue().replace(" ", "") + "/";
				String svg_path = PathToSVGFiles() + field_height_path + level_set;
				File svg_run_folder = new File(svg_path);
				File processed_folder = new File(PathToProcessedFiles() + field_height_path);
				if (!processed_folder.exists()) { continue; }
				if (!svg_run_folder.exists())
				{
					svg_run_folder.mkdirs();
				}
				String command = GetSVGApplicationPath() + " " + PathToProcessedFiles() + field_height_path + "/ " + this.GetResolution() + " " + this.m_run.getValue() + " " + level_set + " " + svg_path + "/\n";
		        try 
		        {
					ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
					Process proc = pb.start();
					proc.waitFor();
					
//					String output = "";
//					String line = "";
//					BufferedReader input = new BufferedReader
//			            (new InputStreamReader(proc.getInputStream()));
//			        while ((line = input.readLine()) != null) {
//			            output += (line + '\n');
//			        }
//			        input.close();
				} 
		        catch (Exception e) 
		        {
					e.printStackTrace();
				}
			}
		}
	}
	
	private int GetResolution() 
	{
		return this.m_simulations.get(0).GetResolution();
	}

	public static String GetSVGApplicationPath()
	{
		String os = System.getProperty("os.name").toLowerCase();
		String path = "";
		if (os.indexOf("win") >= 0)
		{
			path = GetRootApplicationPath() + "\\application\\main.exe";
		}
		else if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) // is mac or windows
		{
			path = GetRootApplicationPath() + "/application/main";
		}
		return path;
	}

	public String GetRunValue() 
	{
		return m_run.getValue();
	}

	public String GetForecastValue() 
	{	
		return m_forecast.getValue();
	}
	
}
