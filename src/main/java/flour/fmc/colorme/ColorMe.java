package flour.fmc.colorme;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.IModule;
import flour.fmc.utils.Log4jFilter;

import io.papermc.lib.PaperLib;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
	private boolean isDisabled = true;
	
	private final boolean useTeams;
	private final CConfig colorMeConfig;
	private final FMC fmc;
	
	public ColorMe(FMC fmc)
	{
		this.fmc = fmc;
		
		this.colorMeConfig = new CConfig(fmc, "colorme.yml");
		
		// Creates default config if not present
		colorMeConfig.saveDefaultConfig();
		
		useTeams = colorMeConfig.getConfig().getBoolean("use-vanilla-teams-for-coloring");
	}
	
	@Override
	public boolean onEnable()
	{
		// first let's check the use-vanilla-world-scoreboard-name-coloring variable
		if(PaperLib.isPaper()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(new File("paper.yml"));
			
			boolean use_vanilla_world_scoreboard_name_coloring = config.getBoolean("world-settings.default.use-vanilla-world-scoreboard-name-coloring");
			
			if(useTeams && !use_vanilla_world_scoreboard_name_coloring) {
				fmc.getLogger().log(Level.SEVERE, "use-vanilla-world-scoreboard-name-coloring has to be set to true in \'paper.yml\' for ColorMe to work in teams mode!!!");
				onDisable();
				return false;
			}
			else if(!useTeams && use_vanilla_world_scoreboard_name_coloring) {
				fmc.getLogger().log(Level.SEVERE, "use-vanilla-world-scoreboard-name-coloring has to be set to false in \'paper.yml\' for ColorMe to work without teams!!!");
				onDisable();
				return false;
			}
		}
		
		fmc.getCommand("colorme").setTabCompleter(new ColorMeTabCompleter());
		fmc.getCommand("colorme").setExecutor(this);
		
		if(useTeams) {
			BukkitRunnable runnable = new BukkitRunnable() {
				@Override
				public void run()
				{
					// adds a filter for team messages a.k.a less console spam
					Log4jFilter teamLogFilter = new Log4jFilter(new String[] {" from any team", " to team ", "Created team ", "A team already exists", "Updated the color for team ", "That team already has that color"});
					fmc.addLogFilter(teamLogFilter);
					
					// adds teams, nothing will happen if teams are already present
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_dark_red");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_dark_red color dark_red");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_red");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_red color red");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_gold");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_gold color gold");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_yellow");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_yellow color yellow");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_dark_green");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_dark_green color dark_green");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_green");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_green color green");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_aqua");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_aqua color aqua");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_dark_aqua");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_dark_aqua color dark_aqua");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_dark_blue");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_dark_blue color dark_blue");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_blue");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_blue color blue");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_light_purple");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_light_purple color light_purple");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_dark_purple");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_dark_purple color dark_purple");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_dark_gray");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_dark_gray color dark_gray");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team add cme_gray");
					fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team modify cme_gray color gray");
					
					// checks if player is already in the team associated with the color in the config file, if not adds him. Also checks if colors are valid
					Map<String, Object> playerColors = colorMeConfig.getConfig().getConfigurationSection("player-colors").getValues(false);
					
					playerColors.forEach((String str, Object obj) -> {
						
						String playerName = str;
						String color = obj.toString();
						Team team = fmc.getServer().getScoreboardManager().getMainScoreboard().getTeam("cme_" + color);
						
						if(team != null) {
							if(!team.hasEntry(playerName)) {
								colorPlayerTeams(playerName, color);
							}
						}
						else {
							fmc.getLogger().log(Level.WARNING, "Invalid entry in colorme.yml: ''{0}: {1}''", new Object[] {playerName, color});
							colorMeConfig.getConfig().set("player-colors." + playerName, null);
						}
					});
					
					// enable logging for teams again
					fmc.removeLogFilter(teamLogFilter);
				}
			};
			runnable.runTaskLater(fmc, 1L);
		}
		else {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void playerJoin(PlayerJoinEvent event)
				{
					Player player = event.getPlayer();

					// Colors the player when he joins
					String color = colorMeConfig.getConfig().getString("player-colors." + player.getName());
					if(color != null) {
						if(!colorPlayer(player, color)) {
							fmc.getLogger().log(Level.WARNING, "Invalid entry in colorme.yml: ''{0}: {1}''", new Object[] {player.getName(), color});
							colorMeConfig.getConfig().set("player-colors." + player.getName(), null);
						}
					}
				}
			}, fmc);
		}
		
		isDisabled = false;
		return true;
	}
	
	@Override
	public void onDisable()
	{
		if(useTeams) {
			removeTeams();
		}
		colorMeConfig.saveConfig();
		
		isDisabled = true;
		fmc.getLogger().log(Level.INFO, "Disabled ColorMe module.");
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
	
	public boolean getDisabled()
	{
		return isDisabled;
	}
	
	private boolean colorPlayer(Player player, String color)
	{
		if(useTeams) {
			return colorPlayerTeams(player.getName(), color);
		}
		else {
			return colorPlayerBukkit(player, color);
		}
	}
	
	private boolean colorPlayerTeams(String playerName, String color)
	{
		switch(color) {
			case "dark_red":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_dark_red " + playerName);
				break;
			case "red":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_red " + playerName);
				break;
			case "gold":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_gold " + playerName);
				break;
			case "yellow":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_yellow " + playerName);
				break;
			case "dark_green":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_dark_green " + playerName);
				break;
			case "green":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_green " + playerName);
				break;
			case "aqua":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_aqua " + playerName);
				break;
			case "dark_aqua":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_dark_aqua " + playerName);
				break;
			case "dark_blue":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_dark_blue " + playerName);
				break;
			case "blue":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_blue " + playerName);
				break;
			case "light_purple":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_light_purple " + playerName);
				break;
			case "dark_purple":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_dark_purple " + playerName);
				break;
			case "dark_gray":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_dark_gray " + playerName);
				break;
			case "gray":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team join cme_gray " + playerName);
				break;
			case "white":
				fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team leave " + playerName);
				break;
			default:
				return false;
		}
		
		return true;
	}
	
	private boolean colorPlayerBukkit(Player player, String color)
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
	
	private void removeTeams()
	{
		Scoreboard mainScoreboard = fmc.getServer().getScoreboardManager().getMainScoreboard();
		
		if(mainScoreboard.getTeam("cme_dark_red") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_dark_red");
		}
		if(mainScoreboard.getTeam("cme_red") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_red");
		}
		if(mainScoreboard.getTeam("cme_gold") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_gold");
		}
		if(mainScoreboard.getTeam("cme_yellow") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_yellow");
		}
		if(mainScoreboard.getTeam("cme_dark_green") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_dark_green");
		}
		if(mainScoreboard.getTeam("cme_green") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_green");
		}
		if(mainScoreboard.getTeam("cme_aqua") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_aqua");
		}
		if(mainScoreboard.getTeam("cme_dark_aqua") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_dark_aqua");
		}
		if(mainScoreboard.getTeam("cme_dark_blue") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_dark_blue");
		}
		if(mainScoreboard.getTeam("cme_blue") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_blue");
		}
		if(mainScoreboard.getTeam("cme_light_purple") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_light_purple");
		}
		if(mainScoreboard.getTeam("cme_dark_purple") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_dark_purple");
		}
		if(mainScoreboard.getTeam("cme_dark_gray") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_dark_gray");
		}
		if(mainScoreboard.getTeam("cme_gray") != null) {
			fmc.getServer().dispatchCommand(fmc.getServer().getConsoleSender(), "team remove cme_gray");
		}
	}
}
