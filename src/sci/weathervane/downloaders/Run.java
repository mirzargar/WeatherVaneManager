package sci.weathervane.downloaders;

import java.util.Calendar;
import java.util.ArrayList;

import sci.weathervane.database.DatabaseManager;

public class Run 
{
	private Calendar m_date;
	private RUN m_run;
	private FORECAST m_forecast;
	private boolean m_repeat;
	private ArrayList<Simulation> m_simulations;
	private Integer m_simulation_download_index;
	
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
	
	public enum HEIGHT
	{
		SURFACE ("surface"),
		MB_500 ("500 mb"),
		MB_750 ("750 mb"),
		M_2_ABOVE_GROUND ("2 m above ground"),
		M_10_ABOVE_GROUND ("10 m above ground");
		
		private final String m_height;
		HEIGHT(String height) { this.m_height = height; }
	    public String getValue() { return m_height; }
	    public static String getEnumDatabaseString()
	    {
	    	String enum_string = "";
	    	for (HEIGHT height : HEIGHT.values())
	    	{
	    		enum_string += "'" + height.getValue() + "',";
	    	}
	    	enum_string = (enum_string.length() > 0) ? enum_string.substring(0, enum_string.lastIndexOf(',')) : ""; // remove the last ','
	    	return "ENUM(" + enum_string + ")";
	    }
	}
	
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
	}
	
	public Run(Calendar _date, RUN _run, FORECAST _forecast, boolean _repeat)
	{
		m_simulation_download_index = 0;
		m_date = _date;
		m_run = _run;
		m_forecast = _forecast;
		m_repeat = _repeat;
		m_simulations = new ArrayList<Simulation>();
		DatabaseManager.DropRunTable(this);
		DatabaseManager.CreateRunTable(this);
		
		for (Simulation.SIMULATE_MODEL model : Simulation.SIMULATE_MODEL.values())
		{
			for (Simulation.RESOLUTION resolution : Simulation.RESOLUTION.values())
			{
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
	
	public String GetDateString()
	{
		String month_string = (m_date.get(Calendar.MONTH) + 1 < 10) ? "0" + Integer.toString(m_date.get(Calendar.MONTH) + 1) : Integer.toString(m_date.get(Calendar.MONTH) + 1);
		String day_string = (m_date.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + Integer.toString(m_date.get(Calendar.DAY_OF_MONTH)) : Integer.toString(m_date.get(Calendar.DAY_OF_MONTH));
		return Integer.toString(m_date.get(Calendar.YEAR)) + month_string + day_string;
	}
	
	public boolean DownloadAndProcessSimulations()
	{
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1; ++i)
		{
			DownloadSimulationThread t1 = new DownloadSimulationThread();
			t1.start();
		}
		
		WaitForDownload();
		long end = System.currentTimeMillis();
		float total_minutes = ((float)(end - start)) / 60000f;
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
				sim.ExtractAndAddToDatabase();
			}
		}
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
