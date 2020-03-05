package flour.fmc.stats;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.IModule;
import java.util.logging.Level;
import jdk.jfr.internal.LogLevel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
	
	private MySQLConnection sql;
	
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
					sender.sendMessage("showing stats for " + args[0]);
				}
				else if(args.length == 0) {
					// showing stats for the caller
					sender.sendMessage(ChatColor.RED + "You must specify the player to show the stats for!");
				}
				else {
					return false;
				}
				
				return true;
			}
			
			Player player = (Player) sender;
			
			// can show stats for others
			if(player.hasPermission("fmc.stats.others")) {
				if(args.length == 1) {
					// showing stats for someone else
					player.sendMessage("showing stats for " + args[0]);
				}
				else if(args.length == 0) {
					// showing stats for the caller
					player.sendMessage("showing stats for yourself");
				}
				else {
					return false;
				}
			}
			else {
				if(args.length == 1) {
					player.sendMessage(ChatColor.RED + "You do not have permission to see others statistics!");
				}
				else if(args.length == 0) {
					// showing stats for the caller
					player.sendMessage("showing stats for yourself");
				}
				else {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}
}
