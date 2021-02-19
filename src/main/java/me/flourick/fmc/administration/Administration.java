package me.flourick.fmc.administration;

import me.flourick.fmc.FMC;
import me.flourick.fmc.stats.Stats;
import me.flourick.fmc.utils.CConfig;
import me.flourick.fmc.utils.IModule;
import me.flourick.fmc.utils.OfflinePlayerUtils;
import me.flourick.fmc.utils.SilentOutputSender;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Administration module to make some tasks easier.
 * 
 * @author Flourick
 */
public class Administration implements IModule, CommandExecutor
{
	private boolean isEnabled = false;

	private SilentOutputSender soSender;

	private final FMC fmc;
	private final CConfig administrationConfig;

	private String tabHeader = null;
	private String tabFooter = null;

	public Administration(FMC fmc)
	{
		this.fmc = fmc;
		this.administrationConfig = new CConfig(fmc, "administration.yml");

		// Creates default config if not present
		administrationConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("inactive").setExecutor(this);
		fmc.getCommand("inactive").setTabCompleter(new InactiveTabCompleter());

		fmc.getCommand("deluser").setTabCompleter(new DelUserTabCompleter());
		fmc.getCommand("deluser").setExecutor(this);

		soSender = new SilentOutputSender(fmc.getServer().getConsoleSender());

		if(administrationConfig.getConfig().getBoolean("enable-custom-tab-header")) {
			tabHeader = ChatColor.translateAlternateColorCodes('&', administrationConfig.getConfig().getString("custom-tab-header-message"));
		}

		if(administrationConfig.getConfig().getBoolean("enable-custom-tab-footer")) {
			tabFooter = ChatColor.translateAlternateColorCodes('&', administrationConfig.getConfig().getString("custom-tab-footer-message"));
		}

		if(tabFooter != null || tabHeader != null) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onPlayerJoin(PlayerJoinEvent event)
				{
					String tps = getTPS();
					String mspt = getMSPT();

					event.getPlayer().setPlayerListHeaderFooter(tabHeader, tabFooter == null ? null : tabFooter.replace("{TPS}", tps).replace("{MSPT}", mspt));
				}
			}, fmc);

			fmc.getServer().getScheduler().scheduleSyncRepeatingTask(fmc, new Runnable() {
				@Override
				public void run()
				{
					String tps = getTPS();
					String mspt = getMSPT();

					for(Player player : Bukkit.getOnlinePlayers()) {
						player.setPlayerListHeaderFooter(tabHeader, tabFooter == null ? null : tabFooter.replace("{TPS}", tps).replace("{MSPT}", mspt));
					}
				}
			}, 20, 20 * 2);
		}

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
			if(args.length == 1) {
				// check if player is whitelisted
				String[] argz = args[0].split("/");

				if(argz.length == 2) {
					try {
						String name = argz[0];
						UUID uuid = UUID.fromString(argz[1]);

						if(!Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) {
							sender.sendMessage(ChatColor.RED + "Given player never even played on this server!");
							return true;
						}

						// remove from whitelist
						if(Bukkit.getOfflinePlayer(uuid).isWhitelisted()) {
							fmc.getServer().dispatchCommand(soSender, "whitelist remove " + name);
						}

						// rename base marker if DynFMC is enabled & running
						if(fmc.isModuleRunning("DynFMC")) {
							soSender.clearCurrentMessages();
							fmc.getServer().dispatchCommand(soSender,
									"dmarker update set:bases label:" + name + " newlabel:ABANDONED-(" + name + ")");
							List<String> response = soSender.consumeCurrentMessages();

							if(response.isEmpty()) {
								sender.sendMessage(ChatColor.RED + "Error updating base marker!");
							}
							else if (response.get(0).startsWith("Error:")) {
								sender.sendMessage("Player did not have a base set!");
							}
							else {
								sender.sendMessage("Sucessfully updated base marker!");
							}
						}

						// mark player as inactive if Stats is enabled & running
						if(fmc.isModuleRunning("Stats")) {
							Stats stats = (Stats) fmc.runningModules.get("Stats");

							if(!stats.sql.setInactivePlayer(name, true)) {
								sender.sendMessage(ChatColor.RED + "Error setting inactivity in your Stats database!");
							}
							else {
								sender.sendMessage("Inactivity updated in database!");
							}
						}
					}
					catch (IllegalArgumentException e) {
						sender.sendMessage(ChatColor.RED + "Could not find such player!");
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
		else if(cmd.getName().toLowerCase().equals("deluser")) {
			if(args.length == 1) {
				String[] argz = args[0].split("/");

				if(argz.length == 2) {
					if(OfflinePlayerUtils.deleteUserDataFiles(argz[1])) {
						sender.sendMessage(ChatColor.GREEN + "Successfully deleted user's .dat files!");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Could not find any user's .dat files!");
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

	private String getTPS()
	{
		double tps = fmc.getServer().getTPS()[0] > 20.0D ? 20.0D : fmc.getServer().getTPS()[0];
		String color = "§2";

		if(tps < 15.0D) {
			color = "§4";
		}
		else if(tps < 18.0D) {
			color = "§6";
		}

		return color + String.format("%.01f", tps) + "§r";
	}

	private String getMSPT()
	{
		double mspt = fmc.getServer().getAverageTickTime();

		return (mspt > 50.0D ? "§4" : "§2") + String.format("%.01f", mspt) + "§r";
	}
}
