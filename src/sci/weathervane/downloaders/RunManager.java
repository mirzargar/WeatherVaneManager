package sci.weathervane.downloaders;

import java.io.File;
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
	
	private class RunDownloadThread extends Thread
	{

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
					next_run.DownloadAndProcessSimulations();
					next_run.ProcessRun();
					Calendar expire_date = Calendar.getInstance();
					expire_date.add(Calendar.DAY_OF_YEAR, -2);
					DeleteRunsBeforeDate(expire_date);
					
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
				
			}
		}
		
	}
}
