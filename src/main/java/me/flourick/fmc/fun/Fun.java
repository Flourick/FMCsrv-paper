package me.flourick.fmc.fun;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.flourick.fmc.FMC;
import me.flourick.fmc.utils.EmptyTabCompleter;
import me.flourick.fmc.utils.IModule;

/**
 * Module for various 'fun' features
 * 
 * @author Flourick
 */
public class Fun implements IModule, CommandExecutor
{
	private boolean isEnabled = false;
	
	private final FMC fmc;
	
	public Fun(FMC fmc)
	{
		this.fmc = fmc;
	}
	
	@Override
	public boolean onEnable()
	{
		fmc.getCommand("hat").setTabCompleter(new EmptyTabCompleter());
		fmc.getCommand("hat").setExecutor(this);

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
		if(cmd.getName().toLowerCase().equals("hat")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}

			Player player = (Player) sender;
			ItemStack handItem = player.getInventory().getItemInMainHand();

			// empty hand or not a block type
			if(handItem.getType() == Material.AIR) {
				return true;
			}

			ItemStack helmet = player.getInventory().getHelmet();
			player.getInventory().setItemInMainHand(helmet);
			player.getInventory().setHelmet(handItem);
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
		return "Fun";
	}
}
