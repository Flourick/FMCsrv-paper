package flour.fmc;

import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the FMC plugin
 * 
 * @author Flourick
 */
public class FMC extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		PaperLib.suggestPaper(this);
	}

	@Override
	public void onDisable()
	{
		getLogger().info("FMC plugin has been disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if(commandLabel.equalsIgnoreCase("colorme") || commandLabel.equalsIgnoreCase("cme")){
			if(!(sender instanceof Player)){
				sender.sendMessage("Players only command.");
				return true;
			}
			
			if(args.length != 1) {
				return false;
			}
			
			Player player = (Player) sender;
			
			if(player.hasPermission("fmc.colorme")) {
				if(args[0].equalsIgnoreCase("list")) {
					player.sendMessage(new String[] {
							"Available colors: ",
							  "§4dark_red§r, "
							+ "§cred§r, "
							+ "§6gold§r, "
							+ "§eyellow§r, "
							+ "§2dark_green§r, "
							+ "§agreen§r, "
							+ "§baqua§r, "
							+ "§3dark_aqua§r, "
							+ "§1dark_blue§r, "
							+ "§9blue§r, "
							+ "§dlight_purple§r, "
							+ "§5dark_purple§r, "
							+ "§8dark_gray§r, "
							+ "§7gray§r, "
							+ "§fwhite"
					});
				}
				else {
					switch(args[0]) {
						case "dark_red":
							
							break;
						case "red":
							
							break;
						case "gold":
							
							break;
						case "yellow":
							
							break;
						case "dark_green":
							
							break;
						case "green":
							
							break;
						case "aqua":
							
							break;
						case "dark_aqua":
							
							break;
						case "dark_blue":
							
							break;
						case "blue":
							
							break;
						case "light_purple":
							
							break;
						case "dark_purple":
							
							break;
						case "dark_gray":
							
							break;
						case "gray":
							
							break;
						case "white":
							
							break;
							
						default:
							player.sendMessage("§cInvalid arguments!");
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
}
