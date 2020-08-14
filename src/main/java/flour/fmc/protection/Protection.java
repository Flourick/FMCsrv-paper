package flour.fmc.protection;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import flour.fmc.utils.LogFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTItem;

import net.md_5.bungee.api.ChatColor;

/**
 * Module containing various protection features
 * 
 * @author Flourick
 */
public class Protection implements IModule, CommandExecutor
{
	private boolean isEnabled = false;
	private final FMC fmc;

	private final Logger protectionLog;
	private FileHandler protectionLogFileHandler;

	private final HashMap<String, Inventory> openedEnderChests;

	public Protection(FMC fmc)
	{
		this.fmc = fmc;
		this.openedEnderChests = new HashMap<>();
		protectionLog = Logger.getLogger("Protection");
	}

	@Override
	public boolean onEnable()
	{
		if(!setupLogs()) {
			return isEnabled = false;
		}

		fmc.getCommand("enderchest").setTabCompleter(new EnderChestTabCompleter());
		fmc.getCommand("enderchest").setExecutor(this);

		// logs pet deaths becouse for some reason vanilla does not do that
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onEntityDeath(EntityDeathEvent event)
			{
				if(event.getEntity() instanceof Tameable) {
					Tameable entity = (Tameable) event.getEntity();

					if(entity.isTamed()) {
						String name = entity.getCustomName() == null ? entity.getName() : entity.getCustomName();
						String killer = entity.getKiller() == null ? "unknown" : entity.getKiller().getName();
						String owner = entity.getOwner().getName() == null ? "unknown" : entity.getOwner().getName();

						log(Level.INFO, name + " owned by " + owner + " was killed by " + killer + " via " + entity.getLastDamageCause().getCause() + "!");
					}
				}
			}
		}, fmc);

		// A player whos ender chest is currently opened is attempting to join so we close it to prevent duplication (bit hacky but eh)
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority = EventPriority.LOWEST)
			public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event)
			{
				if(openedEnderChests.containsKey(event.getUniqueId().toString())) {
					List<HumanEntity> v = openedEnderChests.get(event.getUniqueId().toString()).getViewers();
					HumanEntity[] viewers = new HumanEntity[v.size()];
					v.toArray(viewers);

					for(HumanEntity viewer : viewers) {
						Bukkit.getScheduler().runTask(fmc, () -> {
							viewer.closeInventory();
						});
					}
				}
			}
		}, fmc);

		// when you close an ender chest of offline player, last to have it opened also triggers saving and removal from hash map
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onInventoryClose(InventoryCloseEvent event)
			{
				if(event.getInventory().getType() == InventoryType.ENDER_CHEST && event.getInventory().getHolder() instanceof EnderChestInventoryHolder) {
					if(event.getViewers().size() < 2) {
						String uuid = ((EnderChestInventoryHolder)event.getInventory().getHolder()).getUUID();
						openedEnderChests.remove(uuid);
						
						if(!saveOfflinePlayerEnderChest(uuid, event.getInventory())) {
							fmc.getLogger().info(ChatColor.RED + "[Protection] Error saving Ender Chest!");
						}
					}
				}
			}
		}, fmc);

		return isEnabled = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String args[])
	{
		// be ware that duplication is still possible in case of forced disconnects/restarts etc..
		// also isn't this a beautiful spaghetti monster?
		if(cmd.getName().toLowerCase().equals("enderchest")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}

			if(args.length == 2) {
				if(args[0].equals("drop")) {
					if(!sender.hasPermission("fmc.enderchest.others")) {
						sender.sendMessage(ChatColor.RED + "You do not have permission to drop others Ender Chest!");
						return true;
					}

					String[] argz = args[1].split("/");

					Player player = (Player) sender;
					Player onlineOther = Bukkit.getPlayer(argz[0]);

					if(onlineOther == null && argz.length == 2) {
						try {
							if(Bukkit.getOfflinePlayer(UUID.fromString(argz[1])).hasPlayedBefore()) {
								Inventory enderChest = getOfflinePlayerEnderChest(argz[0], argz[1]);
								
								if(openedEnderChests.containsKey(argz[1])) {
									// cannot drop ender chest if someone else is already editing it (becouse possible duplication)
									player.sendMessage(ChatColor.RED + "That Ender Chest is currently open, cannot drop!");
								}
								else if(enderChest != null) {
									for(ItemStack item : enderChest.getContents()) {
										if(item != null && item.getType() != Material.AIR) {
											player.getWorld().dropItemNaturally(player.getLocation(), item);
										}
									}

									saveOfflinePlayerEnderChest(argz[1], null);
								}
								else {
									player.sendMessage(ChatColor.RED + "Could not open the Ender Chest!");
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "Could not find such player.");
							}
						}
						catch(IllegalArgumentException e) {
							player.sendMessage(ChatColor.RED + "Could not find such player.");
						}
					}
					else if(onlineOther != null){
						// is online
						for(ItemStack item : onlineOther.getEnderChest().getContents()) {
							if(item != null && item.getType() != Material.AIR) {
								player.getWorld().dropItemNaturally(player.getLocation(), item);
							}
						}

						onlineOther.getEnderChest().clear();
					}
					else {
						player.sendMessage(ChatColor.RED + "Could not find such player.");
					}
				}
				else if(args[0].equals("open")) {
					if(!sender.hasPermission("fmc.enderchest.others")) {
						sender.sendMessage(ChatColor.RED + "You do not have permission to open others Ender Chest!");
						return true;
					}

					String[] argz = args[1].split("/");

					Player player = (Player) sender;
					Player onlineOther = Bukkit.getPlayer(argz[0]);

					if(onlineOther == null && argz.length == 2) {
						try {
							if(Bukkit.getOfflinePlayer(UUID.fromString(argz[1])).hasPlayedBefore()) {
								Inventory enderChest = null;
	
								// use the cached enderchest so more ppl can simultaneously edit it
								if(openedEnderChests.containsKey(argz[1])) {
									enderChest = openedEnderChests.get(argz[1]);
								}
								else {
									enderChest = getOfflinePlayerEnderChest(argz[0], argz[1]);
								}
								
								if(enderChest != null) {
									openedEnderChests.put(argz[1], enderChest);
									player.openInventory(enderChest);						
								}
								else {
									player.sendMessage(ChatColor.RED + "Could not open the Ender Chest!");
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "Could not find such player.");
							}
						}
						catch(IllegalArgumentException e) {
							player.sendMessage(ChatColor.RED + "Could not find such player.");
						}
					}
					else if(onlineOther != null) {
						// is online
						player.openInventory(onlineOther.getEnderChest());
					}
					else {
						player.sendMessage(ChatColor.RED + "Could not find such player.");
					}
				}
				else {
					return false;
				}
			}
			else if(args.length == 1) {
				// opening or dropping my own enderchest
				if(args[0].equals("drop")) {
					Player player = (Player) sender;

					for(ItemStack item : player.getEnderChest().getContents()) {
						if(item != null && item.getType() != Material.AIR) {
							player.getWorld().dropItemNaturally(player.getLocation(), item);
						}
					}

					player.getEnderChest().clear();
				}
				else if(args[0].equals("open")) {
					Player player = (Player) sender;
					player.openInventory(player.getEnderChest());
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
		return "Protection";
	}

	private boolean setupLogs()
	{
		try {
			protectionLog.setUseParentHandlers(false);

			Path folder = Paths.get(fmc.getDataFolder().toString(), "logs");
			if(!Files.isDirectory(folder)) {
				Files.createDirectories(folder);
			}

			protectionLogFileHandler = new FileHandler(Paths.get(folder.toString(), LocalDate.now() + "-protection.log").toString(), true);
			protectionLogFileHandler.setFormatter(new LogFormatter());

			protectionLog.addHandler(protectionLogFileHandler);
		}
		catch(SecurityException | IOException e) {
			return false;
		}

		return true;
	}

	/**
	* Wrapper for Logger.log method
	*/
	private void log(Level level, String msg)
	{
		Path today = Paths.get(Paths.get(fmc.getDataFolder().toString(), "logs").toString(), LocalDate.now() + "-protection.log");

		if(Files.notExists(today)) {
			try {
				protectionLogFileHandler.close();
				protectionLog.removeHandler(protectionLogFileHandler);
				protectionLogFileHandler = new FileHandler(today.toString(), true);
				protectionLogFileHandler.setFormatter(new LogFormatter());
			}
			catch (SecurityException | IOException e) {
				fmc.getLogger().warning("Error creating new daily Protection log file!");
				return;
			}

			protectionLog.addHandler(protectionLogFileHandler);
		}

		protectionLog.log(level, msg);
	}

	/**
	* Gets Ender Chest of offline player
	* 
	* @param  name	Name of the player (used in the Ender Chest display)
	* @param  uuid  String represantation of UUID class coresponding to the player
	* 
	* @return The Ender Chest of given player
	*/
	private static Inventory getOfflinePlayerEnderChest(String name, String uuid)
	{
		Inventory eChest = null;

		File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");

		if(nbtFile.exists() && nbtFile.canRead()) {
			try {
				NBTFile file = new NBTFile(nbtFile);
				NBTCompoundList enderChest = file.getCompoundList("EnderItems");

				eChest = Bukkit.createInventory(new EnderChestInventoryHolder(uuid, eChest), InventoryType.ENDER_CHEST, name + "\'s Ender Chest");

				for(NBTCompound entry : enderChest) {
					eChest.setItem(entry.getByte("Slot"), NBTItem.convertNBTtoItem(entry));
				}
			}
			catch(IOException | SecurityException | IllegalArgumentException e) {
				eChest = null;
			}
		}

		return eChest;
	}

	/**
	* Saves Ender Chest of offline player
	* 
	* @param  uuid        String representation of UUID class coresponding to the player
	* @param  enderChest  Ender Chest contents, can be null to empty it
	*/
	private static boolean saveOfflinePlayerEnderChest(String uuid, Inventory enderChest)
	{
		File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");
		boolean success = false;

		if(nbtFile.exists() && nbtFile.canWrite()) {
			try {
				NBTFile file = new NBTFile(nbtFile);
				
				NBTCompoundList NBTeChest = file.getCompoundList("EnderItems");
				NBTeChest.clear();

				if(enderChest != null) {
					for(Byte i = 0; i < 27; i++) {
						ItemStack item = enderChest.getItem(i);
						if(item != null && item.getType() != Material.AIR) {
							NBTCompound compound = NBTItem.convertItemtoNBT(item);
							compound.setByte("Slot", i);
							NBTeChest.addCompound(compound);
						}
					}
				}

				file.save();
				success = true;
			}
			catch(IOException | SecurityException | IllegalArgumentException e) {
				// nada
			}
		}

		return success;
	}
}
