package sci.weathervane.services;

import java.util.ArrayList;

import sci.weathervane.downloaders.Simulation;


public class NOAAUpdateService 
{
	private static NOAAUpdateService s_instance = null;
	private static final int SLEEP_INTERVAL = 36000;
	
	private NOAAUpdateThread m_update_thread;
	private ArrayList<Simulation> m_downloadables;
	
	public static NOAAUpdateService Instance()
	{
		if (s_instance == null)
		{
			s_instance = new NOAAUpdateService();
		}
		return s_instance;
	}
	
	private NOAAUpdateService()
	{
		m_update_thread = new NOAAUpdateThread();
		m_downloadables = new ArrayList<Simulation>();
	}
	
	public void StartService()
	{
		
		synchronized (m_update_thread) 
		{
			// if the thread has not been started then start the thread
			if (!m_update_thread.isAlive())
			{
				m_update_thread.start();
			}
		}
	}
	
	public void StopService()
	{
		synchronized (m_update_thread) 
		{
			// if the thread has not been started then start the thread
			if (m_update_thread.isAlive())
			{
				m_update_thread.StopUpdater();
			}
		}
	}
	
	public void PushDownloadable(Simulation downloadable)
	{
		synchronized (m_downloadables) 
		{
			m_downloadables.add(downloadable);
		}
	}
	
	private class NOAAUpdateThread extends Thread
	{
		private boolean m_stop_thread;
		
		public void StopUpdater()
		{
			synchronized(this)
			{
				m_stop_thread = true;
				notify();
			}
		}
		
		public void run()
		{
			m_stop_thread = false;
			try
			{
				synchronized (this) 
				{
					while (!m_stop_thread)
					{
						
						this.wait(SLEEP_INTERVAL); // wait until the SLEEP_INTERVAL or until someone notifies us to wake
						if (m_stop_thread) // if StopUpdater has been called then exit
						{
							continue;
						}
						GetNOAAUpdate();
					}
				}
			}
			catch (InterruptedException inter_exc)
			{
				inter_exc.printStackTrace();
			}
		}
		
		private void GetNOAAUpdate()
		{
			
		}
		
	}
}
