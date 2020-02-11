package flour.fmc;

import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatColor;

import flour.fmc.colorme.ColorMeTabCompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the FMC plugin
 * 
 * @author Flourick
 */
public class FMC extends JavaPlugin
{
	private static FMC instance;
	
	public static FMC getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable()
	{
		instance = this;
		PaperLib.suggestPaper(this);
		
		// Creates config.yml if not already present
		this.saveDefaultConfig();
		
		this.getCommand("colorme").setTabCompleter(new ColorMeTabCompleter());
		this.getCommand("cme").setTabCompleter(new ColorMeTabCompleter());
		
		 // Player joins
		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void playerJoin(PlayerJoinEvent event)
			{
		 		Player player = event.getPlayer();
				
				// Colors the player when he joins
				String color = FMC.getInstance().getConfig().getString("player-colors." + player.getName());
				if(color != null) {
					FMC.getInstance().colorPlayer(player, color);
				}
			}
		}, this);
		
		getLogger().info("FMC plugin has been enabled");
	}

	@Override
	public void onDisable()
	{
		saveConfig();
		getLogger().info("FMC plugin has been disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if(cmd.getName().equalsIgnoreCase("colorme")){
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
					player.sendMessage(
						"Available colors: "
						+ "§4dark_red§r, "
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
					);
				}
				else {
					if(colorPlayer(player, args[0])) {
						this.getConfig().set("player-colors." + player.getName(), args[0]);
					}
					else {
						player.sendMessage("§cInvalid argument " + "\'" + args[0] + "\'" + ".");
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean colorPlayer(Player player, String color)
	{
		switch(color) {
			case "dark_red":
				player.setDisplayName(ChatColor.DARK_RED + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.DARK_RED + player.getName() + ChatColor.RESET);
				break;
			case "red":
				player.setDisplayName(ChatColor.RED + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.RED + player.getName() + ChatColor.RESET);
				break;
			case "gold":
				player.setDisplayName(ChatColor.GOLD + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.GOLD + player.getName() + ChatColor.RESET);
				break;
			case "yellow":
				player.setDisplayName(ChatColor.YELLOW + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.YELLOW + player.getName() + ChatColor.RESET);
				break;
			case "dark_green":
				player.setDisplayName(ChatColor.DARK_GREEN + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.DARK_GREEN + player.getName() + ChatColor.RESET);
				break;
			case "green":
				player.setDisplayName(ChatColor.GREEN + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.GREEN + player.getName() + ChatColor.RESET);
				break;
			case "aqua":
				player.setDisplayName(ChatColor.AQUA + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.AQUA + player.getName() + ChatColor.RESET);
				break;
			case "dark_aqua":
				player.setDisplayName(ChatColor.DARK_AQUA + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.DARK_AQUA + player.getName() + ChatColor.RESET);
				break;
			case "dark_blue":
				player.setDisplayName(ChatColor.DARK_BLUE + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.DARK_BLUE + player.getName() + ChatColor.RESET);
				break;
			case "blue":
				player.setDisplayName(ChatColor.BLUE + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.BLUE + player.getName() + ChatColor.RESET);
				break;
			case "light_purple":
				player.setDisplayName(ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.RESET);
				break;
			case "dark_purple":
				player.setDisplayName(ChatColor.DARK_PURPLE + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.DARK_PURPLE + player.getName() + ChatColor.RESET);
				break;
			case "dark_gray":
				player.setDisplayName(ChatColor.DARK_GRAY + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.DARK_GRAY + player.getName() + ChatColor.RESET);
				break;
			case "gray":
				player.setDisplayName(ChatColor.GRAY + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RESET);
				break;
			case "white":
				player.setDisplayName(player.getName());
				player.setPlayerListName(player.getName());
				break;
			default:
				return false;
		}
		return true;
	}
}
