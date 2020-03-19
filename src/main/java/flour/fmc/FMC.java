package flour.fmc;

import flour.fmc.afk.AFK;
import flour.fmc.chatter.Chatter;
import io.papermc.lib.PaperLib;

import flour.fmc.colorme.ColorMe;
import flour.fmc.dynfmc.DynFMC;
import flour.fmc.oneplayersleep.OnePlayerSleep;
import flour.fmc.stats.Stats;
import flour.fmc.utils.EmptyTabCompleter;
import flour.fmc.utils.IModule;

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

	private final ArrayList<IModule> runningModules = new ArrayList<>();
	
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
		
		ArrayList<IModule> modules = new ArrayList<>();
		
		// --- adding enabled modules to array (ONLY HERE YOU ADD A NEW MODULE) ---
		if(getConfig().getBoolean("enable-colorme")) {
			modules.add(new ColorMe(this));
		}
		if(getConfig().getBoolean("enable-oneplayersleep")) {
			modules.add(new OnePlayerSleep(this));
		}
		if(getConfig().getBoolean("enable-dynfmc")) {
			modules.add(new DynFMC(this));
		}
		if(getConfig().getBoolean("enable-afk")) {
			modules.add(new AFK(this));
		}
		if(getConfig().getBoolean("enable-stats")) {
			modules.add(new Stats(this));
		}
		if(getConfig().getBoolean("enable-chatter")) {
			modules.add(new Chatter(this));
		}
		// --- ---
		
		for(IModule module : modules) {
			if(module.onEnable()) {
				runningModules.add(module);
			}
		}
		
		getLogger().log(Level.INFO, "FMC has been successfully enabled!");
		getLogger().log(Level.INFO, "Loaded modules: {0}", getPrintableModules());
	}

	@Override
	public void onDisable()
	{		
		// call onDisable() of modules
		for(IModule module : runningModules) {
			if(module.isEnabled()) {
				module.onDisable();
				getLogger().log(Level.INFO, "Disabled {0} module.", module.getName());
			}
		}
		
		getLogger().log(Level.INFO, "FMC has been disabled.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
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
	
	private String getPrintableModules()
	{
		String modules = "[";
		
		int sz = runningModules.size();
		
		for(int i = 0; i < sz; i++) {
			modules += runningModules.get(i).getName();
			
			if(i < sz - 1) {
				modules += ", ";
			}
		}
		modules += "]";
		
		return modules;
	}
}
