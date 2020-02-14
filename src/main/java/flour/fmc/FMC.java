package flour.fmc;

import io.papermc.lib.PaperLib;

import flour.fmc.colorme.ColorMe;
import flour.fmc.dyngen.DynGen;

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
	private ColorMe colorMe;
	private boolean colorMeEnabled = false;
	private DynGen dynGen;
	private boolean dynGenEnabled = false;
	
	@Override
	public void onEnable()
	{
		PaperLib.suggestPaper(this);
		if(PaperLib.isPaper()) {
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
		}
		if(dynGenEnabled = getConfig().getBoolean("enable-dyngen")) {
			dynGen = new DynGen(this);
			if(dynGen.onEnable()) {
				enabledModules.add("DynGen");
			}
		}
		
		getLogger().log(Level.INFO, "FMC has been successfully enabled!");
		getLogger().log(Level.INFO, "Loaded modules: {0}", enabledModules);
	}

	@Override
	public void onDisable()
	{
		// save main config from memory
		saveConfig();
		
		// call onDisable() of modules
		if(colorMeEnabled && !colorMe.getDisabled()) {
			colorMe.onDisable();
		}
		if(dynGenEnabled && !dynGen.getDisabled()) {
			dynGen.onDisable();
		}
		
		getLogger().log(Level.INFO, "FMC has been disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		// No base FMC commands for now
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
