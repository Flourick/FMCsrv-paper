package flour.fmc.dynfmc;

import flour.fmc.FMC;
import flour.fmc.utils.EmptyTabCompleter;
import flour.fmc.utils.IModule;
import flour.fmc.utils.Log4jFilter;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * DynFMC module class
 * 
 * @author Flourick
 */
public class DynFMC implements IModule, CommandExecutor
{	
	private final FMC fmc;
	private boolean isEnabled = false;
	
	public DynFMC(FMC fmc)
	{
		this.fmc = fmc;
	}

	@Override
	public boolean onEnable()
	{
		if(fmc.getServer().getPluginManager().getPlugin("dynmap") == null || !fmc.getServer().getPluginManager().getPlugin("dynmap").isEnabled()) {
			fmc.getLogger().log(Level.SEVERE, "[DynFMC] DynMap is REQUIRED for DynFMC module to work!");
			
			return false;
		}
		
		fmc.getCommand("setbase").setExecutor(this);
		fmc.getCommand("setbase").setTabCompleter(new EmptyTabCompleter());
		fmc.getCommand("removebase").setExecutor(this);
		fmc.getCommand("removebase").setTabCompleter(new EmptyTabCompleter());
		
		// adds the bases set if not already created + filters out the error message
		Log4jFilter basesFilter = new Log4jFilter(new String[] {"Error: set already exists - id:bases", "Added set id:\'bases\' (bases)"}, true);
		fmc.addLogFilter(basesFilter);
		fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "dmarker addset bases");
		fmc.removeLogFilter(basesFilter);
		
		isEnabled = true;
		return true;
	}

	@Override
	public void onDisable()
	{
		isEnabled = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		if(cmd.getName().toLowerCase().equals("setbase")) {
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}
			
			Player player = (Player) sender;
			int x = player.getLocation().getBlockX();
			int y = player.getLocation().getBlockY();
			int z = player.getLocation().getBlockZ();
			
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "dmarker delete " + player.getName() + " set:bases");
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "dmarker add " + player.getName() + " icon:house set:bases x:" + x + " y:" + y + " z:" + z + " world:" + player.getWorld().getName());
			
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2[&aDynFMC&2] &ePlaced base marker on DynMap!"));
		}
		else if(cmd.getName().toLowerCase().equals("removebase")) {
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}
			
			Player player = (Player) sender;
			
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "dmarker delete " + player.getName() + " set:bases");
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2[&aDynFMC&2] &eRemoved base marker from DynMap!"));
		}
		
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	@Override
	public String getName()
	{
		return "DynFMC";
	}
}
