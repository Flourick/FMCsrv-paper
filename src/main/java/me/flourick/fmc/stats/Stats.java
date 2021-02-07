package me.flourick.fmc.stats;

import java.text.SimpleDateFormat;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import me.flourick.fmc.FMC;
import me.flourick.fmc.utils.CConfig;
import me.flourick.fmc.utils.EmptyTabCompleter;
import me.flourick.fmc.utils.IModule;

/**
 * Module for collecting various statistics
 * 
 * @author Flourick
 */
public class Stats implements IModule, CommandExecutor
{
	private final FMC fmc;
	private boolean isEnabled = false;
	
	private final CConfig statsConfig;
	
	public final StatsSQLConnection sql;
	
	public Stats(FMC fmc)
	{
		this.fmc = fmc;
		this.statsConfig = new CConfig(fmc, "stats.yml");
		
		// Creates default config if not present
		statsConfig.saveDefaultConfig();
		
		// jdbc:mysql://hostname:port/database?autoReconnect=true
		String connString = "jdbc:mysql://" + statsConfig.getConfig().getString("hostname") + ":" + statsConfig.getConfig().getInt("port") + "/" + statsConfig.getConfig().getString("database") + "?autoReconnect=true";
		
		this.sql = new StatsSQLConnection(connString, statsConfig.getConfig().getString("username"), statsConfig.getConfig().getString("password"));
	}
	
	@Override
	public boolean onEnable()
	{
		if(statsConfig.getConfig().getString("hostname").equals("hostname")) {
			// default config file
			fmc.getLogger().log(Level.WARNING, "[Stats] Default values in stats.yml! Please change them and restart/reload the server.");
			onDisable();
			return false;
		}
		
		boolean sqlCorrect = sql.initialize();
		if(!sqlCorrect) {
			fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
			sql.clearExceptionLog();
			onDisable();
			return false;
		}
		
		fmc.getLogger().log(Level.INFO, "[Stats] Successfully initialized MySQL database connection!");

		fmc.getCommand("statistics").setExecutor(this);
		fmc.getCommand("statistics").setTabCompleter(new StatsTabCompleter());
		fmc.getCommand("topstats").setExecutor(this);
		fmc.getCommand("topstats").setTabCompleter(new EmptyTabCompleter());
		
		// listen for player join and update his join times
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event)
			{
				Player player = event.getPlayer();

				if(fmc.getServer().hasWhitelist()) {
					if(event.getPlayer().isWhitelisted()) {
						if(!sql.onPlayerJoin(player)) {
							fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
							sql.clearExceptionLog();
						}
					}
				}
				else {
					if(!sql.onPlayerJoin(player)) {
						fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
						sql.clearExceptionLog();
					}
				}
			}
		}, fmc);
		
		// listening to a level change
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerLevelChange(PlayerLevelChangeEvent event)
			{
				Player player = event.getPlayer();
				
				// LEVEL UP!
				if(event.getNewLevel() > event.getOldLevel()) {
					if(!sql.onPlayerLevelUp(player)) {
						fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
						sql.clearExceptionLog();
					}
				}
			}
		}, fmc);
		
		return isEnabled = true;
	}

	@Override
	public void onDisable()
	{
		sql.close();
		isEnabled = false;
	}
	
	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}

	@Override
	public String getName()
	{
		return "Stats";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		if(cmd.getName().toLowerCase().equals("statistics")) {
			if(!(sender instanceof Player)) {
				if(args.length == 1) {
					// showing stats for someone else
					PlayerStats pStats = sql.getPlayerStats(args[0]);
					if(pStats == null) {
						sender.sendMessage(ChatColor.RED + "Could not get " + args[0] + "\'s statistics!");
						
						if(sql.getExceptionLog() != null) {
							fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
							sql.clearExceptionLog();
						}
						
						return true;
					}
					
					sendStatsMessage(sender, pStats);
				}
				else if(args.length == 0) {
					// only players can omit the first argument
					sender.sendMessage(ChatColor.RED + "You must specify the player to show the stats for!");
				}
				else {
					return false;
				}
				
				return true;
			}
			
			Player player = (Player) sender;
			
			if(args.length == 1) {
				// showing stats for someone else
				if(player.hasPermission("fmc.stats.others")) {
					PlayerStats pStats = sql.getPlayerStats(args[0]);
					if(pStats == null) {
						player.sendMessage(ChatColor.RED + "Could not get " + args[0] + "\'s statistics!");
						
						if(sql.getExceptionLog() != null) {
							fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
							sql.clearExceptionLog();
						}
						
						return true;
					}
					
					sendStatsMessage(player, pStats);
				}
				else {
					player.sendMessage(ChatColor.RED + "You do not have permission to see others statistics!");
				}
			}
			else if(args.length == 0) {
				// showing stats for the caller
				PlayerStats pStats = sql.getPlayerStats(player);
				if(pStats == null) {
					player.sendMessage(ChatColor.RED + "Could not get " + player.getName() + "\'s statistics!");
					
					if(sql.getExceptionLog() != null) {
						fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
						sql.clearExceptionLog();
					}
					
					return true;
				}
				
				sendStatsMessage(player, pStats);
			}
			else {
				return false;
			}
		}
		else if(cmd.getName().toLowerCase().equals("topstats")) {
			if(args.length != 0) {
				return false;
			}
			
			SQLTopStats sqlStats = sql.getTopStats();
			if(sqlStats == null) {
				sender.sendMessage(ChatColor.RED + "Could not get top statistics!");
				
				if(sql.getExceptionLog() != null) {
					fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.getExceptionLog());
					sql.clearExceptionLog();
				}
				
				return true;
			}

			OfflinePlayer[] players = Bukkit.getOfflinePlayers();
			
			// since it can take several seconds, better to not stall the entire server
			Bukkit.getScheduler().runTaskAsynchronously(fmc, new Runnable()
			{
				@Override
				public void run() {
					sendTopStatsMessage(sender, sqlStats, SrvTopStats.getServerTopStats(players));
				}
						
			});
		}
		
		return true;
	}
	
	private void sendStatsMessage(CommandSender to, PlayerStats pStats)
	{
		String firstJoined = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(pStats.getFirstJoined());
		String lastJoined = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(pStats.getLastJoined());

		to.sendMessage(new String[] {
			ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Stats" + ChatColor.DARK_GREEN + "] " + ChatColor.YELLOW + pStats.getName() + "\'s statistics:",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " UUID" + 				ChatColor.DARK_GRAY + ": " + ChatColor.RESET + pStats.getUUID(),
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " First joined" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + firstJoined,
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Last joined" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + lastJoined,
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Times joined" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + pStats.getTimesJoined(),
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Max level reached" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + pStats.getMaxLevelReached()
		});
	}
	
	private void sendTopStatsMessage(CommandSender to, SQLTopStats sqlStats, SrvTopStats srvStats)
	{
		String firstJoined = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(sqlStats.getFirstJoined());
		String lastJoined = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(sqlStats.getLastJoined());

		String playTime = SrvTopStats.getFormattedTicks(srvStats.getPlayTime());
		String sinceDeath = SrvTopStats.getFormattedTicks(srvStats.getSinceDeath());

		to.sendMessage(new String[] {
			ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Stats" + ChatColor.DARK_GREEN + "] " + ChatColor.YELLOW + "Top statistics:",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " First joined" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + firstJoined + 					ChatColor.GRAY + " (" + ChatColor.YELLOW + sqlStats.getWhoFirstJoined() +		ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Last joined" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + lastJoined + 					ChatColor.GRAY + " (" + ChatColor.YELLOW + sqlStats.getWhoLastJoined() + 		ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Times joined" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + sqlStats.getTimesJoined() + 		ChatColor.GRAY + " (" + ChatColor.YELLOW + sqlStats.getWhoTimesJoined() + 		ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Most time played" + 	ChatColor.DARK_GRAY + ": " + ChatColor.RESET + playTime + 						ChatColor.GRAY + " (" + ChatColor.YELLOW + srvStats.getWhoPlayTime() + 			ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Time since death" + 	ChatColor.DARK_GRAY + ": " + ChatColor.RESET + sinceDeath + 					ChatColor.GRAY + " (" + ChatColor.YELLOW + srvStats.getWhoSinceDeath() + 		ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Deaths" + 			ChatColor.DARK_GRAY + ": " + ChatColor.RESET + srvStats.getDeaths() +			ChatColor.GRAY + " (" + ChatColor.YELLOW + srvStats.getWhoDeaths() + 			ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Mob kills" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + srvStats.getMobKills() + 		ChatColor.GRAY + " (" + ChatColor.YELLOW + srvStats.getWhoMobKills() + 			ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Player kills" + 		ChatColor.DARK_GRAY + ": " + ChatColor.RESET + srvStats.getPlayerKills() + 		ChatColor.GRAY + " (" + ChatColor.YELLOW + srvStats.getWhoPlayerKills() + 		ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Max level reached" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + sqlStats.getMaxLevelReached() + 	ChatColor.GRAY + " (" + ChatColor.YELLOW + sqlStats.getWhoMaxLevelReached() + 	ChatColor.GRAY + ")",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Cake slices eaten" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + srvStats.getCakeSlices() + 		ChatColor.GRAY + " (" + ChatColor.YELLOW + srvStats.getWhoCakeSlices() + 		ChatColor.GRAY + ")"
		});
	}
}
