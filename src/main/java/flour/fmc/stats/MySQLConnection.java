package flour.fmc.stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
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
	private PreparedStatement getPlayerByNameStatement;
	
	private final String playerStatsTableName = "PlayerStatsTest";
	
	private final String connString;
	private final String username;
	private final String password;
	
	private String exceptionLog = null;
	
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
			getPlayerByNameStatement = conn.prepareStatement("SELECT * FROM " + playerStatsTableName + " WHERE name = ?");
			
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
		String sqlCreatePlayerStats =
			  "CREATE TABLE IF NOT EXISTS " + playerStatsTableName
            + "  (uuid                BINARY(16) PRIMARY KEY,"
            + "   name                VARCHAR(16) NOT NULL,"
            + "   first_joined        DATETIME,"
			+ "   last_joined         DATETIME,"
            + "   times_joined        INT NOT NULL DEFAULT 0,"
            + "   max_level_reached   INT NOT NULL DEFAULT 0)";
		
		st.execute(sqlCreatePlayerStats);
		
		return true;
	}
	
	public PlayerStats getPlayerStats(Player player)
	{
		testConnectivity();

		PlayerStats pStats = null;
		String UUID = player.getUniqueId().toString();
		//String name = player.getName();
		
		String sqlGetPlayer = "SELECT * FROM " + playerStatsTableName + " WHERE uuid = UNHEX('" + UUID.replace("-", "") + "')";
		
		try(ResultSet results = st.executeQuery(sqlGetPlayer)) {
			if(!results.next()) {
				// player not found
				return null;
			}
			
			String foundName = results.getString("name");
			Timestamp firstJoined = results.getTimestamp("first_joined");
			Timestamp lastJoined = results.getTimestamp("last_joined");
			int timesJoined = results.getInt("times_joined");
			int maxLevelReached = results.getInt("max_level_reached");
			
			pStats = new PlayerStats(UUID, foundName, firstJoined, lastJoined, timesJoined, maxLevelReached);
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
		}
		
		return pStats;
	}
	
	public PlayerStats getPlayerStats(String name)
	{
		testConnectivity();

		PlayerStats pStats = null;
		ResultSet results = null;
		
		try {
			getPlayerByNameStatement.setString(1, name);
			results = getPlayerByNameStatement.executeQuery();
			
			if(!results.next()) {
				// player not found
				return null;
			}
			
			String myUUID = toUUID(results.getBytes("uuid")).toString();
			String foundName = results.getString("name");
			Timestamp firstJoined = results.getTimestamp("first_joined");
			Timestamp lastJoined = results.getTimestamp("last_joined");
			int timesJoined = results.getInt("times_joined");
			int maxLevelReached = results.getInt("max_level_reached");
			
			pStats = new PlayerStats(myUUID, foundName, firstJoined, lastJoined, timesJoined, maxLevelReached);
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
		}
		finally {
			try { results.close(); } catch (Exception e) { }
			try { getPlayerByNameStatement.clearParameters(); } catch (SQLException e) { }
		}
		
		return pStats;
	}
	
	public TopStats getTopStats()
	{
		testConnectivity();

		TopStats tStats = null;
		ResultSet results = null;
		
		String sqlGetTop = 
				"(SELECT name, first_joined, null AS last_joined, -1 AS times_joined, -1 AS max_level_reached FROM (SELECT MIN(first_joined) AS g FROM " + playerStatsTableName + ") AS f JOIN (SELECT name, first_joined FROM " + playerStatsTableName + ") AS e ON f.g = e.first_joined) "
				+ "UNION "
				+ "(SELECT name, null AS first_joined, last_joined, -1 AS times_joined, -1 AS max_level_reached FROM (SELECT MAX(last_joined) AS g FROM " + playerStatsTableName + ") AS f JOIN (SELECT name, last_joined FROM " + playerStatsTableName + ") AS e ON f.g = e.last_joined) "
				+ "UNION "
				+ "(SELECT name, null AS first_joined, null AS last_joined, times_joined, -1 AS max_level_reached FROM (SELECT MAX(times_joined) AS g FROM " + playerStatsTableName + ") AS f JOIN (SELECT name, times_joined FROM " + playerStatsTableName + ") AS e ON f.g = e.times_joined) "
				+ "UNION "
				+ "(SELECT name, null AS first_joined, null AS last_joined, -1 AS times_joined, max_level_reached FROM (SELECT MAX(max_level_reached) AS g FROM " + playerStatsTableName + ") AS f JOIN (SELECT name, max_level_reached FROM " + playerStatsTableName + ") AS e ON f.g = e.max_level_reached)";
		
		try {
			results = st.executeQuery(sqlGetTop);
			
			Timestamp firstJoined = null;
			String whoFirstJoined = "<EMPTY>";

			Timestamp lastJoined = null;
			String whoLastJoined = "<EMPTY>";

			int timesJoined = -1;
			String whoTimesJoined = "<EMPTY>";

			int maxLevelReached = -1;
			String whoMaxLevelReached = "<EMPTY>";
			
			if(!results.next()) {
				// empty select???
				return null;
			}
			else {
				do {
					String curName = results.getString("name");
					Timestamp curFirstJoined = results.getTimestamp("first_joined");
					Timestamp curLastJoined = results.getTimestamp("last_joined");
					int curTimesJoined = results.getInt("times_joined");
					int curMaxLevelReached = results.getInt("max_level_reached");
					
					if(curFirstJoined != null) {
						firstJoined = curFirstJoined;
						whoFirstJoined = curName;
					}
					
					if(curLastJoined != null) {
						lastJoined = curLastJoined;
						whoLastJoined = curName;
					}
					
					if(curTimesJoined != -1) {
						timesJoined = curTimesJoined;
						whoTimesJoined = curName;
					}
					
					if(curMaxLevelReached != -1) {
						maxLevelReached = curMaxLevelReached;
						whoMaxLevelReached = curName;
					}
					
				}
				while(results.next());
			}
			
			tStats = new TopStats(firstJoined, whoFirstJoined, lastJoined, whoLastJoined, timesJoined, whoTimesJoined, maxLevelReached, whoMaxLevelReached);
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
		}
		finally {
			try { results.close(); } catch (Exception e) { }
			try { getPlayerByNameStatement.clearParameters(); } catch (SQLException e) { }
		}
		
		return tStats;
	}
	
	public boolean onPlayerJoin(Player player)
	{
		testConnectivity();

		String UUID = player.getUniqueId().toString();
		String name = player.getName();
		
		String sqlGetPlayer = "SELECT * FROM " + playerStatsTableName + " WHERE uuid = UNHEX('" + UUID.replace("-", "") + "')";
		
		try(ResultSet results = st.executeQuery(sqlGetPlayer)) {
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());
			
			if(results.next()) {
				// player already has a record so we update his last join time and his name if he changed it
				String foundName = results.getString("name");
				int joinCount = results.getInt("times_joined");
				
				if(!foundName.equals(name)) {
					name = foundName;
				}
				
				String sqlUpdatePlayer = "UPDATE " + playerStatsTableName + " SET name = '" + name + "', last_joined = '" + ts + "', times_joined = " + ++joinCount + " WHERE uuid = UNHEX('" + UUID.replace("-", "") + "')";
				st.execute(sqlUpdatePlayer);
			}
			else {
				// player does not have a record yet so we create one
				String sqlNewPlayer = "INSERT INTO " + playerStatsTableName + " (uuid, name, first_joined, last_joined, times_joined) VALUES (UNHEX(REPLACE('" + UUID.replace("-", "") + "', '-', '')), '" + name + "', '" + ts + "', '" + ts + "', 1)";
				st.execute(sqlNewPlayer);
			}
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
			return false;
		}
		
		return true;
	}
	
	public boolean onPlayerLevelUp(Player player)
	{
		testConnectivity();

		String UUID = player.getUniqueId().toString();
		
		String sqlGetPlayer = "SELECT * FROM " + playerStatsTableName + " WHERE uuid = UNHEX('" + UUID.replace("-", "") + "')";
		
		try(ResultSet results = st.executeQuery(sqlGetPlayer)) {
			if(results.next()) {
				int curMaxLevelReached = results.getInt("max_level_reached");

				if(player.getLevel() > curMaxLevelReached) {
					// player hit a new max!
					String sqlUpdatePlayer = "UPDATE " + playerStatsTableName + " SET max_level_reached = '" + player.getLevel() + "' WHERE uuid = UNHEX('" + UUID.replace("-", "") + "')";
					st.execute(sqlUpdatePlayer);
				}
			}
			else {
				// PLAYER DOES NOT EXIST???
				exceptionLog = "PLAYER DOES NOT HAVE A RECORD IN DATABASE!!!";
				return false;
			}
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
			return false;
		}
		
		return true;
	}
	
	public String getExceptionLog()
	{
		return exceptionLog;
	}
	
	public void clearExceptionLog()
	{
		exceptionLog = null;
	}

	private void testConnectivity()
	{
		try {
			conn.isValid(3);
		}
		catch (SQLException e) {
			// only thrown for invalid timeout, so yeah, nothing to do here.
		}
	}
	
	private static UUID toUUID(byte[] bytes)
	{
		if(bytes.length != 16) {
			throw new IllegalArgumentException();
		}
		
		int i = 0;
		
		long msl = 0;
		for(; i < 8; i++) {
			msl = (msl << 8) | (bytes[i] & 0xFF);
		}
		
		long lsl = 0;
		for(; i < 16; i++) {
			lsl = (lsl << 8) | (bytes[i] & 0xFF);
		}
		
		return new UUID(msl, lsl);
	}
}
