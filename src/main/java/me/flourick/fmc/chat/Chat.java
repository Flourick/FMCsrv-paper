package me.flourick.fmc.chat;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.papermc.lib.PaperLib;

import me.flourick.fmc.FMC;
import me.flourick.fmc.utils.CConfig;
import me.flourick.fmc.utils.EmptyTabCompleter;
import me.flourick.fmc.utils.IModule;

/**
 * Module for various chat features
 * 
 * @author Flourick
 */
public class Chat implements IModule, CommandExecutor
{
	private boolean isEnabled = false;
	private final FMC fmc;
	
	private final CConfig chatConfig;
	
	private boolean useCustomMessages = false;
	private List<String> joinMessages;
	private List<String> quitMessages;
	
	public Chat(FMC fmc)
	{
		this.fmc = fmc;
		this.chatConfig = new CConfig(fmc, "chat.yml");
		
		// Creates default config if not present
		chatConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("announce").setExecutor(this);
		fmc.getCommand("announce").setTabCompleter(new EmptyTabCompleter());

		fmc.getCommand("colorme").setExecutor(this);
		fmc.getCommand("colorme").setTabCompleter(new ColorMeTabCompleter());
		
		if(chatConfig.getConfig().getBoolean("allow-chat-color-codes")) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority=EventPriority.HIGHEST)
				public void OnPlayerAsyncChat(AsyncPlayerChatEvent e)
				{
					e.setMessage(ChatColor.translateAlternateColorCodes('&',e.getMessage()));
				}
			}, fmc);
		}
		
		if(useCustomMessages = chatConfig.getConfig().getBoolean("custom-messages.use-custom-join-and-quit-messages")) {
			// gets custom messages from YAML, falls back to defaults if not found
			joinMessages = chatConfig.getConfig().getStringList("custom-messages.join-messages");
			quitMessages = chatConfig.getConfig().getStringList("custom-messages.quit-messages");
		}
		
		// Overrides default join/quit message broadcasted to others
		if(useCustomMessages) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority=EventPriority.HIGHEST)
				public void onPlayerJoin(PlayerJoinEvent event)
				{
					Player player = event.getPlayer();

					event.setJoinMessage(getRandomJoinMessage(player.getDisplayName()));
				}
			}, fmc);
			
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority=EventPriority.HIGHEST)
				public void onPlayerQuit(PlayerQuitEvent event)
				{
					Player player = event.getPlayer();

					event.setQuitMessage(getRandomQuitMessage(player.getDisplayName()));
				}
			}, fmc);
		}

		if(chatConfig.getConfig().getBoolean("enable-player-colors")) {
			if(PaperLib.isPaper()) {
				FileConfiguration config = YamlConfiguration.loadConfiguration(new File("paper.yml"));
				
				boolean use_vanilla_world_scoreboard_name_coloring = config.getBoolean("world-settings.default.use-vanilla-world-scoreboard-name-coloring");
				
				if(use_vanilla_world_scoreboard_name_coloring) {
					fmc.getLogger().log(Level.SEVERE, "[Chat] use-vanilla-world-scoreboard-name-coloring has to be set to false in \'paper.yml\' for player colors to work!");
					onDisable();
					return false;
				}
			}

			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority=EventPriority.LOWEST)
				public void onPlayerJoin(PlayerJoinEvent event)
				{
					Player player = event.getPlayer();
	
					// Colors the player when he joins
					String color = chatConfig.getConfig().getString("player-colors." + player.getName());
					if(color != null) {
						if(!colorPlayer(player, color)) {
							fmc.getLogger().log(Level.WARNING, "[Chat] Invalid entry in chat.yml: ''{0}: {1}''", new Object[] {player.getName(), color});
							chatConfig.getConfig().set("player-colors." + player.getName(), null);
							chatConfig.saveConfig();
						}
					}
					else {
						colorPlayer(player, "white");
						chatConfig.getConfig().set("player-colors." + player.getName(), "white");
						chatConfig.saveConfig();
					}
				}
			}, fmc);
		}
		
		return isEnabled = true;
	}

	@Override
	public void onDisable()
	{
		isEnabled = false;
	}
	
	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}

	@Override
	public String getName()
	{
		return "Chat";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args)
	{
		if(cmd.getName().toLowerCase().equals("announce")) {
			if(args.length < 1) {
				return false;
			}
			
			String message;
			message =  org.bukkit.ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
			fmc.getServer().broadcastMessage(message);
		}
		else if(cmd.getName().toLowerCase().equals("colorme")) {
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}

			if(args.length != 1) {
				return false;
			}

			Player player = (Player) sender;

			if(colorPlayer(player, args[0])) {
				chatConfig.getConfig().set("player-colors." + player.getName(), args[0]);
				chatConfig.saveConfig();
			}
			else {
				player.sendMessage(ChatColor.RED + "Invalid argument " + "\'" + args[0] + "\'" + ".");
			}
		}
		
		return true;
	}

	private String getRandomJoinMessage(String playerName)
	{
		Random rnd = new Random();
		int idx = rnd.nextInt(joinMessages.size());
		
		String message = ChatColor.translateAlternateColorCodes('&', joinMessages.get(idx).replace("{PLAYER}", playerName));
		
		return message;
	}
	
	private String getRandomQuitMessage(String playerName)
	{
		Random rnd = new Random();
		int idx = rnd.nextInt(quitMessages.size());
		
		String message = ChatColor.translateAlternateColorCodes('&', quitMessages.get(idx).replace("{PLAYER}", playerName));
		
		return message;
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
				player.setDisplayName(ChatColor.WHITE + player.getName() + ChatColor.RESET);
				player.setPlayerListName(ChatColor.WHITE + player.getName() + ChatColor.RESET);
				break;
			default:
				return false;
		}
		
		return true;
	}
}
