package me.flourick.fmc.administration;

import me.flourick.fmc.FMC;
import me.flourick.fmc.stats.Stats;
import me.flourick.fmc.utils.IModule;
import me.flourick.fmc.utils.SilentOutputSender;
import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Administration module class
 * <p>
 * Various admin features
 * </p>
 * 
 * @author Flourick
 */
public class Administration implements IModule, CommandExecutor
{
	private boolean isEnabled = false;

	private SilentOutputSender soSender;

	private final FMC fmc;
	
	public Administration(FMC fmc)
	{
		this.fmc = fmc;
	}
	
	@Override
	public boolean onEnable()
	{		
		fmc.getCommand("inactive").setExecutor(this);
		fmc.getCommand("inactive").setTabCompleter(new InactiveTabCompleter());

		soSender = new SilentOutputSender(fmc.getServer().getConsoleSender());
		
		return isEnabled = true;
	}
	
	@Override
	public void onDisable()
	{
		isEnabled = false;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String args[])
	{
		if(cmd.getName().toLowerCase().equals("inactive")) {
			// if(!(sender instanceof Player)){
			// 	sender.sendMessage(ChatColor.RED + "Players only command.");
			// 	return true;
			// }

			if(args.length == 1) {
				// check if player is whitelisted
				String[] argz = args[0].split("/");

				if(argz.length == 2) {
					String name = argz[0];
					UUID uuid = UUID.fromString(argz[1]);

					if(!Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) {
						sender.sendMessage(ChatColor.RED + "Given player never even played on this server!");
						return true;
					}

					if(Bukkit.getOfflinePlayer(uuid).isWhitelisted()) {
						fmc.getServer().dispatchCommand(soSender, "whitelist remove " + name);
					}

					// rename base marker if DynFMC is enabled & running
					if(fmc.isModuleRunning("DynFMC")) {
						soSender.clearCurrentMessages();
						fmc.getServer().dispatchCommand(soSender, "dmarker update set:bases label:" + name + " newlabel:ABANDONED-(" + name + ")");
						List<String> response = soSender.consumeCurrentMessages();

						if(response.isEmpty()) {
							sender.sendMessage(ChatColor.RED + "Error updating base marker!");
						}
						else if(response.get(0).startsWith("Error:")) {
							sender.sendMessage("Player did not have a base set!");
						}
						else {
							sender.sendMessage("Sucessfully updated base marker!");
						}
					}

					// mark player as inactive if Stats is enabled & running
					if(fmc.isModuleRunning("Stats")) {
						Stats stats = (Stats)fmc.runningModules.get("Stats");

						if(!stats.sql.setInactivePlayer(name, true)) {
							sender.sendMessage(ChatColor.RED + "Error setting inactivity in your Stats database!");
						}
						else {
							sender.sendMessage("Inactivity updated in database!");
						}
					}
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}
	
	@Override
	public String getName()
	{
		return "Administration";
	}
}
