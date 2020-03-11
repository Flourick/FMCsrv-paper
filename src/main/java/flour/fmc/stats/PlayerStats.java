package flour.fmc.stats;

import java.sql.Timestamp;

/**
 * Wrapper class for PlayerStats table in database
 * 
 * @author Flourick
 */
public class PlayerStats
{
	private final String UUID;
	private final String name;
	private final Timestamp firstJoined;
	private final Timestamp lastJoined;
	private final int timesJoined;
	private final int maxLevelReached;
	
	public PlayerStats(String UUID, String name, Timestamp firstJoinTime, Timestamp lastJoinTime, int timesJoined, int maxLevelReached)
	{
		this.UUID = UUID;
		this.name = name;
		this.firstJoined = firstJoinTime;
		this.lastJoined = lastJoinTime;
		this.timesJoined = timesJoined;
		this.maxLevelReached = maxLevelReached;
	}

	public String getUUID()
	{
		return UUID;
	}

	public String getName()
	{
		return name;
	}

	public Timestamp getFirstJoined()
	{
		return firstJoined;
	}

	public Timestamp getLastJoined()
	{
		return lastJoined;
	}
	
	public int getTimesJoined()
	{
		return timesJoined;
	}
	
	public int getMaxLevelReached()
	{
		return maxLevelReached;
	}
}
