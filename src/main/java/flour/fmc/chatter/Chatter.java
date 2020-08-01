package flour.fmc.chatter;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.EmptyTabCompleter;
import flour.fmc.utils.IModule;

import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Module for various chat features
 * 
 * @author Flourick
 */
public class Chatter implements IModule, CommandExecutor
{
	private boolean isEnabled = false;
	private final FMC fmc;
	
	private final CConfig chatterConfig;
	
	private boolean useCustomMessages = false;
	private List<String> joinMessages;
	private List<String> quitMessages;
	
	public Chatter(FMC fmc)
	{
		this.fmc = fmc;
		this.chatterConfig = new CConfig(fmc, "chatter.yml");
		
		// Creates default config if not present
		chatterConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("announce").setExecutor(this);
		fmc.getCommand("announce").setTabCompleter(new EmptyTabCompleter());
		
		if(chatterConfig.getConfig().getBoolean("allow-chat-color-codes")) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority=EventPriority.HIGHEST)
				public void OnPlayerAsyncChat(AsyncPlayerChatEvent e)
				{
					e.setMessage(ChatColor.translateAlternateColorCodes('&',e.getMessage()));
				}
			}, fmc);
		}
		
		if(useCustomMessages = chatterConfig.getConfig().getBoolean("custom-messages.use-custom-join-and-quit-messages")) {
			// gets custom messages from YAML, falls back to defaults if not found
			joinMessages = chatterConfig.getConfig().getStringList("custom-messages.join-messages");
			quitMessages = chatterConfig.getConfig().getStringList("custom-messages.quit-messages");
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
		return "Chatter";
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
		
		return true;
	}
}
