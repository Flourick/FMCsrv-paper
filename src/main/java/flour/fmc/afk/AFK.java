package flour.fmc.afk;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
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
public class AFK implements IModule
{
	private final FMC fmc;
	private boolean isEnabled = false;
	
	Map<Player, Integer> playerTimes;
	private int afkTimeout = 300;
	
	public AFK(FMC fmc)
	{
		this.fmc = fmc;
		this.playerTimes = new HashMap<>();
	}

	@Override
	public boolean onEnable()
	{
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerMove(final PlayerMoveEvent e)
			{
				// didn't move enough
				if(e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ() && e.getFrom().getBlockY() == e.getTo().getBlockY()) {
					return;
				}
				
				// AFK user moved
				if(playerTimes.get(e.getPlayer()) == -1) {
					setPlayerNotAFK(e.getPlayer());
				}
				else {
					playerTimes.put(e.getPlayer(), 0);
				}
			}
		}, fmc);
		
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority=EventPriority.LOW)
			public void OnPlayerAsyncChat(AsyncPlayerChatEvent e)
			{
				// player chatted
				if(playerTimes.get(e.getPlayer()) == -1) {
					setPlayerNotAFK(e.getPlayer());
				}
				else {
					playerTimes.put(e.getPlayer(), 0);
				}
			}
		}, fmc);
		
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority=EventPriority.LOW)
			public void OnPlayerIssuedCommand(PlayerCommandPreprocessEvent e)
			{
				// player issued a command
				if(playerTimes.get(e.getPlayer()) == -1) {
					setPlayerNotAFK(e.getPlayer());
				}
				else {
					playerTimes.put(e.getPlayer(), 0);
				}
			}
		}, fmc);
		
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority=EventPriority.NORMAL)
			public void onPlayerJoin(PlayerJoinEvent e)
			{		
				// joined, let's watch him
				playerTimes.put(e.getPlayer(), 0);
			}
		}, fmc);
		
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority=EventPriority.NORMAL)
			public void OnPlayerQuit(PlayerQuitEvent e)
			{
				// disconnected, no longer checking for AFK
				playerTimes.remove(e.getPlayer());
			}
		}, fmc);
		
		// in case of reload
		for(Player pl : fmc.getServer().getOnlinePlayers()) {
			playerTimes.put(pl, 0);
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
			
		}, 20, 20);
		
		isEnabled = true;
		return true;
	}
	
	private void setPlayerAFK(Player player)
	{
		player.setPlayerListName(player.getPlayerListName() + ChatColor.GRAY + " [" + ChatColor.RESET +"AFK" + ChatColor.GRAY + "]" + ChatColor.RESET);
		playerTimes.put(player, -1);
		
		//fmc.getServer().broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " is now AFK!");
	}
	
	private void setPlayerNotAFK(Player player)
	{
		player.setPlayerListName(player.getDisplayName());
		playerTimes.put(player, 0);
		
		//fmc.getServer().broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " is no longer AFK!");
	}

	@Override
	public void onDisable()
	{
		isEnabled = false;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
