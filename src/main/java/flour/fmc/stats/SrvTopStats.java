package flour.fmc.stats;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

/**
 * Wrapper class for /top command output (server collected statistics)
 * 
 * @author Flourick
 */
public class SrvTopStats
{
	private final int deaths;
	private final String whoDeaths;

	private final int playerKills;
	private final String whoPlayerKills;

	private final int playTime;
	private final String whoPlayTime;

	private final int sinceDeath;
	private final String whoSinceDeath;

	private final int cakeSlices;
	private final String whoCakeSlices;

	public SrvTopStats(int deaths, String whoDeaths, int playerKills, String whoPlayerKills, int playTime,String whoPlayTime, int sinceDeath, String whoSinceDeath, int cakeSlices, String whoCakeSlices)
	{
		this.deaths = deaths;
		this.whoDeaths = whoDeaths;
		this.playerKills = playerKills;
		this.whoPlayerKills = whoPlayerKills;
		this.playTime = playTime;
		this.whoPlayTime = whoPlayTime;
		this.sinceDeath = sinceDeath;
		this.whoSinceDeath = whoSinceDeath;
		this.cakeSlices = cakeSlices;
		this.whoCakeSlices = whoCakeSlices;
	}

	public int getDeaths()
	{
		return deaths;
	}

	public int getPlayerKills()
	{
		return playerKills;
	}

	public int getPlayTime()
	{
		return playTime;
	}

	public int getSinceDeath()
	{
		return sinceDeath;
	}

	public int getCakeSlices()
	{
		return cakeSlices;
	}
	
	public String getWhoDeaths()
	{
		return whoDeaths;
	}

	public String getWhoPlayerKills()
	{
		return whoPlayerKills;
	}

	public String getWhoPlayTime()
	{
		return whoPlayTime;
	}

	public String getWhoSinceDeath()
	{
		return whoSinceDeath;
	}

	public String getWhoCakeSlices()
	{
		return whoCakeSlices;
	}

	public static String getFormattedTicks(int ticks)
	{
		int seconds = ticks / 20;

		long days = TimeUnit.SECONDS.toDays(seconds);
		seconds -= TimeUnit.DAYS.toSeconds(days);

		long hours = TimeUnit.SECONDS.toHours(seconds);
		seconds -= TimeUnit.HOURS.toSeconds(hours);

		long minutes = TimeUnit.SECONDS.toMinutes(seconds);
		seconds -= TimeUnit.MINUTES.toSeconds(minutes);

		String str = days + "d " + hours + "h " + minutes + "m " + seconds + "s";

		return str;
	}

	public static SrvTopStats getServerTopStats()
	{
		int topDeaths = 0;
		String topWhoDeaths = "?";

		int topPlayerKills = 0;
		String topWhoPlayerKills = "?";

		int topPlayTime = 0;
		String topWhoPlayTime = "?";

		int topSinceDeath = 0;
		String topWhoSinceDeath = "?";

		int topCakeSlices = 0;
		String topWhoCakeSlices = "?";

		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			int deaths = player.getStatistic(Statistic.DEATHS);
			int playerKils = player.getStatistic(Statistic.PLAYER_KILLS);
			int playTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
			int sinceDeath = player.getStatistic(Statistic.TIME_SINCE_DEATH);
			int cakeSlices = player.getStatistic(Statistic.CAKE_SLICES_EATEN);

			if(deaths > topDeaths) {
				topDeaths = deaths;
				topWhoDeaths = player.getName();
			}
			if(playerKils > topPlayerKills) {
				topPlayerKills = playerKils;
				topWhoPlayerKills = player.getName();
			}
			if(playTime > topPlayTime) {
				topPlayTime = playTime;
				topWhoPlayTime = player.getName();
			}
			if(sinceDeath > topSinceDeath) {
				topSinceDeath = sinceDeath;
				topWhoSinceDeath = player.getName();
			}
			if(cakeSlices > topCakeSlices) {
				topCakeSlices = cakeSlices;
				topWhoCakeSlices = player.getName();
			}
		}

		return new SrvTopStats(topDeaths, topWhoDeaths, topPlayerKills, topWhoPlayerKills, topPlayTime, topWhoPlayTime, topSinceDeath, topWhoSinceDeath, topCakeSlices, topWhoCakeSlices);
	}
}
