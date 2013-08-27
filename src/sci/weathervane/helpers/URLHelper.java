//package sci.weathervane.helpers;
//
//import java.util.Calendar;
//
//public class URLHelper 
//{
//	private URLHelper() {}
//	
//	public enum RUN
//	{
//		RUN_03 ("03"),
//		RUN_09 ("09"),
//		RUN_15 ("15"),
//		RUN_21 ("21");
//		
//		private final String run;
//		RUN(String run) { this.run = run; }
//	    public String getValue() { return run; }
//	}
//	
//	public enum SIMULATE_MODEL 
//	{
//	    EM ("em"),
//	    NMB ("nmb"),
//	    NMM ("nmm");
//
//	    private final String name;       
//
//	    private SIMULATE_MODEL(String s) 
//	    {
//	        name = s;
//	    }
//
//	    public boolean equals(String otherName)
//	    {
//	        return (otherName == null)? false:name.equals(otherName);
//	    }
//
//	    public String getValue()
//	    {
//	       return name;
//	    }
//	}
//	
//	public enum RESOLUTION
//	{
//		RESOLUTION_132 (132);
//		
//		private final int resolution;
//		RESOLUTION(int resolution) { this.resolution = resolution; }
//	    public int getValue() { return resolution; }
//	}
//	
//	public enum PERTURBATION
//	{
//	    CTL ("ctl"),
//	    N1 ("n1"),
//	    N2 ("n2"),
//	    N3 ("n3"),
//	    P1 ("p1"),
//	    P2 ("p2"),
//	    p3 ("p3");
//
//	    private final String name;       
//
//	    private PERTURBATION(String s) 
//	    {
//	        name = s;
//	    }
//
//	    public String getValue()
//	    {
//	       return name;
//	    }
//	}
//
//	public static String GenerateURL(Calendar day, RUN run, SIMULATE_MODEL simulate_model, RESOLUTION resolution, PERTURBATION ddd, short forcast_hour)
//	{
//		String dayString = Integer.toString(day.get(Calendar.YEAR)) + Integer.toString(day.get(Calendar.MONTH)) + Integer.toString(day.get(Calendar.DAY_OF_MONTH));
//		return "http://nomads.ncep.noaa.gov/pub/data/nccf/com/sref/prod/sref." + dayString + 
//				"/" + run.getValue() + "/pgrb/sref_" + simulate_model.getValue() + ".t" + run.getValue() + "z.pgrb" + 
//				resolution.getValue() + "." + ddd.getValue() + ".f" + Short.toString(forcast_hour) + ".grib2";
//	}
//}
