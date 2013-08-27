package sci.weathervane.downloaders;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.io.*;

import sci.weathervane.database.DatabaseManager;

public class Simulation 
{
	public static final String DOWNLOADS_PATH = "/tmp/weathervane/";
	
	private int m_id;
	private Calendar m_run_date;
	private RUN m_run;
	private SIMULATE_MODEL m_simulate_model;
	private RESOLUTION m_resolution;
	private PERTURBATION m_perturbation;
	private FORCAST_HOUR m_forcast_hour;
	private boolean m_priority;
	private boolean m_repeat;
	
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
	
	public enum SIMULATE_MODEL 
	{
	    EM ("em"),
	    NMB ("nmb"),
	    NMM ("nmm");

	    private final String name;       

	    private SIMULATE_MODEL(String s) 
	    {
	        name = s;
	    }

	    public boolean equals(String otherName)
	    {
	        return (otherName == null)? false:name.equals(otherName);
	    }

	    public String getValue()
	    {
	       return name;
	    }
	}
	
	public enum RESOLUTION
	{
		RESOLUTION_132 (132);
		
		private final int resolution;
		RESOLUTION(int resolution) { this.resolution = resolution; }
	    public int getValue() { return resolution; }
	}
	
	public enum PERTURBATION
	{
	    CTL ("ctl"),
	    N1 ("n1"),
	    N2 ("n2"),
	    N3 ("n3"),
	    P1 ("p1"),
	    P2 ("p2"),
	    p3 ("p3");

	    private final String name;       

	    private PERTURBATION(String s) 
	    {
	        name = s;
	    }

	    public String getValue()
	    {
	       return name;
	    }
	}
	
	public enum FORCAST_HOUR
	{
		F00 ("00"),
		F03 ("03"),
		F06 ("06"),
		F09 ("09"),
		F12 ("12"),
		F15 ("15"),
		F18 ("18"),
		F21 ("21"),
		F24 ("24"),
		F27 ("27"),
		F30 ("30"),
		F33 ("33"),
		F36 ("36"),
		F39 ("39"),
		F42 ("42"),
		F45 ("45"),
		F48 ("48"),
		F51 ("51"),
		F54 ("54"),
		F57 ("57"),
		F60 ("60"),
		F63 ("63"),
		F66 ("66"),
		F69 ("69"),
		F72 ("72"),
		F75 ("75"),
		F78 ("78"),
		F81 ("81"),
		F84 ("84"),
		F87 ("87");
		
		private final String name;       

	    private FORCAST_HOUR(String s) 
	    {
	        name = s;
	    }

	    public String getValue()
	    {
	       return name;
	    }
	}
	
	public int GetID()
	{
		return m_id;
	}
	
	public String GetRun()
	{
		return m_run.getValue();
	}
	
	public String GetModel()
	{
		return m_simulate_model.getValue();
	}
	
	public int GetResolution()
	{
		return m_resolution.getValue();
	}
	
	public String GetPerturbation()
	{
		return m_perturbation.getValue();
	}
	
	public long GetRunDateInMilliseconds()
	{
		return m_run_date.getTimeInMillis();
	}
	
	public String GetForcastHour()
	{
		return m_forcast_hour.getValue(); 
	}
	
	public boolean GetPriority()
	{
		return m_priority;
	}
	
	public boolean GetRepeat()
	{
		return m_repeat;
	}
	
	public Simulation(Calendar _day, RUN _run, SIMULATE_MODEL _simulate_model, RESOLUTION _resolution, PERTURBATION _perturbation, FORCAST_HOUR _forcast_hour, boolean _priority, boolean _repeat)
	{
		this.m_run_date = _day;
		this.m_run = _run;
		this.m_simulate_model = _simulate_model;
		this.m_resolution = _resolution;
		this.m_perturbation = _perturbation;
		this.m_forcast_hour = _forcast_hour;
		this.m_priority = _priority;
		this.m_repeat = _repeat;
	}
	
	private void DownloadFile(String url_string, String file_name)
	{
		File file = new File(file_name);
		if (!file.exists()) 
		{
			try
			{
				File tmp_file = new File(file_name + "_tmp");
				URL url = new URL(url_string);
				FileUtils.copyURLToFile(url, tmp_file);
				tmp_file.renameTo(file);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
			}
		}
	}

	public void Download() 
	{
		DownloadFile(GenerateURL(), GetFileName());
		DatabaseManager.DeleteSimulation(this);
		if (m_repeat)
		{
			this.m_run_date.add(Calendar.HOUR, 24); // add 24 hours to the run date
			DatabaseManager.InsertSimulation(this);
		}
	}

	public void SetID(int _id) 
	{
		this.m_id = _id;
	}
	
	private String GetDateString()
	{
		String month_string = (m_run_date.get(Calendar.MONTH) + 1 < 10) ? "0" + Integer.toString(m_run_date.get(Calendar.MONTH) + 1) : Integer.toString(m_run_date.get(Calendar.MONTH) + 1);
		String day_string = (m_run_date.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + Integer.toString(m_run_date.get(Calendar.DAY_OF_MONTH)) : Integer.toString(m_run_date.get(Calendar.DAY_OF_MONTH));
		return Integer.toString(m_run_date.get(Calendar.YEAR)) + month_string + day_string;
	}
	
	public String GenerateURL()
	{
		return "http://nomads.ncep.noaa.gov/pub/data/nccf/com/sref/prod/sref." + GetDateString() + 
				"/" + m_run.getValue() + "/pgrb/sref_" + m_simulate_model.getValue() + ".t" + m_run.getValue() + "z.pgrb" + 
				m_resolution.getValue() + "." + m_perturbation .getValue() + ".f" + m_forcast_hour.getValue() + ".grib2";
	}
	
	public String GetFileName()
	{
		File file_path = new File(DOWNLOADS_PATH);
		if (!file_path.exists())
		{
			file_path.mkdirs();
		}
		return DOWNLOADS_PATH + GetDateString() + "." + m_run.getValue() + "." + m_simulate_model.getValue() + "." + m_run.getValue() + "." + 
				m_resolution.getValue() + "." + m_perturbation.getValue() + "." + m_forcast_hour.getValue();
	}

	public boolean IsDownloaded() 
	{
		File file = new File(GetFileName());
		return file.exists();
	}
	
}
