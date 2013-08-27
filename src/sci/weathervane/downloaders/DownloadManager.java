package sci.weathervane.downloaders;

import sci.weathervane.database.DatabaseManager;

public class DownloadManager 
{
	private DownloadManagerThread m_download_thread;
	private static DownloadManager s_instance;
	
	private DownloadManager()
	{
		m_download_thread = new DownloadManagerThread();
		
	}
	
	public static void StartDownloadManager()
	{
		if (s_instance == null)
		{
			s_instance = new DownloadManager();
			s_instance.m_download_thread.start();
		}
	}
	
	private class DownloadManagerThread extends Thread
	{
		
		public void run()
		{	
			while (true)
			{			
				try
				{
					Simulation simulation = DatabaseManager.GetNextReadySimulation();
					if (simulation != null)
					{
						simulation.Download();
						DatabaseManager.DeleteSimulation(simulation);
					}
					Thread.sleep(1000); // sleep for 1 second before checking again
				}
				catch (Exception exc)
				{
				exc.printStackTrace();
				}
			}
		}
		
	}

}
