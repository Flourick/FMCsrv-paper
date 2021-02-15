package me.flourick.fmc.protection;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * deluser command TAB completion
 * 
 * @author Flourick
 */
public class DelUserTabCompleter implements TabCompleter
{
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
        final List<String> completions = new ArrayList<>();

		if(args.length == 1) {
			for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
				String suggestion = player.getName() + "/" + player.getUniqueId();

				if(sender instanceof Player && ((Player)sender).getUniqueId().equals(player.getUniqueId())) {
					continue;
				}

				if(suggestion.toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(suggestion);
				}
			}
		}

        return completions;
	}
}
