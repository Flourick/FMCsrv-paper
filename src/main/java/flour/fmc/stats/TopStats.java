package flour.fmc.stats;

import java.sql.Timestamp;

/**
 * Wrapper class for /top command output
 * 
 * @author Flourick
 */
public class TopStats
{
	private final Timestamp firstJoined;
	private final String whoFirstJoined;
	
	private final Timestamp lastJoined;
	private final String whoLastJoined;
	
	private final int timesJoined;
	private final String whoTimesJoined;
	
	private final int maxLevelReached;
	private final String whoMaxLevelReached;

	public TopStats(Timestamp firstJoined, String whoFirstJoined, Timestamp lastJoined, String whoLastJoined, int timesJoined, String whoTimesJoined, int maxLevelReached, String whoMaxLevelReached)
	{
		this.firstJoined = firstJoined;
		this.whoFirstJoined = whoFirstJoined;
		this.lastJoined = lastJoined;
		this.whoLastJoined = whoLastJoined;
		this.timesJoined = timesJoined;
		this.whoTimesJoined = whoTimesJoined;
		this.maxLevelReached = maxLevelReached;
		this.whoMaxLevelReached = whoMaxLevelReached;
	}

	public Timestamp getFirstJoined()
	{
		return firstJoined;
	}

	public String getWhoFirstJoined()
	{
		return whoFirstJoined;
	}

	public Timestamp getLastJoined()
	{
		return lastJoined;
	}

	public String getWhoLastJoined()
	{
		return whoLastJoined;
	}

	public int getTimesJoined()
	{
		return timesJoined;
	}

	public String getWhoTimesJoined()
	{
		return whoTimesJoined;
	}

	public int getMaxLevelReached()
	{
		return maxLevelReached;
	}

	public String getWhoMaxLevelReached()
	{
		return whoMaxLevelReached;
	}
}
