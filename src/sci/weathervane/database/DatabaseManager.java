package sci.weathervane.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

import sci.weathervane.downloaders.Simulation;
import sci.weathervane.downloaders.Simulation.FORCAST_HOUR;
import sci.weathervane.downloaders.Simulation.PERTURBATION;
import sci.weathervane.downloaders.Simulation.RESOLUTION;
import sci.weathervane.downloaders.Simulation.RUN;
import sci.weathervane.downloaders.Simulation.SIMULATE_MODEL;

public class DatabaseManager 
{
	private static final String URL = "jdbc:mysql://localhost:3306/" + DatabaseManager.DATABASE;
	private static final String DATABASE = "weathervane";
	private static final String USERNAME = "weathervane_user";
	private static final String PASSWORD = "$56aB86?1#";
	
	private DatabaseManager() {}
	
	public static boolean ClearTable(String tablename)
	{
		boolean success = false;
		String statement_string = "DELETE FROM " + DATABASE + "." + tablename + "";
		Connection con = null;
		PreparedStatement prep_statement = null;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			
			prep_statement = con.prepareStatement(statement_string);
			prep_statement.executeUpdate();
			success = true;
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try 
			{
                if (prep_statement != null) 
                {
                	prep_statement.close();
                }
                if (con != null) 
                {
                    con.close();
                }

            } 
			catch (SQLException ex) 
            {
            }
		}
		return success;
	}

	public static Simulation GetNextReadySimulation() 
	{
		Simulation simulation = GetNextSimulation(true);
		if (simulation == null)
		{
			simulation = GetNextSimulation(false);
		}
		return simulation;
	}
	
	private static Simulation GetNextSimulation(boolean priority)
	{
		String priority_string = (priority) ? "1" : "0";
		Connection con = null;
		PreparedStatement prep_statement = null;
		ResultSet result_set = null;
		Simulation next_simulation = null;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			String priority_statement = "SELECT * FROM " + DATABASE + ".simulation as s WHERE s.priority=" + priority_string + " AND DATE(rundate) <= DATE(NOW()) AND CAST(CAST(run AS CHAR) AS UNSIGNED) <= HOUR(NOW()) order by id LIMIT 1";
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			prep_statement = con.prepareStatement(priority_statement);
			result_set = prep_statement.executeQuery();
			ArrayList<Simulation> priority_simulations = GetSimulationsFromStatement(result_set);
			if (priority_simulations.size() > 0)
			{
				next_simulation = priority_simulations.get(0);
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try 
			{
                if (result_set != null) 
                {
                	result_set.close();
                }
                if (prep_statement != null) 
                {
                	prep_statement.close();
                }
                if (con != null) 
                {
                    con.close();
                }

            } 
			catch (SQLException ex) 
            {
            }
		}
		return next_simulation;
	}
	
	private static ArrayList<Simulation> GetSimulationsFromStatement(ResultSet result_set)
	{
		ArrayList<Simulation> simulations = new ArrayList<Simulation>();
		try 
		{
			while (result_set.next())
			{
				Simulation simulation = GetSimulationFromResultSet(result_set);
				simulations.add(simulation);
			}
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return simulations;
	}
	
	private static Simulation GetSimulationFromResultSet(ResultSet result_set)
	{
		try
		{
			int id = result_set.getInt("id");
			RUN run = RUN.valueOf("RUN_" + result_set.getString("run"));
			SIMULATE_MODEL model = SIMULATE_MODEL.valueOf(result_set.getString("model").toUpperCase());
			RESOLUTION resolution = RESOLUTION.valueOf("RESOLUTION_" + result_set.getString("resolution"));
			PERTURBATION perturbation = PERTURBATION.valueOf(result_set.getString("perturbation").toUpperCase());
			FORCAST_HOUR forcast_hour = FORCAST_HOUR.valueOf("F" + result_set.getString("forcast_hour"));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(result_set.getDate("rundate"));
			boolean priority = result_set.getBoolean("priority");
			boolean repeat = result_set.getBoolean("repeat");
			Simulation simulation = new Simulation(calendar, run, model, resolution, perturbation, forcast_hour, priority, repeat);
			simulation.SetID(id);
			
			return simulation;
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
		return null;
	}
	
	public static boolean InsertSimulation(Simulation simulation) 
	{
		Connection con = null;
		PreparedStatement prep_statement = null;
		ResultSet generated_keys = null;
		int affected_rows = 0;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			String statement_string = "INSERT INTO " + DATABASE + ".simulation (run, model, resolution, perturbation, rundate, forcast_hour, priority, `repeat`) values (?,?,?,?,?,?,?,?)";
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			prep_statement = con.prepareStatement(statement_string, Statement.RETURN_GENERATED_KEYS);
			generated_keys = null;
			prep_statement.setString(1, simulation.GetRun());
			prep_statement.setString(2, simulation.GetModel());
			prep_statement.setString(3, Integer.toString(simulation.GetResolution()));
			prep_statement.setString(4, simulation.GetPerturbation());
			prep_statement.setDate(5, new Date(simulation.GetRunDateInMilliseconds()));
			prep_statement.setString(6, simulation.GetForcastHour());
			prep_statement.setBoolean(7, simulation.GetPriority());
			prep_statement.setBoolean(8, simulation.GetRepeat());
			
			affected_rows = prep_statement.executeUpdate();
			if (affected_rows > 0)
			{
				generated_keys = prep_statement.getGeneratedKeys();
				if (generated_keys.next())
				{
					simulation.SetID(generated_keys.getInt(1));
				}
				return true;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
                if (generated_keys != null) 
                {
                	generated_keys.close();
                }
                if (prep_statement != null) 
                {
                	prep_statement.close();
                }
                if (con != null) 
                {
                    con.close();
                }

            } 
			catch (SQLException ex) 
            {
				ex.printStackTrace();
            }
		}
		return false;
	}

	public static boolean DeleteSimulation(Simulation simulation) 
	{
		boolean success = false;
		String statement_string = "DELETE FROM " + DATABASE + ".simulation WHERE id=?";
		Connection con = null;
		PreparedStatement prep_statement = null;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			
			prep_statement = con.prepareStatement(statement_string);
			prep_statement.setInt(1, simulation.GetID());
			
			success = prep_statement.executeUpdate() == 1;
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try 
			{
                if (prep_statement != null) 
                {
                	prep_statement.close();
                }
                if (con != null) 
                {
                    con.close();
                }

            } 
			catch (SQLException ex) 
            {
            }
		}
		return success;
	}
}
