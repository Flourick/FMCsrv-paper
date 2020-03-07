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
	
	public PlayerStats(String UUID, String name, Timestamp firstJoinTime, Timestamp lastJoinTime, int timesJoined)
	{
		this.UUID = UUID;
		this.name = name;
		this.firstJoined = firstJoinTime;
		this.lastJoined = lastJoinTime;
		this.timesJoined = timesJoined;
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
}
