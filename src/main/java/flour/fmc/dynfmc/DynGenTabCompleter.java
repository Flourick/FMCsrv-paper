package flour.fmc.dynfmc;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Tab completer for the dyngen command
 * 
 * @author Flourick
 */
public class DynGenTabCompleter implements TabCompleter
{
	private static final String[] FIRST_ARGS = {"round", "square"};
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
        final List<String> completions = new ArrayList<>();
        
		if(args.length == 1) {
			for(String arg : FIRST_ARGS) {
				if(arg.toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(arg);
				}
			}
		}
		else if(args.length == 5) {
			for(World world : sender.getServer().getWorlds()) {
				if(world.getName().toLowerCase().startsWith(args[4].toLowerCase())) {
					completions.add(world.getName());
				}
			}
		}
		
        return completions;
	}
}
