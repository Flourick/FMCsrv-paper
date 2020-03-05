package flour.fmc;

import flour.fmc.afk.AFK;
import io.papermc.lib.PaperLib;

import flour.fmc.colorme.ColorMe;
import flour.fmc.dynfmc.DynFMC;
import flour.fmc.oneplayersleep.OnePlayerSleep;
import flour.fmc.stats.Stats;
import flour.fmc.utils.EmptyTabCompleter;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the FMC plugin
 * 
 * @author Flourick
 */
public class FMC extends JavaPlugin
{
	private Logger rootLogger;
	
	// modules
	private ColorMe			colorMe;
	private boolean			colorMeEnabled = false;
	private DynFMC			dynFMC;
	private boolean			dynFMCEnabled = false;
	private OnePlayerSleep	onePlayerSleep;
	private boolean			onePlayerSleepEnabled = false;
	private AFK				afk;
	private boolean			afkEnabled = false;
	private Stats			stats;
	private boolean			statsEnabled = false;
	
	@Override
	public void onEnable()
	{
		PaperLib.suggestPaper(this);
		if(!PaperLib.isPaper()) {
			getLogger().log(Level.WARNING, "This plugin might not work as expected or at all on anything other than Paper! I HIGHLY recommend using Paper.");
		}
		
		rootLogger = (Logger) LogManager.getRootLogger();
		
		// creates config.yml if not already present
		saveDefaultConfig();
		
		ArrayList<String> enabledModules = new ArrayList<>();
		
		// load modules
		if(colorMeEnabled = getConfig().getBoolean("enable-colorme")) {
			colorMe = new ColorMe(this);
			if(colorMe.onEnable()) {
				enabledModules.add("ColorMe");
			}
			else {
				// module disabled itself, maybe wrong config?
				colorMe = null;
				colorMeEnabled = false;
			}
		}
		if(onePlayerSleepEnabled = getConfig().getBoolean("enable-oneplayersleep")) {
			onePlayerSleep = new OnePlayerSleep(this);
			if(onePlayerSleep.onEnable()) {
				enabledModules.add("OnePlayerSleep");
			}
			else {
				// module disabled itself, maybe wrong config?
				onePlayerSleep = null;
				onePlayerSleepEnabled = false;
			}
		}
		if(dynFMCEnabled = getConfig().getBoolean("enable-dynfmc")) {
			dynFMC = new DynFMC(this);
			if(dynFMC.onEnable()) {
				enabledModules.add("DynFMC");
			}
			else {
				// module disabled itself, maybe wrong config?
				dynFMC = null;
				dynFMCEnabled = false;
			}
		}
		if(afkEnabled = getConfig().getBoolean("enable-afk")) {
			afk = new AFK(this);
			if(afk.onEnable()) {
				enabledModules.add("AFK");
			}
			else {
				// module disabled itself, maybe wrong config?
				afk = null;
				afkEnabled = false;
			}
		}
		if(statsEnabled = getConfig().getBoolean("enable-stats")) {
			stats = new Stats(this);
			if(stats.onEnable()) {
				enabledModules.add("Stats");
			}
			else {
				// module disabled itself, maybe wrong config?
				stats = null;
				statsEnabled = false;
			}
		}
		
		//FMC commands
		getCommand("announce").setTabCompleter(new EmptyTabCompleter());
		
		getLogger().log(Level.INFO, "FMC has been successfully enabled!");
		getLogger().log(Level.INFO, "Loaded modules: {0}", enabledModules);
	}

	@Override
	public void onDisable()
	{
		// save main config from memory
		saveConfig();
		
		// call onDisable() of modules
		if(colorMeEnabled) {
			colorMe.onDisable();
		}
		if(onePlayerSleepEnabled) {
			onePlayerSleep.onDisable();
		}
		if(dynFMCEnabled) {
			dynFMC.onDisable();
		}
		if(afkEnabled) {
			afk.onDisable();
		}
		if(statsEnabled) {
			stats.onDisable();
		}
		
		getLogger().log(Level.INFO, "FMC has been disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if(cmd.getName().toLowerCase().equals("announce")) {
			if(args.length < 1) {
				return false;
			}
			
			String message;
			message =  ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
			getServer().broadcastMessage(message);
			
			return true;
		}
		
		// Any other leftover commands from disabled modules
		sender.sendMessage(ChatColor.RED + "Disabled command.");
		
		return true;
	}
	
	public void addLogFilter(Filter filter)
	{
		rootLogger.getContext().addFilter(filter);
	}
	
	public void removeLogFilter(Filter filter)
	{
		rootLogger.getContext().removeFilter(filter);
	}
}
