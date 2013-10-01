package sci.weathervane.downloaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
//import java.util.HashMap;

import org.apache.commons.io.*;

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
		RESOLUTION_132 (132),
//		RESOLUTION_221 (221),
		RESOLUTION_212 (212);
		
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
	
	public enum HEIGHT
	{
	    mb500 ("500 mb"),
	    mb700 ("700 mb");

	    private final String name;       

	    private HEIGHT(String s) 
	    {
	        name = s;
	    }

	    public String getValue()
	    {
	       return name;
	    }
	}
	
	public enum FIELD
	{
	    TMP ("TMP"),
//	    RH ("RH"),
	    HGT ("HGT");

	    private final String name;       

	    private FIELD(String s) 
	    {
	        name = s;
	    }

	    public String getValue()
	    {
	       return name;
	    }
	}
	
	private String GetFieldAndHeightCommand(FIELD field, HEIGHT height)
	{
		return ":" + field.getValue() + ":" + height.getValue();
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
		String url = "";
		switch (m_resolution)
		{
//		case RESOLUTION_221:
//			url = "http://nomads.ncep.noaa.gov/cgi-bin/filter_sref_na.pl?file=sref_" + m_simulate_model.getValue() + ".t" + m_run.GetRunValue() + "z.pgrb" + m_resolution.getValue() + "." + m_perturbation .getValue() + ".f" + m_forecast_hour.getValue() + ".grib2&lev_10_m_above_ground=on&lev_2_m_above_ground=on&lev_500_mb=on&lev_700_mb=on&lev_surface=on&var_APCP=on&var_HGT=on&var_RH=on&var_TMP=on&var_UGRD=on&var_VGRD=on&leftlon=0&rightlon=0&toplat=0&bottomlat=0&dir=%2Fsref." + GetDateString() + "%2F" + m_run.GetRunValue() + "%2Fpgrb"; 
//			break;
		case RESOLUTION_212:
			url = "http://nomads.ncep.noaa.gov/cgi-bin/filter_sref.pl?file=sref_" + m_simulate_model.getValue() + ".t" + m_run.GetRunValue() + "z.pgrb" + m_resolution.getValue() + "." + m_perturbation .getValue() + ".f" + m_forecast_hour.getValue() + ".grib2&lev_10_m_above_ground=on&lev_2_m_above_ground=on&lev_500_mb=on&lev_700_mb=on&lev_surface=on&var_APCP=on&var_HGT=on&var_RH=on&var_TMP=on&var_UGRD=on&var_VGRD=on&leftlon=0&rightlon=0&toplat=0&bottomlat=0&dir=%2Fsref." + GetDateString() + "%2F" + m_run.GetRunValue() + "%2Fpgrb";
			break;
		case RESOLUTION_132:
			url = "http://nomads.ncep.noaa.gov/cgi-bin/filter_sref_" + m_resolution.getValue() + ".pl?file=sref_" + m_simulate_model.getValue() + ".t" + m_run.GetRunValue() + "z.pgrb" + m_resolution.getValue() + "." + m_perturbation .getValue() + ".f" + m_forecast_hour.getValue() + ".grib2&lev_10_m_above_ground=on&lev_2_m_above_ground=on&lev_500_mb=on&lev_700_mb=on&lev_surface=on&var_APCP=on&var_HGT=on&var_RH=on&var_TMP=on&var_UGRD=on&var_VGRD=on&leftlon=0&rightlon=0&toplat=0&bottomlat=0&dir=%2Fsref." + GetDateString() + "%2F" + m_run.GetRunValue() + "%2Fpgrb";
			break;
		}
		return url;
	}
	
	public String GetProcessedFilePath(FIELD field, HEIGHT height)
	{
		String processed_path = m_run.PathToProcessedFiles() + field.getValue().replace(" ", "") + "/" + height.getValue().replace(" ", "") + "/";
		File file_path = new File(processed_path);
		if (!file_path.exists())
		{
			file_path.mkdirs();
		}
//		String processed_file_path = processed_path + m_simulate_model.getValue() + "." + m_resolution.getValue() + "." + m_perturbation.getValue() + "." + m_forecast_hour.getValue() + ".csv";
		String processed_file_path = processed_path + "sref_" +  m_simulate_model.getValue() + ".t" + this.GetRun().GetRunValue() + "z.pgrb" + m_resolution.getValue() + "." + m_perturbation.getValue() + ".f" + m_forecast_hour.getValue() + ".txt";
		return processed_file_path;
	}
	
	public String GetDownloadFilePath()
	{
		String download_path = m_run.PathToDownloadedFiles();
		File file_path = new File(download_path);
		if (!file_path.exists())
		{
			file_path.mkdirs();
		}
		String download_file_path = download_path + m_simulate_model.getValue() + "." + m_resolution.getValue() + "." + m_perturbation.getValue() + "." + m_forecast_hour.getValue();
		return download_file_path;
	}

	public boolean IsDownloaded() 
	{
		File file = new File(GetDownloadFilePath());
		return file.exists();
	}
	
	public synchronized void ProcessToCSV()
	{
		for (FIELD field : FIELD.values())
		{
			for (HEIGHT height : HEIGHT.values())
			{
				String processed_filepath = GetProcessedFilePath(field, height);
				String parameter = GetFieldAndHeightCommand(field, height);
				String command = PathToWgrib2() + " -match \"" + parameter + "\" " + GetDownloadFilePath() + " -csv " + processed_filepath  + "_tmp" + "\n";
		        try 
		        {
					ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
					Process proc = pb.start();
					
					proc.waitFor();
					cleanCSV(processed_filepath);
					File tmp_file = new File(processed_filepath  + "_tmp");
					tmp_file.delete();
				} 
		        catch (Exception e) 
		        {
					e.printStackTrace();
				}
				
				
			}
		}
	}
	
	private void cleanCSV(String processed_filepath)
	{
		try
		{
			FileWriter f0 = new FileWriter(processed_filepath);

			String newLine = System.getProperty("line.separator");
			
			BufferedReader br = new BufferedReader(new FileReader(processed_filepath + "_tmp"));

			String line;
			while ((line = br.readLine()) != null) 
			{
				String[] values = line.split(",");
				f0.write(values[4] +"," +values[5] + "," + values[6] + newLine);
			   // process the line.
			}
			br.close();
			f0.close();
		}
		catch (Exception exc){}
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

	public String GetTableName()
	{
		return m_run.GetDateString() + "_" + m_run.GetRunValue() + "_" + m_simulate_model.getValue() + "_" + m_perturbation.getValue() + "_" + m_forecast_hour.getValue();
	}
	
}
