package flour.fmc.protection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import flour.fmc.FMC;
import flour.fmc.utils.IModule;
import flour.fmc.utils.LogFormatter;

/**
 * Module containing various protection features (logging...)
 * 
 * @author Flourick
 */
public class Protection implements IModule {
	private boolean isEnabled = false;
	private final FMC fmc;

	private final Logger protectionLog;

	public Protection(FMC fmc) {
		this.fmc = fmc;
		protectionLog = Logger.getLogger("Protection");
	}

	@Override
	public boolean onEnable()
	{
		if(!setupLogs()) {
			return isEnabled = false;
		}

		// logs pet deaths becouse for some reason vanilla does not do that
		fmc.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onEntityDeath(EntityDeathEvent event)
			{
				if(event.getEntity() instanceof Tameable) {
					Tameable entity = (Tameable) event.getEntity();

					if(entity.isTamed()) {
						String name = entity.getCustomName() == null ? entity.getName() : entity.getCustomName();
						String killer = entity.getKiller() == null ? "unknown" : entity.getKiller().getName();
						String owner = entity.getOwner().getName() == null ? "unknown" : entity.getOwner().getName();

						protectionLog.log(Level.INFO, name + " owned by " + owner + " was killed by " + killer + " via " + entity.getLastDamageCause().getCause() + "!");
					}
				}
			}
		}, fmc);

		return isEnabled = true;
	}

	@Override
	public void onDisable()
	{
		isEnabled = false;
	}
	
	@Override
	public boolean isEnabled()
	{
		return isEnabled;
	}

	@Override
	public String getName()
	{
		return "Protection";
	}

	private boolean setupLogs()
	{
		try {
			protectionLog.setUseParentHandlers(false);

			String folder = fmc.getDataFolder() + "\\" + "logs";
			if(!Files.isDirectory(Paths.get(folder))) {
				Files.createDirectories(Paths.get(folder));
			}

			FileHandler fh = new FileHandler(folder + "\\" + getLogName());
			fh.setFormatter(new LogFormatter());

			protectionLog.addHandler(fh);
		}
		catch (SecurityException | IOException e) {
			return false;
		}

		return true;
	}

	private String getLogName()
	{
		return LocalDate.now() + "-protection.log";
	}
}
