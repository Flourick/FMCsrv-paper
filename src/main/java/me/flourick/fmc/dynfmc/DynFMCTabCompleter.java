package me.flourick.fmc.dynfmc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * TAB completion for dynfmc command.
 * 
 * @author Flourick
 */
public class DynFMCTabCompleter implements TabCompleter
{
	private static final String[] FIRST_ARGS = { "base", "town", "poi"};
	private static final String[] SECOND_ARGS = { "set", "remove", "list"};

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
		else if(args.length == 2) {
			for(String arg : SECOND_ARGS) {
				if(arg.toLowerCase().startsWith(args[1].toLowerCase())) {
					completions.add(arg);
				}
			}
		}

        return completions;
	}
}