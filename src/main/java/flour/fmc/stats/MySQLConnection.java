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
	
	private final String playerStatsTableName = "PlayerStats";
	
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
            + "  (uuid            BINARY(16) PRIMARY KEY,"
            + "   name            VARCHAR(16) NOT NULL,"
            + "   first_joined    DATETIME,"
			+ "   last_joined     DATETIME,"
            + "   times_joined    INT NOT NULL DEFAULT 0)";
		
		st.execute(sqlCreatePlayerStats);
		
		return true;
	}
	
	public PlayerStats getPlayerStats(Player player)
	{
		PlayerStats pStats = null;
		String UUID = player.getUniqueId().toString();
		String name = player.getName();
		
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
			
			pStats = new PlayerStats(UUID, foundName, firstJoined, lastJoined, timesJoined);
		}
		catch(SQLException e) {
			exceptionLog = e.getMessage();
		}
		
		return pStats;
	}
	
	public PlayerStats getPlayerStats(String name)
	{
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
			
			pStats = new PlayerStats(myUUID, foundName, firstJoined, lastJoined, timesJoined);
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
	
	public boolean onPlayerJoin(Player player)
	{
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
