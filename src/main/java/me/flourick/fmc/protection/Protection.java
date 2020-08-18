package me.flourick.fmc.protection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.flourick.fmc.FMC;
import me.flourick.fmc.utils.CConfig;
import me.flourick.fmc.utils.IModule;
import me.flourick.fmc.utils.LogFormatter;
import me.flourick.fmc.utils.MyInventoryHolder;
import me.flourick.fmc.utils.OfflinePlayerUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Module containing protection related features
 * 
 * @author Flourick
 */
public class Protection implements IModule, CommandExecutor
{
	private boolean isEnabled = false;

	private final CConfig protectionConfig;
	private final FMC fmc;

	private final Logger protectionLog;
	private final Path protectionLogsFolder;
	private FileHandler protectionLogFileHandler;
	private Path currentLogPath;

	private final HashMap<String, Inventory> openedEnderChests;

	public Protection(FMC fmc)
	{
		this.fmc = fmc;
		this.openedEnderChests = new HashMap<>();

		this.protectionLogsFolder = Paths.get(fmc.getDataFolder().toString(), "logs");
		this.protectionLog = Logger.getLogger("Protection");

		this.protectionConfig = new CConfig(fmc, "protection.yml");
		// Creates default config if not present
		protectionConfig.saveDefaultConfig();
	}

	@Override
	public boolean onEnable()
	{
		if(!setupLogs()) {
			return isEnabled = false;
		}

		initLogs();

		fmc.getCommand("enderchest").setTabCompleter(new EnderChestTabCompleter());
		fmc.getCommand("enderchest").setExecutor(this);

		fmc.getCommand("inventory").setTabCompleter(new InventoryTabCompleter());
		fmc.getCommand("inventory").setExecutor(this);

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

		// when you close an ender chest of offline player, last to have it opened also triggers saving and removal from map
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onInventoryClose(InventoryCloseEvent event)
			{
				if(event.getInventory().getType() == InventoryType.ENDER_CHEST && event.getInventory().getHolder() instanceof MyInventoryHolder) {
					if(event.getViewers().size() < 2) {
						String uuid = ((MyInventoryHolder)event.getInventory().getHolder()).getUUID();
						openedEnderChests.remove(uuid);
						
						if(!OfflinePlayerUtils.saveOfflinePlayerEnderChest(uuid, event.getInventory())) {
							fmc.getLogger().info(ChatColor.RED + "[Protection] Error saving Ender Chest!");
						}
					}
				}
			}
		}, fmc);

		return isEnabled = true;
	}

	private void initLogs()
	{
		// logs pet deaths becouse for some reason vanilla does not do that
		if(protectionConfig.getConfig().getBoolean("logger.pets")) {
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
		}

		// logging of chests opening
		if(protectionConfig.getConfig().getBoolean("logger.chests")) {
			fmc.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onPlayerInteractEvent(PlayerInteractEvent event)
				{
					if(event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if(event.getClickedBlock().getType() == Material.CHEST) {
							log(Level.INFO, event.getPlayer().getName() + " opened a chest at [" + event.getClickedBlock().getLocation().getBlockX() + ", " + event.getClickedBlock().getLocation().getBlockY() + ", " + event.getClickedBlock().getLocation().getBlockZ() + "] in " + event.getPlayer().getWorld().getName());
						}
						else if(event.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
							log(Level.INFO, event.getPlayer().getName() + " opened a trapped chest at [" + event.getClickedBlock().getLocation().getBlockX() + ", " + event.getClickedBlock().getLocation().getBlockY() + ", " + event.getClickedBlock().getLocation().getBlockZ() + "] in " + event.getPlayer().getWorld().getName());
						}
					}
				}
			}, fmc);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String args[])
	{
		// isn't this a beautiful spaghetti monster?
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
						// is offline
						try {
							if(Bukkit.getOfflinePlayer(UUID.fromString(argz[1])).hasPlayedBefore()) {
								Inventory enderChest = OfflinePlayerUtils.getOfflinePlayerEnderChest(argz[0], argz[1]);
								
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

									OfflinePlayerUtils.saveOfflinePlayerEnderChest(argz[1], null);
								}
								else {
									player.sendMessage(ChatColor.RED + "Could not drop the Ender Chest!");
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
									enderChest = OfflinePlayerUtils.getOfflinePlayerEnderChest(argz[0], argz[1]);
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
		else if(cmd.getName().toLowerCase().equals("inventory")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}

			if(args.length == 2) {
				if(args[0].equals("drop")) {
					// dropping someone elses inventory
					String[] argz = args[1].split("/");

					Player player = (Player) sender;
					Player onlineOther = Bukkit.getPlayer(argz[0]);

					if(onlineOther == null && argz.length == 2) {
						// is offline
						try {
							if(Bukkit.getOfflinePlayer(UUID.fromString(argz[1])).hasPlayedBefore()) {
								Inventory inventory = OfflinePlayerUtils.getOfflinePlayerInventory(argz[0], argz[1]);

								if(inventory != null) {
									for(ItemStack item : inventory.getContents()) {
										if(item != null && item.getType() != Material.AIR) {
											player.getWorld().dropItemNaturally(player.getLocation(), item);
										}
									}

									OfflinePlayerUtils.saveOfflinePlayerInventory(argz[1], null);
								}
								else {
									player.sendMessage(ChatColor.RED + "Could not drop the Inventory!");
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
						for(ItemStack item : onlineOther.getInventory().getContents()) {
							if(item != null && item.getType() != Material.AIR) {
								player.getWorld().dropItemNaturally(player.getLocation(), item);
							}
						}

						onlineOther.getInventory().clear();
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
				// dropping my own inventory
				if(args[0].equals("drop")) {
					Player player = (Player) sender;

					for(ItemStack item : player.getInventory().getContents()) {
						if(item != null && item.getType() != Material.AIR) {
							player.getWorld().dropItemNaturally(player.getLocation(), item);
						}
					}

					player.getInventory().clear();
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

			if(!Files.isDirectory(protectionLogsFolder)) {
				Files.createDirectories(protectionLogsFolder);
			}

			Path previousLogPath = getPreviousProtectionLog();
			currentLogPath = Paths.get(protectionLogsFolder.toString(), LocalDate.now() + "-protection.log");

			if(previousLogPath != null && !previousLogPath.getFileName().toString().substring(0, 7).equals(currentLogPath.getFileName().toString().substring(0, 7))){
				compressMonthLog(previousLogPath.getFileName().toString().substring(0, 7));
			}

			protectionLogFileHandler = new FileHandler(currentLogPath.toString(), true);
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
		Path today = Paths.get(protectionLogsFolder.toString(), LocalDate.now() + "-protection.log");

		if(Files.notExists(today)) {
			try {
				protectionLogFileHandler.close();
				protectionLog.removeHandler(protectionLogFileHandler);

				// another month
				if(!today.getFileName().toString().substring(5, 7).equals(currentLogPath.getFileName().toString().substring(5, 7))) {
					compressMonthLog(currentLogPath.getFileName().toString().substring(0, 7));
				}

				protectionLogFileHandler = new FileHandler(today.toString(), true);
				protectionLogFileHandler.setFormatter(new LogFormatter());
			}
			catch (SecurityException | IOException e) {
				fmc.getLogger().log(Level.SEVERE, "Error creating new daily Protection log file!");
				return;
			}

			protectionLog.addHandler(protectionLogFileHandler);
		}

		protectionLog.log(level, msg);
	}

	private void compressMonthLog(String folderName)
	{
		Path monthFolder = Paths.get(protectionLogsFolder.toString(), folderName);

		try {
			Files.createDirectories(monthFolder);

			File[] files = protectionLogsFolder.toFile().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name)
				{
					if(name.toLowerCase().endsWith(".log")) {
						return true;
					}
					else {
						return false;
					}
				}
			});

			boolean empty = true;

			for(File logFile : files) {
				// only move valid and non-empty files
				if(logFile.length() == 0) {
					Files.delete(logFile.toPath());
				}
				else {
					Files.move(logFile.toPath(), monthFolder.resolve(logFile.toPath().getFileName()));
					empty = false;
				}
			}

			// no need to zip no files
			if(empty) { 
				return;
			}

			// now zip the folder
			zipFolder(monthFolder, Paths.get(protectionLogsFolder.toString(), folderName + ".zip"));
			// and remove the old one
			Files.delete(monthFolder);
		}
		catch (SecurityException | IOException e) {
			fmc.getLogger().log(Level.SEVERE, "Error creating month log directory!");
			return;
		}
	}

	private void zipFolder(Path source, Path output) throws IOException 
	{
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output.toFile()));
		
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
                zos.putNextEntry(new ZipEntry(source.relativize(file).toString()));
                Files.copy(file, zos);
				zos.closeEntry();

				Files.delete(file);
				
                return FileVisitResult.CONTINUE;
            }
		});
		
        zos.close();
	}
	
	private Path getPreviousProtectionLog()
	{
		File[] files = protectionLogsFolder.toFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name)
			{
				if(name.toLowerCase().endsWith(".log")) {
					return true;
				}
				else {
					return false;
				}
			}
		});

		Path newest = null;

		for(File file : files) {
			if(newest == null) {
				newest = file.toPath();
			}
			else if(file.toPath().getFileName().toString().compareTo(newest.toString()) > 0) {
				newest = file.toPath();
			}
		}

		return newest;
	}
}
