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
	private boolean isDisabled = true;
	
	public DynGen(FMC fmc)
	{
		this.fmc = fmc;
	}

	@Override
	public boolean onEnable()
	{
		fmc.getCommand("dyngen").setExecutor(this);
		
		isDisabled = false;
		return true;
	}

	@Override
	public void onDisable()
	{
		isDisabled = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		// TODO: a lot
		
		return true;
	}

	public boolean getDisabled() {
		return isDisabled;
	}
}
