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
	
	/**
	* The one and only constructor of the CConfig class
	* 
	* @param  fmc         FMC plugin instance
	* @param  configName  name of the config file including .yml (ex. 'chatter.yml')
	*/
	public CConfig(FMC fmc, String configName)
	{
		this.fmc = fmc;
		this.configName = configName;
	}
	
	/**
	* Gets the actual config instance or reloads one from .jar if not present
	* 
	* @return FileConfiguration associated with this CConfig instance
	*/
	public FileConfiguration getConfig()
	{
		if(customConfig == null) {
			reloadConfig();
		}
		
		return customConfig;
	}
	
	/**
	* Reloads the config from jar or creates one if not present
	*/
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
	
	/**
	* Saves all changes from memory to disk file
	*/
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
	
	/**
	* Creates the .yml file associated with this CConfig instance in plugins data folder, does not overwrite if already present
	*/
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
