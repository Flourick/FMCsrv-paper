package me.flourick.fmc.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Removes the usual names from TAB completion that may be used by default in Paper.
 * 
 * @author Flourick
 */
public class EmptyTabCompleter implements TabCompleter
{
	@Override
	public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] strings)
	{
		return new ArrayList<>();
	}
}
