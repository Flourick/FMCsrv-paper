package flour.fmc.utils;

import flour.fmc.FMC;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Custom config class for additional configuration files
 * 
 * @author Flourick
 */
public class CConfig
{
	private FMC fmc;
	
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	private final String configName;
	
	public CConfig(FMC fmc, String configName)
	{
		this.fmc = fmc;
		this.configName = configName;
	}
	
	public FileConfiguration getConfig()
	{
		if(customConfig == null) {
			reloadConfig();
		}
		
		return customConfig;
	}
	
	public void reloadConfig()
	{
		if(customConfigFile == null) {
			customConfigFile = new File(fmc.getDataFolder(), configName);
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

		// Look for defaults in the jar
		Reader defConfigStream = new InputStreamReader(fmc.getResource(configName), StandardCharsets.UTF_8);
		YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
		customConfig.setDefaults(defConfig);
	}
	
	public void saveConfig()
	{
		if(customConfig == null || customConfigFile == null) {
			return;
		}
		try {
			getConfig().save(customConfigFile);
		}
		catch (IOException ex) {
			fmc.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}
	
	public void saveDefaultConfig()
	{
		if(customConfigFile == null) {
			customConfigFile = new File(fmc.getDataFolder(), configName);
		}
		if(!customConfigFile.exists()) {            
			 fmc.saveResource(configName, false);
		 }
	}
}
