package me.flourick.fmc.protection;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * enderchest command TAB completion
 * 
 * @author Flourick
 */
public class EnderChestTabCompleter implements TabCompleter
{
	private static final String[] FIRST_ARGS = { "open", "drop"};

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
        final List<String> completions = new ArrayList<>();

		if(args.length == 1) {
			for(String arg : FIRST_ARGS) {
				if(arg.startsWith(args[0].toLowerCase())) {
					completions.add(arg);
				}
			}
		}
		else if(args.length == 2 && sender.hasPermission("fmc.enderchest.others")) {
			for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
				String suggestion = player.getName() + "/" + player.getUniqueId();

				if(suggestion.toLowerCase().startsWith(args[1].toLowerCase())) {
					completions.add(suggestion);
				}
			}
		}

        return completions;
	}
}
