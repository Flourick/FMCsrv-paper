package flour.fmc;

import io.papermc.lib.PaperLib;

import flour.fmc.colorme.ColorMe;
import java.util.ArrayList;
import java.util.logging.Level;
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
	// modules
	private ColorMe colorMe;
	private boolean colorMeEnabled = true;
	
	@Override
	public void onEnable()
	{
		PaperLib.suggestPaper(this);
		
		// creates config.yml if not already present
		saveDefaultConfig();
		
		ArrayList<String> enabledModules = new ArrayList<>();
		
		// load modules
		if(colorMeEnabled = getConfig().getBoolean("enable-color-me")) {
			colorMe = new ColorMe(this);
			enabledModules.add("ColorMe");
		}
		
		getLogger().log(Level.INFO, "FMC has been successfully enabled");
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
		
		getLogger().log(Level.INFO, "FMC has been disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		// No FMC commands for now
		sender.sendMessage(ChatColor.RED + "Disabled command.");
		
		return true;
	}
}
