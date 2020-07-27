package flour.fmc.afk;

import flour.fmc.FMC;
import flour.fmc.utils.CConfig;
import flour.fmc.utils.EmptyTabCompleter;
import flour.fmc.utils.IModule;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Simple AFK module
 * 
 * @author Flourick
 */
public class AFK implements IModule, CommandExecutor
{
	private final FMC fmc;
	private boolean isEnabled = false;
	
	private final CConfig afkConfig;
	private boolean announceAFKStatus;
	private String playerIsAFKMessage;
	private String playerNoLongerAFKMessage;
	private int afkTimeout;
	private int afkPeriod;
	
	Map<Player, Integer> playerTimes;
	
	public AFK(FMC fmc)
	{
		this.fmc = fmc;
		this.playerTimes = new HashMap<>();
		this.afkConfig = new CConfig(fmc, "afk.yml");
		
		// creates default config if not present
		afkConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("afk").setTabCompleter(new EmptyTabCompleter());
		fmc.getCommand("afk").setExecutor(this);
		
		// player joined, let's watch him closely
		fmc.getServer().getPluginManager().registerEvents(new Listener()
		{
			@EventHandler(priority=EventPriority.NORMAL)
			public void onPlayerJoin(PlayerJoinEvent e)
			{		
				playerTimes.put(e.getPlayer(), 0);
			}
		}, fmc);
		
		// player disconnected, no longer checking for AFK
		fmc.getServer().getPluginManager().registerEvents(new Listener()
		{
			@EventHandler(priority=EventPriority.NORMAL)
			public void OnPlayerQuit(PlayerQuitEvent e)
			{
				playerTimes.remove(e.getPlayer());
			}
		}, fmc);
		
		// in case of reload
		for(Player pl : fmc.getServer().getOnlinePlayers()) {
			playerTimes.put(pl, 0);
			pl.setPlayerListName(pl.getDisplayName());
		}
		
		// registers events that cause player to no longer be AFK
		registerAFKTriggers();
		
		// get values from configuration yaml file
		announceAFKStatus = afkConfig.getConfig().getBoolean("announce-afk-in-chat");
		if(announceAFKStatus) {
			playerIsAFKMessage = afkConfig.getConfig().getString("announce-afk-messages.is-afk-message");
			playerNoLongerAFKMessage = afkConfig.getConfig().getString("announce-afk-messages.no-longer-afk-message");
		}
		
		afkPeriod = afkConfig.getConfig().getInt("afk-check-period") * 20;
		if(afkPeriod < 20 || afkPeriod > 200) {
			afkPeriod = 20;
			fmc.getLogger().log(Level.WARNING, "[AFK] afk-check-period is out of bounds! Falling back to defaults.");
		}
		
		afkTimeout = afkConfig.getConfig().getInt("afk-timeout");
		if(afkTimeout == -1) {
			// not checking periodically, skipping...
			isEnabled = true;
			return true;
		}
		else if(afkTimeout < afkPeriod || afkTimeout < 30 || afkTimeout > 86400) {
			afkTimeout = 300;
			fmc.getLogger().log(Level.WARNING, "[AFK] afk-timeout is out of bounds! Falling back to defaults.");
		}
		
		// checks for AFK players every second
		fmc.getServer().getScheduler().scheduleSyncRepeatingTask(fmc, new Runnable()
		{
			@Override
			public void run()
			{
				for(Map.Entry<Player, Integer> entry : playerTimes.entrySet()) {
					Player player = entry.getKey();
					int time = entry.getValue();
					
					if(time == -1) {
						// player already afk
					}
					else if(time >= afkTimeout) {
						// more than timeout, set to AFK
						setPlayerAFK(player);
					}
					else {
						// advance players AFK counter
						playerTimes.put(player, time + 1);
					}
				}	
			}
			
		}, 20, afkPeriod);
		
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
		return "AFK";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		if(cmd.getName().toLowerCase().equals("afk")) {
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}
			
			Player player = (Player) sender;
			
			if(playerTimes.get(player) == -1) {
				// is AFK
				setPlayerNotAFK(player);
			}
			else {
				// not AFK
				setPlayerAFK(player);
				if(!announceAFKStatus) {
					player.sendMessage(ChatColor.YELLOW + "You are now marked as AFK!");
				}
			}
		}
		
		return true;
	}
	
	private void setPlayerAFK(Player player)
	{
		player.setPlayerListName(player.getPlayerListName() + ChatColor.GRAY + " [" + ChatColor.RESET +"AFK" + ChatColor.GRAY + "]" + ChatColor.RESET);
		playerTimes.put(player, -1);
		
		// announce message to chat or atleast log it
		if(announceAFKStatus) {
			fmc.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', playerIsAFKMessage.replace("{PLAYER}", player.getDisplayName())));
		}
		else {
			fmc.getLogger().log(Level.INFO, "[AFK] {0} is now AFK!", player.getName());
		}
	}
	
	private void setPlayerNotAFK(Player player)
	{
		player.setPlayerListName(player.getDisplayName());
		playerTimes.put(player, 0);
		
		// announce message to chat or atleast log it
		if(announceAFKStatus) {
			fmc.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', playerNoLongerAFKMessage.replace("{PLAYER}", player.getDisplayName())));
		}
		else {
			fmc.getLogger().log(Level.INFO, "[AFK] {0} is no longer AFK!", player.getName());
		}
	}
	
	private void registerAFKTriggers()
	{
		// player moved a.k.a no longer AFK!
		fmc.getServer().getPluginManager().registerEvents(new Listener()
		{
			@EventHandler
			public void onPlayerMove(final PlayerMoveEvent e)
			{
				// didn't move enough
				if(e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ() && e.getFrom().getBlockY() == e.getTo().getBlockY()) {
					return;
				}
				
				if(playerTimes.get(e.getPlayer()) == -1) {
					setPlayerNotAFK(e.getPlayer());
				}
				else {
					playerTimes.put(e.getPlayer(), 0);
				}
			}
		}, fmc);
		
		// player chatted so no longer AFK!
		fmc.getServer().getPluginManager().registerEvents(new Listener()
		{
			@EventHandler(priority=EventPriority.LOW)
			public void OnPlayerAsyncChat(AsyncPlayerChatEvent e)
			{
				if(playerTimes.get(e.getPlayer()) == -1) {
					setPlayerNotAFK(e.getPlayer());
				}
				else {
					playerTimes.put(e.getPlayer(), 0);
				}
			}
		}, fmc);
		
		// player issued a command so no longer AFK
		fmc.getServer().getPluginManager().registerEvents(new Listener()
		{
			@EventHandler(priority=EventPriority.LOW)
			public void OnPlayerIssuedCommand(PlayerCommandPreprocessEvent e)
			{
				if(e.getMessage().toLowerCase().startsWith("/afk")) {
					// skip check for afk command
					return;
				}
				
				if(playerTimes.get(e.getPlayer()) == -1) {
					setPlayerNotAFK(e.getPlayer());
				}
				else {
					playerTimes.put(e.getPlayer(), 0);
				}
			}
		}, fmc);
	}
}
