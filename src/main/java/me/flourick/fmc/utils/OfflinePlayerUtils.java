package me.flourick.fmc.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTItem;

/**
 * Holder of various utility function regarding offline player saving and loading.
 * 
 * @author Flourick
 */
public class OfflinePlayerUtils
{
	private static final Map<Byte, Byte> TO_NMS_INVENTORY_INDEXES = new HashMap<Byte, Byte>()
	{
		private static final long serialVersionUID = 1L;

		{
			// armor and offhand
			put((byte)-106, (byte)40); put((byte)103, (byte)39); put((byte)102, (byte)38); put((byte)101, (byte)37); put((byte)100, (byte)36);
			// inventory
			put((byte)0, (byte)0); put((byte)1, (byte)1); put((byte)2, (byte)2); put((byte)3, (byte)3); put((byte)4, (byte)4); put((byte)5, (byte)5); put((byte)6, (byte)6); put((byte)7, (byte)7); put((byte)8, (byte)8); put((byte)9, (byte)9); put((byte)10, (byte)10); put((byte)11, (byte)11); put((byte)12, (byte)12); put((byte)13, (byte)13); put((byte)14, (byte)14); put((byte)15, (byte)15); put((byte)16, (byte)16); put((byte)17, (byte)17); put((byte)18, (byte)18); put((byte)19, (byte)19); put((byte)20, (byte)20); put((byte)21, (byte)21); put((byte)22, (byte)22); put((byte)23, (byte)23); put((byte)24, (byte)24); put((byte)25, (byte)25); put((byte)26, (byte)26); put((byte)27, (byte)27); put((byte)28, (byte)28); put((byte)29, (byte)29); put((byte)30, (byte)30); put((byte)31, (byte)31); put((byte)32, (byte)32); put((byte)33, (byte)33); put((byte)34, (byte)34); put((byte)35, (byte)35);
		}
	};

	private static final Map<Byte, Byte> FROM_NMS_INVENTORY_INDEXES = new HashMap<Byte, Byte>()
	{
		private static final long serialVersionUID = 1L;

		{
			// armor and offhand
			put((byte)40, (byte)-106); put((byte)39, (byte)103); put((byte)38, (byte)102); put((byte)37, (byte)101); put((byte)36, (byte)100);
			// inventory
			put((byte)0, (byte)0); put((byte)1, (byte)1); put((byte)2, (byte)2); put((byte)3, (byte)3); put((byte)4, (byte)4); put((byte)5, (byte)5); put((byte)6, (byte)6); put((byte)7, (byte)7); put((byte)8, (byte)8); put((byte)9, (byte)9); put((byte)10, (byte)10); put((byte)11, (byte)11); put((byte)12, (byte)12); put((byte)13, (byte)13); put((byte)14, (byte)14); put((byte)15, (byte)15); put((byte)16, (byte)16); put((byte)17, (byte)17); put((byte)18, (byte)18); put((byte)19, (byte)19); put((byte)20, (byte)20); put((byte)21, (byte)21); put((byte)22, (byte)22); put((byte)23, (byte)23); put((byte)24, (byte)24); put((byte)25, (byte)25); put((byte)26, (byte)26); put((byte)27, (byte)27); put((byte)28, (byte)28); put((byte)29, (byte)29); put((byte)30, (byte)30); put((byte)31, (byte)31); put((byte)32, (byte)32); put((byte)33, (byte)33); put((byte)34, (byte)34); put((byte)35, (byte)35);
		}
	};

	/**
	* Gets the ender chest of offline player.
	* 
	* @param name name of the player (used in the Ender Chest display)
	* @param uuid string represantation of UUID class coresponding to the player
	* 
	* @return the ender chest of given player
	*/
	public static Inventory getOfflinePlayerEnderChest(String name, String uuid)
	{
		Inventory eChest = null;

		if(Bukkit.getWorld("world") != null) {
			File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");

			if(nbtFile.exists() && nbtFile.canRead()) {
				try {
					NBTFile file = new NBTFile(nbtFile);
					NBTCompoundList enderChest = file.getCompoundList("EnderItems");

					eChest = Bukkit.createInventory(new MyInventoryHolder(uuid, eChest), InventoryType.ENDER_CHEST, name + "\'s Ender Chest");

					for(NBTCompound entry : enderChest) {
						eChest.setItem(entry.getByte("Slot"), NBTItem.convertNBTtoItem(entry));
					}
				}
				catch(IOException | SecurityException | IllegalArgumentException e) {
					eChest = null;
				}
			}
		}

		return eChest;
	}

	/**
	* Saves the ender chest of offline player.
	* 
	* @param uuid       string representation of UUID class corresponding to the player
	* @param enderChest ender chest contents, can be null to empty it
	*
	* @return {@code true} if saving was successful, {@code false} otherwise
	*/
	public static boolean saveOfflinePlayerEnderChest(String uuid, Inventory enderChest)
	{
		boolean success = false;

		if(Bukkit.getWorld("world") != null) {
			File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");

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
		}

		return success;
	}

	/**
	* Gets the inventory of offline player.
	* 
	* @param name name of the player
	* @param uuid string represantation of UUID class corresponding to the player
	* 
	* @return the inventory of given player
	*/
	public static Inventory getOfflinePlayerInventory(String name, String uuid)
	{
		Inventory inventory = null;

		File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");

		if(nbtFile.exists() && nbtFile.canRead()) {
			try {
				NBTFile file = new NBTFile(nbtFile);
				NBTCompoundList inv = file.getCompoundList("Inventory");

				inventory = Bukkit.createInventory(new MyInventoryHolder(uuid, inventory), InventoryType.PLAYER, name + "\'s Inventory");

				for(NBTCompound entry : inv) {
					Byte slot = TO_NMS_INVENTORY_INDEXES.get(entry.getByte("Slot"));

					inventory.setItem(slot, NBTItem.convertNBTtoItem(entry));				
				}
			}
			catch(IOException | SecurityException | IllegalArgumentException e) {
				inventory = null;
			}
		}

		return inventory;
	}

	/**
	* Saves the inventory of offline player.
	* 
	* @param uuid      string representation of UUID class corresponding to the player
	* @param inventory inventory contents, can be null to empty it
	*
	* @return {@code true} if saving was successful, {@code false} otherwise
	*/
	public static boolean saveOfflinePlayerInventory(String uuid, Inventory inventory)
	{
		File nbtFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");
		boolean success = false;

		if(nbtFile.exists() && nbtFile.canWrite()) {
			try {
				NBTFile file = new NBTFile(nbtFile);
				
				NBTCompoundList NBTinventory = file.getCompoundList("Inventory");
				NBTinventory.clear();

				if(inventory != null) {
					for(Byte i = 0; i < 41; i++) {
						ItemStack item = inventory.getItem(i);
						
						if(item != null && item.getType() != Material.AIR) {
							NBTCompound compound = NBTItem.convertItemtoNBT(item);
							compound.setByte("Slot", FROM_NMS_INVENTORY_INDEXES.get(i));
							NBTinventory.addCompound(compound);
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

	/**
	* Deletes users .dat files, basically resetting all of his progress.
	* 
	* @param uuid string representation of UUID class corresponding to the player
	*
	* @return {@code true} if deletion was successful, {@code false} if not or no files found for given user
	*/
	public static boolean deleteUserDataFiles(String uuid)
	{
		File dataFile = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");
		File dataFileOld = new File(new File(Bukkit.getWorld("world").getWorldFolder(), "playerdata"), uuid + ".dat");
		boolean success = false;

		if(dataFile.exists() && dataFile.canWrite()) {
			try {
				dataFile.delete();
				success = true;
			}
			catch(SecurityException e) {
				// nada
			}
		}

		if(dataFileOld.exists() && dataFileOld.canWrite()) {
			try {
				dataFileOld.delete();
				success = true;
			}
			catch(SecurityException e) {
				// nada
			}
		}

		return success;
	}
}
