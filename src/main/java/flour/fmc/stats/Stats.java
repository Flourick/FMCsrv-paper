package flour.fmc.stats;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.IModule;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
	
	private final MySQLConnection sql;
	
	public Stats(FMC fmc)
	{
		this.fmc = fmc;
		this.statsConfig = new CConfig(fmc, "stats.yml");
		
		// Creates default config if not present
		statsConfig.saveDefaultConfig();
		
		this.sql = new MySQLConnection(statsConfig.getConfig().getString("connection-string"), statsConfig.getConfig().getString("username"), statsConfig.getConfig().getString("password"));
	}
	
	@Override
	public boolean onEnable()
	{
		fmc.getCommand("statistics").setExecutor(this);
		fmc.getCommand("statistics").setTabCompleter(new StatsTabCompleter());
		
		if(statsConfig.getConfig().getString("connection-string").equals("jdbc:mysql://hostname:port/databaseName")) {
			// default config file
			fmc.getLogger().log(Level.WARNING, "[Stats] Default connection-string in stats.yml! Please change it and restart/reload the server.");
			onDisable();
			return false;
		}
		
		boolean sqlCorrect = sql.initialize();
		if(!sqlCorrect) {
			fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.exceptionLog);
			onDisable();
			return false;
		}
		
		fmc.getLogger().log(Level.INFO, "[Stats] Successfully initialized MySQL database connection!");
		
		// listen for player join and update his join times
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event)
			{
				Player player = event.getPlayer();
				if(!sql.onPlayerJoin(player)) {
					fmc.getLogger().log(Level.SEVERE, "[Stats] {0}", sql.exceptionLog);
				}
			}
		}, fmc);
		
		isEnabled = true;
		return true;
	}

	@Override
	public void onDisable()
	{
		sql.close();
		isEnabled = false;
		fmc.getLogger().log(Level.INFO, "Disabled Stats module.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		if(cmd.getName().toLowerCase().equals("statistics")) {
			if(!(sender instanceof Player)){
				if(args.length == 1) {
					// showing stats for someone else
					PlayerStats pStats = sql.getPlayerStats(args[0]);
					if(pStats == null) {
						sender.sendMessage(ChatColor.RED + "Could not get " + args[0] + "\'s statistics!");
						
						if(sql.exceptionLog != null) {
							sender.sendMessage(ChatColor.RED + "REASON: " + sql.exceptionLog);
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
					return true;
				}
				
				sendStatsMessage(player, pStats);
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	private void sendStatsMessage(CommandSender to, PlayerStats pStats)
	{
		String firstJoined = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(pStats.getFirstJoined());
		String lastJoined = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy").format(pStats.getLastJoined());

		to.sendMessage(new String[] {
			ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "Stats" + ChatColor.DARK_GREEN + "] " + ChatColor.YELLOW + pStats.getName() + "\'s statistics" + ChatColor.GRAY + ":",
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " UUID" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + pStats.getUUID(),
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " First joined" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + firstJoined,
			ChatColor.DARK_GRAY + "-" + ChatColor.GRAY + " Last joined" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + lastJoined
		});
	}
	
	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}
}
