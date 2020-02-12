package flour.fmc.colorme;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.IModule;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * ColorMe module class
 * <p>
 * Allows players to change their names color using either
 * Bukkit API calls or vanilla teams feature.
 * </p>
 * 
 * @author Flourick
 */
public class ColorMe implements IModule, CommandExecutor
{
	private final boolean useTeams;
	
	private final CConfig colorMeConfig;
	
	public ColorMe(FMC fmc)
	{
		this.colorMeConfig = new CConfig(fmc, "colorme.yml");
		
		// Creates default config if not present
		colorMeConfig.saveDefaultConfig();
		
		useTeams = colorMeConfig.getConfig().getBoolean("use-vanilla-teams-for-coloring");
		
		fmc.getCommand("colorme").setTabCompleter(new ColorMeTabCompleter());
		fmc.getCommand("colorme").setExecutor(this);
		
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void playerJoin(PlayerJoinEvent event)
			{
		 		Player player = event.getPlayer();
				
				// Colors the player when he joins
				String color = colorMeConfig.getConfig().getString("player-colors." + player.getName());
				if(color != null) {
					colorPlayer(player, color);
				}
			}
		}, fmc);
	}
	
	@Override
	public void onDisable()
	{
		colorMeConfig.saveConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "Players only command.");
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
					colorMeConfig.getConfig().set("player-colors." + player.getName(), args[0]);
				}
				else {
					player.sendMessage("§cInvalid argument " + "\'" + args[0] + "\'" + ".");
				}
			}
		}

		return true;
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
