//package sci.weathervane.helpers;
//
//import java.util.Calendar;
//
//import sci.weathervane.helpers.URLHelper.PERTURBATION;
//import sci.weathervane.helpers.URLHelper.RESOLUTION;
//import sci.weathervane.helpers.URLHelper.RUN;
//import sci.weathervane.helpers.URLHelper.SIMULATE_MODEL;
//
//public class FileNameHelper 
//{
//
//	private FileNameHelper()
//	{
//		
//	}
//	
//	public static String GenerateFileName(Calendar day, RUN run, SIMULATE_MODEL simulate_model, RESOLUTION resolution, PERTURBATION ddd, short forcast_hour)
//	{
//		String dayString = Integer.toString(day.get(Calendar.YEAR)) + Integer.toString(day.get(Calendar.MONTH)) + Integer.toString(day.get(Calendar.DAY_OF_MONTH));
//		return dayString + "." + run.getValue() + "." + simulate_model.getValue() + "." + run.getValue() + "." + 
//				resolution.getValue() + "." + ddd.getValue() + "." + Short.toString(forcast_hour);
//	}
//}
