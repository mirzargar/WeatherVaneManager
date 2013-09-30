package sci.weathervane.startup;

import sci.weathervane.downloaders.Run;
import sci.weathervane.downloaders.RunManager;

public class Main 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		if (args.length > 0)
		{
			String path = (args[0].endsWith("/")) ? args[0] : args[0] + "/";
			Run.SetRootApplicationPath(path);
		}
		RunManager.Instance().StartService();
	}

}
