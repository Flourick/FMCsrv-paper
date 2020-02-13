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
 * generation modes (square, round, spawn)
 * </p>
 * 
 * @author Flourick
 */
public class DynGen implements IModule, CommandExecutor
{
	private FMC fmc;
	
	public DynGen(FMC fmc)
	{
		this.fmc = fmc;
	}

	@Override
	public void onEnable()
	{
		fmc.getCommand("dyngen").setExecutor(this);
	}

	@Override
	public void onDisable()
	{
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)
	{
		
		
		return true;
	}
}
