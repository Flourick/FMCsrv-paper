package me.flourick.fmc;

import me.flourick.fmc.administration.Administration;
import me.flourick.fmc.afk.AFK;
import me.flourick.fmc.chat.Chat;
import me.flourick.fmc.discord.Discord;
import me.flourick.fmc.dynfmc.DynFMC;
import me.flourick.fmc.fun.Fun;
import me.flourick.fmc.loot.Loot;
import me.flourick.fmc.oneplayersleep.OnePlayerSleep;
import me.flourick.fmc.protection.Protection;
import me.flourick.fmc.stats.Stats;
import me.flourick.fmc.utils.IModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
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

	public final HashMap<String, IModule> runningModules = new HashMap<>();
	
	@Override
	public void onEnable()
	{
		rootLogger = (Logger) LogManager.getRootLogger();
		
		// creates config.yml if not already present
		saveDefaultConfig();
		
		ArrayList<IModule> modules = new ArrayList<>();

		// ----- adding enabled modules to array (ONLY HERE AND IN checkConfig() YOU ADD A NEW MODULE) -----
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
		if(getConfig().getBoolean("enable-chat")) {
			modules.add(new Chat(this));
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
		if(getConfig().getBoolean("enable-administration")) {
			modules.add(new Administration(this));
		}
		if(getConfig().getBoolean("enable-discord")) {
			modules.add(new Discord(this));
		}

		checkConfig();
		// ----- -----
		
		for(IModule module : modules) {
			if(module.onEnable()) {
				runningModules.put(module.getName(), module);
			}
		}

		if(runningModules.isEmpty()) {
			getLogger().log(Level.INFO, "No FMC modules enabled! Disabling...");
		}
		else {
			getLogger().log(Level.INFO, "FMC has been successfully enabled!");
			getLogger().log(Level.INFO, "Loaded modules: {0}", getPrintableModules());
		}
	}

	@Override
	public void onDisable()
	{		
		// call onDisable() of all running modules
		for(Entry<String, IModule> moduleEntry : runningModules.entrySet()) {
			IModule module = moduleEntry.getValue();

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

	/**
	* Gets whether a given module is running or not
	* 
	* @param  name	Name of the module to check
	* 
	* @return true if module is running, false otherwise
	*/
	public boolean isModuleRunning(String moduleName)
	{
		for(Entry<String, IModule> moduleEntry : runningModules.entrySet()) {
			IModule module = moduleEntry.getValue();

			if(module.isEnabled()) {
				if(module.getName().equals(moduleName)) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	* Adds a Filter to root logger
	* 
	* @param  filter	Filter to add
	*/
	public void addLogFilter(Filter filter)
	{
		rootLogger.getContext().addFilter(filter);
	}
	
	/**
	* Removes a Filter to root logger
	* 
	* @param  filter	Filter to remove
	*/
	public void removeLogFilter(Filter filter)
	{
		rootLogger.getContext().removeFilter(filter);
	}

	private void checkConfig()
	{
		// this makes sure that even older configs will get properly updated
		// if adding a new module make sure to add appropriate lines here too!

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
		if(!getConfig().isSet("enable-chat")) {
			getConfig().set("enable-chat", true);
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
		if(!getConfig().isSet("enable-administration")) {
			getConfig().set("enable-administration", true);
		}
		if(!getConfig().isSet("enable-discord")) {
			getConfig().set("enable-discord", true);
		}

		saveConfig();
	}
	
	private String getPrintableModules()
	{
		String modules = "[";
		
		for(String module : runningModules.keySet()) {
			modules += module + ", ";
		}

		modules = modules.substring(0, modules.length() - 2);

		modules += "]";
		
		return modules;
	}
}
