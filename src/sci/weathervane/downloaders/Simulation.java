package sci.weathervane.downloaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.*;

import sci.weathervane.database.DatabaseManager;
import sci.weathervane.downloaders.Run.HEIGHT;
import sci.weathervane.downloaders.Run.VAR;

public class Simulation 
{
	
	private int m_id;
	private Run m_run;
	private SIMULATE_MODEL m_simulate_model;
	private RESOLUTION m_resolution;
	private PERTURBATION m_perturbation;
	private FORCAST_HOUR m_forecast_hour;
	private boolean m_priority;
	private boolean m_repeat;
	
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
	    P3 ("p3");

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
	
	public Run GetRun()
	{
		return m_run;
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
	
	public String GetForcastHour()
	{
		return m_forecast_hour.getValue(); 
	}
	
	public boolean GetPriority()
	{
		return m_priority;
	}
	
	public boolean GetRepeat()
	{
		return m_repeat;
	}
	
	public Simulation(Run _run, SIMULATE_MODEL _simulate_model, RESOLUTION _resolution, PERTURBATION _perturbation, FORCAST_HOUR _forcast_hour, boolean _priority, boolean _repeat)
	{
		this.m_run = _run;
		this.m_simulate_model = _simulate_model;
		this.m_resolution = _resolution;
		this.m_perturbation = _perturbation;
		this.m_forecast_hour = _forcast_hour;
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
		DownloadFile(GenerateURL(), GetDownloadFilePath());
	}

	public void SetID(int _id) 
	{
		this.m_id = _id;
	}
	
	private String GetDateString()
	{
		return m_run.GetDateString();
	}
	
	public String GenerateURL()
	{
		return "http://nomads.ncep.noaa.gov/cgi-bin/filter_sref_" + m_resolution.getValue() + ".pl?file=sref_" + m_simulate_model.getValue() + ".t" + m_run.GetRunValue() + "z.pgrb" + m_resolution.getValue() + "." + m_perturbation .getValue() + ".f" + m_forecast_hour.getValue() + ".grib2&lev_10_m_above_ground=on&lev_2_m_above_ground=on&lev_500_mb=on&lev_750_mb=on&lev_surface=on&var_APCP=on&var_HGT=on&var_RH=on&var_TMP=on&var_UGRD=on&var_VGRD=on&leftlon=0&rightlon=0&toplat=0&bottomlat=0&dir=%2Fsref." + GetDateString() + "%2F" + m_run.GetRunValue() + "%2Fpgrb";
	}
	
	public String GetProcessedFilePath()
	{
		String processed_path = PathToProcessedFiles();
		File file_path = new File(processed_path);
		if (!file_path.exists())
		{
			file_path.mkdirs();
		}
		return processed_path + m_simulate_model.getValue() + "." + m_perturbation.getValue() + "." + m_forecast_hour.getValue();
	}
	
	public String GetDownloadFilePath()
	{
		String download_path = PathToDownloadedFiles();
		File file_path = new File(download_path);
		if (!file_path.exists())
		{
			file_path.mkdirs();
		}
		return download_path + GetDateString() + "." + m_run.GetRunValue() + "." + m_simulate_model.getValue() + "." + 
				m_resolution.getValue() + "." + m_perturbation.getValue() + "." + m_forecast_hour.getValue();
	}

	public boolean IsDownloaded() 
	{
		File file = new File(GetDownloadFilePath());
		return file.exists();
	}
	
	public synchronized void ExtractAndAddToDatabase() 
	{
		String command = PathToWgrib2() + " " + GetDownloadFilePath() + " -inv /dev/null -csv -";
//		String command = PathToWgrib2() + " -match \":TMP\" " + GetDownloadFilePath() + " -csv " + GetProcessedFilePath();
		
		try 
		{
			Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            
            String line;
            HashMap<String, Integer> lat_lon_map = new HashMap<String, Integer>(); // this maps the lat_lon to a 0-based index
            HashMap<Integer, String> insert_map = new HashMap<Integer, String>(); // this maps the 0-based index to an insert string
            long start = System.currentTimeMillis();
            int counter = 0;
            while ((line = input.readLine()) != null)
            {
            	String[] split_string = line.split(",");
            	String lat_lon = split_string[4] + "_" + split_string[5];
            	Integer index = lat_lon_map.get(lat_lon);
            	
            	if (index == null) 
            	{ 
            		index = counter;
            		lat_lon_map.put(lat_lon, index);
            	}
            	String insert_string = insert_map.get(index);
            	if (insert_string == null)
            	{
            		insert_string = DatabaseManager.GetSimulationInsertRowString(index, this);
            	}
            	insert_map.put(index, insert_string.replace(DatabaseManager.SimulationValuePlaceHolder(split_string[3].replace("\"", ""), split_string[2].replace("\"", "")), GetInsertValue(split_string[2], split_string[6])));
            	++counter;
            }
            long end = System.currentTimeMillis();
            long minutes = (end - start) / 60000;
            System.out.println("Grib filter completed in " + minutes + " minutes");
            
            start = System.currentTimeMillis();
            DatabaseManager.CreateSimulationTable(this, insert_map);
            end = System.currentTimeMillis();
            minutes = (end - start) / 60000;
            System.out.println("Insert " + insert_map.size() +" rows completed in " + minutes + " minutes");
            int x = 0;
        } 
		catch(Exception e) 
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }
	}

	private String GetInsertValue(String var, String value)
	{
		String return_value = value;
//		VAR var_enum = VAR.valueOf(var);
//		switch (var_enum)
//		{
//		case APCP:
//			break;
//		case HGT:
//			break;
//		case RH:
//			break;
//		case TMP:
//			break;
//		case UGRD:
//			break;
//		case VGRD:
//			break;
//		}
		return return_value;
	}

	public static String PathToWgrib2()
	{
		String os = System.getProperty("os.name").toLowerCase();
		String path = "";
		if (os.indexOf("win") >= 0)
		{
			path = "C:\\Program Files (x86)\\wgrib\\wgrib2.exe";
		}
		else if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) // is mac or windows
		{
			path = "/usr/local/grib2/wgrib2/wgrib2";
		}
		return path;
	}
	
	public static String PathToDownloadedFiles()
	{
		String os = System.getProperty("os.name").toLowerCase();
		String path = "";
		if (os.indexOf("win") >= 0)
		{
			path = "C:\\Users\\Public\\weathervane\\downloads\\";
		}
		else if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) // is mac or windows
		{
			path = "~/weathervane/downloads/";
		}
		return path;
	}
	
	public static String PathToProcessedFiles()
	{
		String os = System.getProperty("os.name").toLowerCase();
		String path = "";
		if (os.indexOf("win") >= 0)
		{
			path = "C:\\Users\\Public\\weathervane\\processed\\";
		}
		else if (os.indexOf("mac") >= 0 || os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") >= 0) // is mac or windows
		{
			path = "~/weathervane/processed/";
		}
		return path;
	}

	public String GetTableName()
	{
		return m_run.GetDateString() + "_" + m_run.GetRunValue() + "_" + m_simulate_model.getValue() + "_" + m_perturbation.getValue() + "_" + m_forecast_hour.getValue();
	}
	
}
