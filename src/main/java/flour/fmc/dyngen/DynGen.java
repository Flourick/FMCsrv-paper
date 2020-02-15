package flour.fmc.dyngen;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * DynGen module class
 * <p>
 * Enables OPs to generate and render dynmap tiles. Has several
 * generation modes
 * </p>
 * 
 * @author Flourick
 */
public class DynGen implements IModule, CommandExecutor
{	
	private FMC fmc;
	private boolean isEnabled = false;
	
	public DynGen(FMC fmc)
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
		// TODO: a lot
		
		return true;
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
