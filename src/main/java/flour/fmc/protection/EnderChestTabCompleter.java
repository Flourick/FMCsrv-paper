package flour.fmc.protection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * colorme command TAB completion
 * 
 * @author Flourick
 */
public class EnderChestTabCompleter implements TabCompleter
{	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
        final List<String> completions = new ArrayList<>();
		
		 Bukkit.getOfflinePlayers();

		if(args.length == 1) {
			for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
				String name = player.getName();
				UUID uuid = player.getUniqueId();

				if(name.toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(name + "/" + uuid.toString());
				}
			}
		}

        return completions;
	}
}
