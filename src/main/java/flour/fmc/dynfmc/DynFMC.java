package flour.fmc.dynfmc;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
			if(args.length < 1) {
				return false;
			}
			
			
		}
		
		return true;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
