package flour.fmc.dynfmc;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * DynFMC module class
 * 
 * @author Flourick
 */
public class DynFMC implements IModule, CommandExecutor
{	
	private final FMC fmc;
	private boolean isEnabled = false;
	
	public DynFMC(FMC fmc)
	{
		this.fmc = fmc;
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("dyngen").setExecutor(this);
		fmc.getCommand("dyngen").setTabCompleter(new DynGenTabCompleter());
		
		isEnabled = true;
		return true;
	}

	@Override
	public void onDisable()
	{
		isEnabled = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		if(cmd.getName().toLowerCase().equals("dyngen")) {
			if(args.length < 2) {
				return false;
			}
			
			if(args[0].equals("square") || args[0].equals("round")) {
				if(args.length == 2) {
					// radius
					if(!(sender instanceof Player)){
						sender.sendMessage(ChatColor.RED + "Only players don't need to specify coordinates!");
						return true;
					}
					
					// radius arg check
					if(!NumberUtils.isNumber(args[1])) {
						sender.sendMessage(ChatColor.RED + "Invalid radius argument " + "\'" + args[1] + "\'" + ".");
						return true;
					}
					int radius = Integer.parseInt(args[1]);
					
					if(radius < 5 || radius > 200000) {
						sender.sendMessage(ChatColor.RED + "Invalid radius argument size " + "\'" + args[1] + "\'" + ".");
						return true;
					}
					
					// all good, lets make work!
					Player player = (Player) sender;
					if(args[0].equals("round")) {
						dynGenRound(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockZ(), radius);
					}
					else {
						dynGenSquare(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockZ(), radius);
					}
				}
				else if(args.length == 4) {
					// radius and coordinates
					if(!(sender instanceof Player)){
						sender.sendMessage(ChatColor.RED + "Only players don't need to specify world!");
						return true;
					}
					
					// radius check
					if(!NumberUtils.isNumber(args[1])) {
						sender.sendMessage(ChatColor.RED + "Invalid radius argument " + "\'" + args[1] + "\'" + ".");
						return true;
					}
					int radius = Integer.parseInt(args[1]);
					
					if(radius < 5 || radius > 200000) {
						sender.sendMessage(ChatColor.RED + "Invalid radius argument size " + "\'" + args[1] + "\'" + ".");
						return true;
					}
					
					// x and z check
					if(!NumberUtils.isNumber(args[2])) {
						sender.sendMessage(ChatColor.RED + "Invalid x argument " + "\'" + args[2] + "\'" + ".");
						return true;
					}
					if(!NumberUtils.isNumber(args[3])) {
						sender.sendMessage(ChatColor.RED + "Invalid z argument " + "\'" + args[3] + "\'" + ".");
						return true;
					}
					int x = Integer.parseInt(args[2]);
					int z = Integer.parseInt(args[3]);
					
					if(x > 3000000 || x < -3000000) {
						sender.sendMessage(ChatColor.RED + "Invalid x argument size " + "\'" + args[2] + "\'" + ".");
						return true;
					}
					if(z > 3000000 || z < -3000000) {
						sender.sendMessage(ChatColor.RED + "Invalid z argument size " + "\'" + args[3] + "\'" + ".");
						return true;
					}
					
					// all good, lets make work!
					Player player = (Player) sender;
					if(args[0].equals("round")) {
						dynGenRound(player.getWorld(), x, z, radius);
					}
					else {
						dynGenSquare(player.getWorld(), x, z, radius);
					}
				}
				else if(args.length == 5) {
					// radius, coordinates and world (this is mainly for console)
					
					// radius check
					if(!NumberUtils.isNumber(args[1])) {
						sender.sendMessage(ChatColor.RED + "Invalid radius argument " + "\'" + args[1] + "\'" + ".");
						return true;
					}
					int radius = Integer.parseInt(args[1]);
					
					if(radius < 5 || radius > 200000) {
						sender.sendMessage(ChatColor.RED + "Invalid radius argument size " + "\'" + args[1] + "\'" + ".");
						return true;
					}
					
					// x and z check
					if(!NumberUtils.isNumber(args[2])) {
						sender.sendMessage(ChatColor.RED + "Invalid x argument " + "\'" + args[2] + "\'" + ".");
						return true;
					}
					if(!NumberUtils.isNumber(args[3])) {
						sender.sendMessage(ChatColor.RED + "Invalid z argument " + "\'" + args[3] + "\'" + ".");
						return true;
					}
					int x = Integer.parseInt(args[2]);
					int z = Integer.parseInt(args[3]);
					
					if(x > 3000000 || x < -3000000) {
						sender.sendMessage(ChatColor.RED + "Invalid x argument size " + "\'" + args[2] + "\'" + ".");
						return true;
					}
					if(z > 3000000 || z < -3000000) {
						sender.sendMessage(ChatColor.RED + "Invalid z argument size " + "\'" + args[3] + "\'" + ".");
						return true;
					}
					
					World foundWorld = null;
					
					// world check
					for(World world : fmc.getServer().getWorlds()) {
						if(world.getName().equals(args[4])) {
							foundWorld = world;
						}
					}
					
					if(foundWorld == null) {
						sender.sendMessage(ChatColor.RED + "Invalid world argument " + "\'" + args[4] + "\'" + ".");
						return true;
					}
					
					// all good, lets make work!
					if(args[0].equals("round")) {
						dynGenRound(foundWorld, x, z, radius);
					}
					else {
						dynGenSquare(foundWorld, x, z, radius);
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "Invalid number of arguments!");
					return true;
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Invalid mode argument " + "\'" + args[0] + "\'" + ".");
				return true;
			}
		}
		
		return true;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
	
	private void dynGenSquare(World world, int x, int z, int radius)
	{
		fmc.getServer().broadcastMessage("SQUARE: " + world.getName() + " " + x + "/" + z + " " + radius);
	}
	
	private void dynGenRound(World world, int x, int z, int radius)
	{
		fmc.getServer().broadcastMessage("ROUND: " + world.getName() + " " + x + "/" + z + " " + radius);
	}
}
