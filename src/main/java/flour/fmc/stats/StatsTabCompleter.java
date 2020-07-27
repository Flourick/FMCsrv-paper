package flour.fmc.stats;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * TAB completion for wakeplayer command
 * 
 * @author Flourick
 */
public class StatsTabCompleter implements TabCompleter
{
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
		final List<String> completions = new ArrayList<>();
		
		if(args.length == 1) {
			for(Player player : sender.getServer().getOnlinePlayers()) {
				if(player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(player.getName());
				}
			}
		}

        return completions;
	}
}