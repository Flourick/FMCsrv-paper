package flour.fmc.colorme;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * colorme command TAB completion
 * 
 * @author Flourick
 */
public class ColorMeTabCompleter implements TabCompleter
{
	private static final String[] FIRST_ARGS = { "dark_red", "red", "gold", "yellow", "dark_green", "green", "aqua", "dark_aqua", "dark_blue", "blue", "light_purple", "dark_purple", "dark_gray", "gray", "white"};
	
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

        return completions;
	}
}
