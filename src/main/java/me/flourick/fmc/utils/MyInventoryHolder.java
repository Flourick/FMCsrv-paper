package me.flourick.fmc.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * InventoryHolder implementation, used in getting offline players ender chest.
 * 
 * @author Flourick
 */
public class MyInventoryHolder implements InventoryHolder
{
	private final String uuid;
	private final Inventory inventory;

	public MyInventoryHolder(String uuid, Inventory inventory)
	{
		this.uuid = uuid;
		this.inventory = inventory;
	}

	@Override
	public Inventory getInventory()
	{
		return inventory;
	}
	
	public String getUUID()
	{
		return uuid;
	}
}
