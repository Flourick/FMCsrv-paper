package flour.fmc.protection;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import flour.fmc.utils.LogFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTItem;

import net.md_5.bungee.api.ChatColor;

/**
 * Module containing various protection features (mostly logging)
 * 
 * @author Flourick
 */
public class Protection implements IModule, CommandExecutor
{
	private boolean isEnabled = false;
	private final FMC fmc;

	private final Logger protectionLog;

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

						protectionLog.log(Level.INFO, name + " owned by " + owner + " was killed by " + killer + " via " + entity.getLastDamageCause().getCause() + "!");
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
		if(cmd.getName().toLowerCase().equals("enderchest")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}

			if(args.length == 1) {
				String[] argz = args[0].split("/");

				Player player = (Player) sender;
				Player onlineOther = Bukkit.getPlayer(argz[0]);

				if(onlineOther == null) {
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
				else {
					// is online
					player.openInventory(onlineOther.getEnderChest());
				}
			}
			else if(args.length == 0) {
				Player player = (Player) sender;
				player.openInventory(player.getEnderChest());
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

			String folder = fmc.getDataFolder() + "\\" + "logs";
			if(!Files.isDirectory(Paths.get(folder))) {
				Files.createDirectories(Paths.get(folder));
			}

			FileHandler fh = new FileHandler(folder + "\\" + LocalDate.now() + "-protection.log", true);
			fh.setFormatter(new LogFormatter());

			protectionLog.addHandler(fh);
		}
		catch(SecurityException | IOException e) {
			return false;
		}

		return true;
	}

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

	private static boolean saveOfflinePlayerEnderChest(String uuid, Inventory enderChest)
	{
		File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");
		boolean success = false;

		if(nbtFile.exists() && nbtFile.canWrite()) {
			try {
				NBTFile file = new NBTFile(nbtFile);
				
				NBTCompoundList NBTeChest = file.getCompoundList("EnderItems");
				NBTeChest.clear();

				for(Byte i = 0; i < 27; i++) {
					ItemStack item = enderChest.getItem(i);
					if(item != null && item.getType() != Material.AIR) {
						NBTCompound compound = NBTItem.convertItemtoNBT(item);
						compound.setByte("Slot", i);
						NBTeChest.addCompound(compound);
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
