package flour.fmc.fun;

import flour.fmc.FMC;
import flour.fmc.utils.EmptyTabCompleter;
import flour.fmc.utils.IModule;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * FUn module class
 * <p>
 * Collection of various 'fun' features
 * </p>
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

			if(handItem == null) {
				return true;
			}
			else if(!handItem.getType().isBlock()) {
				sender.sendMessage(ChatColor.RED + "You can only put blocks on your head!");
				return true;
			}

			// first let's check if anything is already on his head
			if(player.getInventory().getHelmet() == null) {
				player.getInventory().setItemInMainHand(null);
				player.getInventory().setHelmet(handItem);
			}
			else {
				ItemStack helmet = player.getInventory().getHelmet();
				player.getInventory().setItemInMainHand(helmet);
				player.getInventory().setHelmet(handItem);
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
		return "Fun";
	}
}
