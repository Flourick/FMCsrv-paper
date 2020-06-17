package flour.fmc.dynfmc;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import flour.fmc.utils.SilentOutputSender;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
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
	
	private SilentOutputSender soSender;
	
	public DynFMC(FMC fmc)
	{
		this.fmc = fmc;
	}

	@Override
	public boolean onEnable()
	{
		if(fmc.getServer().getPluginManager().getPlugin("dynmap") == null || !fmc.getServer().getPluginManager().getPlugin("dynmap").isEnabled()) {
			fmc.getLogger().log(Level.SEVERE, "[DynFMC] DynMap is REQUIRED for DynFMC module to work!");
			return false;
		}
		
		soSender = new SilentOutputSender(fmc.getServer().getConsoleSender());
		
		fmc.getCommand("dynfmc").setExecutor(this);
		fmc.getCommand("dynfmc").setTabCompleter(new DynFMCTabCompleter());
		
		// adds the sets if not already created
		fmc.getServer().dispatchCommand(soSender, "dmarker addset bases");
		fmc.getServer().dispatchCommand(soSender, "dmarker addset towns");
		
		return isEnabled = true;
	}

	@Override
	public void onDisable()
	{
		isEnabled = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{	
		if(cmd.getName().toLowerCase().equals("dynfmc")) {
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Players only command.");
				return true;
			}
			
			Player player = (Player) sender;
			
			if(args.length == 2 && args[0].equals("base")) {
				switch(args[1]) {
					case "set":
						int x = player.getLocation().getBlockX();
						int y = player.getLocation().getBlockY();
						int z = player.getLocation().getBlockZ();
						
						fmc.getServer().dispatchCommand(soSender, "dmarker delete " + player.getName() + " set:bases");
						fmc.getServer().dispatchCommand(soSender, "dmarker add " + player.getName() + " icon:house set:bases x:" + x + " y:" + y + " z:" + z + " world:" + player.getWorld().getName());
						player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " Placed base marker on DynMap!");
						
						return true;
						
					case "remove":
						fmc.getServer().dispatchCommand(soSender, "dmarker delete " + player.getName() + " set:bases");
						player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " Removed base marker from DynMap!");
						
						return true;
						
					case "list":
						soSender.clearCurrentMessages();
						fmc.getServer().dispatchCommand(soSender, "dmarker list set:bases");
						
						List<String> bases = soSender.consumeCurrentMessages();
						if(bases.isEmpty()) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " No bases found!");
							return true;
						}
						
						player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " Bases list:");
						for(String base : bases) {
							String[] words = base.split("\\s+");
							
							if(words.length < 2) {
								player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " No bases found!");
								return true;
							}
							
							String name = "";
							String X = "";
							String Y = "";
							String Z = "";
							String world = "";
							
							for(String word : words) {
								if(word.startsWith("label:")) {
									name = word.substring(7, word.length() - 2);
								}
								else if(word.startsWith("x:")) {
									X = word.substring(2, word.length() - 3);
								}
								else if(word.startsWith("y:")) {
									Y = word.substring(2, word.length() - 3);
								}
								else if(word.startsWith("z:")) {
									Z = word.substring(2, word.length() - 3);
								}
								else if(word.startsWith("world:")) {
									world = word.substring(6, word.length() - 1);
									
									switch(world) {
										case "world":
											world = "Overworld";
											break;
										case "world_nether":
											world = "Nether";
											break;
										case "world_the_end":
											world = "The End";
											break;
									}
								}
							}

							String coordX = ChatColor.GRAY + "X" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + X + ChatColor.DARK_GRAY + ", ";
							String coordY = ChatColor.GRAY + "Y" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + Y + ChatColor.DARK_GRAY + ", ";
							String coordZ = ChatColor.GRAY + "Z" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + Z + ChatColor.DARK_GRAY;
							
							player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + name + ChatColor.DARK_GRAY + " at " + coordX + coordY + coordZ + ChatColor.DARK_GRAY + " in " + ChatColor.RESET + world);
						}
						
						return true;
						
					default:
						break;
				}
			}
			else if(args.length > 1 && args.length < 4 && args[0].equals("town")) {
				switch(args[1]) {
					case "set":
						if(args.length != 3) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.RED + " You must supply a label for the town!");
							return true;
						}
						
						// first check if town does not already exist
						if(townExists(args[2])) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.RED + " Town with that name already exists!");
							return true;
						}
						
						// check the argument
						if(args[2].length() > 16 || args[2].matches("^.*[^a-zA-Z0-9].*$")) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.RED + " Town name max 16 alphanumerical characters!");
							return true;
						}
						
						fmc.getServer().dispatchCommand(soSender, "dmarker add " + args[2] + " icon:bighouse set:towns x:" + player.getLocation().getBlockX() + " y:" + player.getLocation().getBlockY() + " z:" + player.getLocation().getBlockZ() + " world:" + player.getWorld().getName());
						player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " Placed town marker on DynMap!");
						return true;
						
					case "remove":
						if(args.length != 3) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.RED + " You must supply a label for the town!");
							return true;
						}
						
						// first check if town even exists
						if(!townExists(args[2])) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.RED + " Town with that name does not exist!");
							return true;
						}
						
						fmc.getServer().dispatchCommand(soSender, "dmarker delete " + args[2] + " set:towns");
						player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " Removed town marker '" + args[2] + "' from DynMap!");
						return true;
						
					case "list":
						soSender.clearCurrentMessages();
						fmc.getServer().dispatchCommand(soSender, "dmarker list set:towns");
						
						List<String> towns = soSender.consumeCurrentMessages();
						if(towns.isEmpty()) {
							player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " No towns found!");
							return true;
						}
						
						player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " Towns list:");
						for(String town : towns) {
							String[] words = town.split("\\s+");
							
							if(words.length < 2) {
								player.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + "DynFMC" + ChatColor.DARK_GREEN + "]" + ChatColor.YELLOW + " No towns found!");
								return true;
							}
							
							String name = "";
							String X = "";
							String Y = "";
							String Z = "";
							String world = "";
							
							for(String word : words) {
								if(word.startsWith("label:")) {
									name = word.substring(7, word.length() - 2);
								}
								else if(word.startsWith("x:")) {
									X = word.substring(2, word.length() - 3);
								}
								else if(word.startsWith("y:")) {
									Y = word.substring(2, word.length() - 3);
								}
								else if(word.startsWith("z:")) {
									Z = word.substring(2, word.length() - 3);
								}
								else if(word.startsWith("world:")) {
									world = word.substring(6, word.length() - 1);
									
									// more readable shit
									switch(world) {
										case "world":
											world = "Overworld";
											break;
										case "world_nether":
											world = "Nether";
											break;
										case "world_the_end":
											world = "The End";
											break;
									}
								}
							}

							String coordX = ChatColor.GRAY + "X" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + X + ChatColor.DARK_GRAY + ", ";
							String coordY = ChatColor.GRAY + "Y" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + Y + ChatColor.DARK_GRAY + ", ";
							String coordZ = ChatColor.GRAY + "Z" + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + Z + ChatColor.DARK_GRAY;
							
							player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GRAY + name + ChatColor.DARK_GRAY + " at " + coordX + coordY + coordZ + ChatColor.DARK_GRAY + " in " + ChatColor.RESET + world);
						}
						
						return true;
						
					default:
						break;
				}
			}
		}
		
		return false;
	}
	
	private boolean townExists(String townName)
	{
		soSender.clearCurrentMessages();
		fmc.getServer().dispatchCommand(soSender, "dmarker list set:towns");
		List<String> messages = soSender.consumeCurrentMessages();
		if(!messages.isEmpty()) {
			List<String> towns = new ArrayList<>();

			messages.forEach((town) -> {
				String[] words = town.split("\\s+");

				for(String word : words) {
					if(word.contains("label:")) {
						towns.add(word.substring(7, word.length() - 2));
					}
				}
			});

			if(towns.contains(townName)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	@Override
	public String getName()
	{
		return "DynFMC";
	}
}