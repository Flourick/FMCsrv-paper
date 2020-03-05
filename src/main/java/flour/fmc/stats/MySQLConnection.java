package flour.fmc.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.entity.Player;

/**
 * Wrapper for JDBC MySQL driver and its calls
 * 
 * @author Flourick
 */
public class MySQLConnection
{
	private Connection conn;
	private Statement st;
	
	private final String connString;
	private final String username;
	private final String password;
	
	public String exceptionLog;
	
	public MySQLConnection(String connectionString, String username, String password)
	{
		this.connString = connectionString;
		this.username = username;
		this.password = password;
	}
	
	public boolean initialize()
	{
		try {
			conn = DriverManager.getConnection(connString, username, password);
			st = conn.createStatement();
			
			// creates tables if not already created
			createTables();
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
			return false;
		}
		
		return true;
	}
	
	public void close()
	{
		// silently close connection
		try { st.close(); } catch (Exception e) { }
		try { conn.close(); } catch (Exception e) { }
	}
	
	private boolean createTables() throws SQLException
	{
		//INSERT INTO t VALUES(UUID_TO_BIN(UUID(), true))
		
		String sqlCreatePlayerStats =
			  "CREATE TABLE IF NOT EXISTS PlayerStats"
            + "  (uuid            BINARY(16) PRIMARY KEY,"
            + "   name            VARCHAR(16) NOT NULL,"
            + "   first_joined    DATETIME,"
            + "   last_joined     DATETIME)";
		
		st.execute(sqlCreatePlayerStats);
		
		return true;
	}
	
	public boolean onPlayerJoin(Player player)
	{
		return true;
	}
	
	public boolean onPlayerQuit(Player player)
	{
		return true;
	}
}
