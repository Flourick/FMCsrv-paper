package me.flourick.fmc;

import io.papermc.lib.PaperLib;

import me.flourick.fmc.afk.AFK;
import me.flourick.fmc.chatter.Chatter;
import me.flourick.fmc.colorme.ColorMe;
import me.flourick.fmc.dynfmc.DynFMC;
import me.flourick.fmc.fun.Fun;
import me.flourick.fmc.loot.Loot;
import me.flourick.fmc.oneplayersleep.OnePlayerSleep;
import me.flourick.fmc.protection.Protection;
import me.flourick.fmc.stats.Stats;
import me.flourick.fmc.utils.IModule;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
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
	// can be used to make modules behave differently when debugging
	public static final boolean DEV_MODE = false;
	// CraftBukkit version string
	public static final String CRAFT_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(46) + 1);

	private Logger rootLogger;

	private final ArrayList<IModule> runningModules = new ArrayList<>();
	
	@Override
	public void onEnable()
	{
		PaperLib.suggestPaper(this);
		
		rootLogger = (Logger) LogManager.getRootLogger();
		
		// creates config.yml if not already present
		saveDefaultConfig();
		
		ArrayList<IModule> modules = new ArrayList<>();

		// ----- adding enabled modules to array (ONLY HERE YOU ADD A NEW MODULE) -----
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
		if(getConfig().getBoolean("enable-loot")) {
			modules.add(new Loot(this));
		}
		if(getConfig().getBoolean("enable-fun")) {
			modules.add(new Fun(this));
		}
		if(getConfig().getBoolean("enable-protection")) {
			modules.add(new Protection(this));
		}

		checkConfig();
		// ----- -----
		
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
		// call onDisable() of all running modules
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

	private void checkConfig()
	{
		// this makes sure that even older configs will get properly updated
		// if adding a new module make sure to add appropriate lines here too!

		if(!getConfig().isSet("enable-colorme")) {
			getConfig().set("enable-colorme", true);
		}
		if(!getConfig().isSet("enable-oneplayersleep")) {
			getConfig().set("enable-oneplayersleep", true);
		}
		if(!getConfig().isSet("enable-dynfmc")) {
			getConfig().set("enable-dynfmc", true);
		}
		if(!getConfig().isSet("enable-afk")) {
			getConfig().set("enable-afk", true);
		}
		if(!getConfig().isSet("enable-stats")) {
			getConfig().set("enable-stats", true);
		}
		if(!getConfig().isSet("enable-chatter")) {
			getConfig().set("enable-chatter", true);
		}
		if(!getConfig().isSet("enable-loot")) {
			getConfig().set("enable-loot", true);
		}
		if(!getConfig().isSet("enable-fun")) {
			getConfig().set("enable-fun", true);
		}
		if(!getConfig().isSet("enable-protection")) {
			getConfig().set("enable-protection", true);
		}

		saveConfig();
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
