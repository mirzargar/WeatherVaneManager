package sci.weathervane.downloaders;

import java.io.File;
import java.io.FileNotFoundException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import sci.weathervane.downloaders.Run.FORECAST;
import sci.weathervane.downloaders.Run.RUN;

public class RunManager 
{
	private static RunManager s_instance;
	private RunDownloadThread m_thread;
	
	public static RunManager Instance()
	{
		if (s_instance == null)
		{
			s_instance = new RunManager();
		}
		return s_instance;
	}
	
	private RunManager()
	{
		m_thread = new RunDownloadThread();
	}
	
	public void StartService()
	{
		if (!m_thread.isAlive()) // check so we don't start it more than once
		{
			m_thread.start();
		}
	}
	
	/**
	 * 
	 * @param skip_current_hour - this is used so you can choose to skip the current hour. If it's 3 am when you check but you want the next run time (06) and not 03 then pass in true
	 * @return
	 */
	private Calendar SetCalendarFromRun(Calendar cal, RUN run)
	{
		int run_int = Integer.parseInt(run.getValue());
		cal.set(Calendar.HOUR_OF_DAY, run_int);
		return cal;
	}
	
//	private static boolean DoesRunExist(Calendar run_calendar, RUN run) 
//	{
//		String month_string = (run_calendar.get(Calendar.MONTH) + 1 < 10) ? "0" + Integer.toString(run_calendar.get(Calendar.MONTH) + 1) : Integer.toString(run_calendar.get(Calendar.MONTH) + 1);
//		String day_string = (run_calendar.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + Integer.toString(run_calendar.get(Calendar.DAY_OF_MONTH)) : Integer.toString(run_calendar.get(Calendar.DAY_OF_MONTH));
//		String run_date_string = Integer.toString(run_calendar.get(Calendar.YEAR)) + month_string + day_string;
//		String run_value = run.getValue();
//		String path = Run.GetRootApplicationPath() + "downloaded/" + run_date_string + "/" + run_value;
//		File run_path = new File(path);
//		return (run_path.exists());
//	}

	/*
	 * Deletes downloaded and processed items where the run date is less than the passed in date
	 */
	private void DeleteRunsBeforeDate(Calendar expire_date)
	{
		System.out.println(expire_date.getTime());
		
		String downloaded_path = Run.GetRootApplicationPath() + "downloaded/";
		String processed_path = Run.GetRootApplicationPath() + "processed/";
		String[] paths = {downloaded_path, processed_path};
		ArrayList<File> directories_to_be_deleted = new ArrayList<File>();
		for (String path : paths)
		{
			File path_folder = new File(path);
			for (File date_run_folder : path_folder.listFiles())
			{
				if (!date_run_folder.isDirectory() || date_run_folder.getName().length() != 8)	{ continue;	} // skip files and other non-date folders
				int year = Integer.parseInt(date_run_folder.getName().substring(0, 4));
				int month = Integer.parseInt(date_run_folder.getName().substring(4, 6));
				int day = Integer.parseInt(date_run_folder.getName().substring(6, 8));
				Calendar date_calendar = Calendar.getInstance();
				date_calendar.set(Calendar.YEAR, year);
				date_calendar.set(Calendar.MONTH, month - 1);
				date_calendar.set(Calendar.DATE, day);
				if (date_calendar.before(expire_date))
				{
					directories_to_be_deleted.add(date_run_folder);
				}
			}
		}
		for (File folder_to_delete : directories_to_be_deleted) { folder_to_delete.delete(); } // delete the dirs
	}

	// MAHSA - rewritten the delete
	public static void deleteFolder(File folder) {
		if(!folder.isDirectory())
			// check whether the directory exists
			return;
		else{
		    File[] files = folder.listFiles();
		    if(files!=null) { //some JVMs return null for empty dirs
		        for(File f: files) {
		            if(f.isDirectory()) {
		                deleteFolder(f);
		            } else {
		                f.delete();
		            }
		        }
		    }
		    folder.delete();
		}
	}
	
	// MAHSA - deletes all the downloaded/processed files corresponding to the expire_date
	private void DeleteRun(String expire_date_NOAA, String expire_date_svg)
	{
		String downloaded_path = Run.GetRootApplicationPath() + "downloaded/" + expire_date_NOAA + "/";
		String processed_path = Run.GetRootApplicationPath() + "processed/" + expire_date_NOAA + "/";
		String svg_path = Run.GetRootApplicationPath() + "svg/" + expire_date_svg + "/";

		File download_folder = new File(downloaded_path);
		File processed_folder = new File(processed_path);
		File svg_folder = new File(svg_path);
		
		deleteFolder(download_folder);
		deleteFolder(processed_folder);		
		deleteFolder(svg_folder);
	}
	
	private class RunDownloadThread extends Thread
	{
		
		public String GetDateString(Calendar m_date)
		{
			String month_string = (m_date.get(Calendar.MONTH) + 1 < 10) ? "0" + Integer.toString(m_date.get(Calendar.MONTH) + 1) : Integer.toString(m_date.get(Calendar.MONTH) + 1);
			String day_string = (m_date.get(Calendar.DAY_OF_MONTH) < 10) ? "0" + Integer.toString(m_date.get(Calendar.DAY_OF_MONTH)) : Integer.toString(m_date.get(Calendar.DAY_OF_MONTH));
			return Integer.toString(m_date.get(Calendar.YEAR)) + month_string + day_string;
		}		
		
		@Override
		public void run() 
		{
			super.run();
			try
			{
//				String[] folders = {"500mb_TMP", "750mb_TMP"};
				ArrayList<RUN> runs = new ArrayList<RUN>(Arrays.asList(RUN.values()));
				RUN next_run_time = RUN.RUN_03;
				
				Calendar run_calendar = Calendar.getInstance();
				run_calendar.set(Calendar.MINUTE, 0);
				run_calendar.set(Calendar.SECOND, 0);
				run_calendar.set(Calendar.MILLISECOND, 0);
				while (true)
				{
					run_calendar = SetCalendarFromRun(run_calendar, next_run_time);
					Run next_run = new Run(run_calendar, next_run_time, FORECAST.SREF);
					while (next_run.GetCalendar().getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
					{
						Thread.sleep(60000); // sleep for 1 minute
					}
					String download_log = "Run manager: about to download " + GetDateString(Calendar.getInstance()); 
					System.out.println(download_log);
					next_run.DownloadAndProcessSimulations();
					System.out.println("Run manager: done downloading");
					next_run.ProcessRun();
					System.out.println("Run manager: done processing");
					// MAHSA
					//Calendar expire_date = Calendar.getInstance();
					//System.out.println(expire_date.getTime());					
					//expire_date.add(Calendar.DAY_OF_YEAR, -1);
					//System.out.println(next_run.GetDateString());
					//DeleteRunsBeforeDate(expire_date);
					
					// MAHSA added
					// compute the date to be deleted
					Calendar expire_date_NOAA = Calendar.getInstance(); 
					Calendar expire_date_svg = Calendar.getInstance();
					//System.out.println(expire_date.getTime());
					System.out.println(GetDateString(expire_date_NOAA));
					int DayBack_NOAA = -1;
					int DayBack_svg = -14;
					expire_date_NOAA.add(Calendar.DAY_OF_YEAR, DayBack_NOAA);
					expire_date_svg.add(Calendar.DAY_OF_YEAR, DayBack_svg);
					//System.out.println(expire_date.getTime());
					//System.out.println(GetDateString(expire_date));
					
					String delete_NOAA_log = "Run manager: about to delete NOAA files from" + GetDateString(expire_date_NOAA);
					String delete_svg_log = "Run manager: about to delete SVG files from" + GetDateString(expire_date_svg);
					
					System.out.println(delete_NOAA_log);
					System.out.println(delete_svg_log);
					
					DeleteRun(GetDateString(expire_date_NOAA), GetDateString(expire_date_svg));
										
					System.out.println("Run manager: done deleting");
					//System.exit(1);
					
					if (runs.indexOf(next_run_time) == runs.size() - 1)
					{
						next_run_time = runs.get(0);
						run_calendar.add(Calendar.DAY_OF_YEAR, 1);
					}
					else
					{
						next_run_time = runs.get(runs.indexOf(next_run_time) + 1);
					}
				}
			}
			catch (InterruptedException inter_exc)
			{
				System.out.println("RunManager: main run exception !!!");
			}
		}
		
	}
}
