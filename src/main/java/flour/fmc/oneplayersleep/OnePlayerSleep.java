package flour.fmc.oneplayersleep;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;

/**
 * Allows just one player to skip the night by sleeping. Others can kick him out of bed if skipping is not desired
 * 
 * @author Flourick
 */
public class OnePlayerSleep implements IModule, CommandExecutor
{
	private final FMC fmc;
	private boolean isEnabled = false;
	
	private List<OnePlayerSleepRunnable> scheduledTasks;
	
	public OnePlayerSleep(FMC fmc)
	{
		this.fmc = fmc;
		this.scheduledTasks = new ArrayList<>();
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("wakeplayer").setTabCompleter(new WakePlayerTabCompleter());
		fmc.getCommand("wakeplayer").setExecutor(this);
		
		// player enters the bed
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerEnterBed(PlayerBedEnterEvent e)
			{
				if(e.getBedEnterResult() != BedEnterResult.OK) {
					return;
				}

				OnePlayerSleepRunnable sr = new OnePlayerSleepRunnable(e);
				addSleepTask(sr);
				
				TextComponent message = new TextComponent(e.getPlayer().getDisplayName() + ChatColor.YELLOW +" wants to sleep. ");
				TextComponent clickable = new TextComponent(ChatColor.YELLOW + "Click here to wake them!");
				clickable.setClickEvent(new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/wakeplayer " + e.getPlayer().getName()));
				clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "KICK FROM BED!").create()));
				message.addExtra(clickable);
				
				fmc.getServer().spigot().broadcast(message);

				//Sleep after a moment
				fmc.getServer().getScheduler().runTaskLater(fmc, sr, 100L);
			}
		}, fmc);
		
		// player leaves the bed/gets kicked out of it
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerLeaveBed(PlayerBedLeaveEvent e)
			{
				cancelSleepTask(e.getPlayer());
			}
		}, fmc);
		
		isEnabled = true;
		return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[])
	{
		if(cmd.getName().toLowerCase().equals("wakeplayer")) {
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}

			if(args.length != 1) {
				return false;
			}
			Player player = (Player) sender;

			Player target = fmc.getServer().getPlayer(args[0]);

			if(target == null) {
				player.sendMessage("§cInvalid argument " + "\'" + args[0] + "\'" + ".");
			}
			else if(target == player) {
				player.sendMessage(ChatColor.RED + "You can't wake yourself!");
			}
			else {
				if(target.isSleeping()) {
					target.wakeup(true);

					fmc.getServer().broadcastMessage(target.getDisplayName() + ChatColor.YELLOW + " got kicked out of bed by " + player.getDisplayName() + ChatColor.YELLOW + "!");
				}
				else {
					player.sendMessage(ChatColor.RED + "That player is not sleeping!");
				}
			}
		}
		
		return true;
	}
	
	public void addSleepTask(OnePlayerSleepRunnable sr)
	{
        scheduledTasks.add(sr);
    }
    
    public void cancelSleepTask(Player p)
	{
		List<OnePlayerSleepRunnable> found = new ArrayList<>();
        
		// cancel all runnables associated with a player
		for(OnePlayerSleepRunnable sr : scheduledTasks) {
			if(sr.getPlayer() == p && !sr.isCancelled()) {
				sr.cancel();
				found.add(sr);
			}
		}

		scheduledTasks.remove(found);
    }

	@Override
	public void onDisable()
	{
		isEnabled = false;
		
		fmc.getLogger().log(Level.INFO, "Disabled OnePlayerSleep module.");
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	@Override
	public String getName()
	{
		return "OnePlayerSleep";
	}
}
