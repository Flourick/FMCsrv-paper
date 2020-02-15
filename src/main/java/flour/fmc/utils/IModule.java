package flour.fmc.utils;

/**
 * Interface that all modules should implement
 * 
 * @author Flourick
 */
public interface IModule
{
	/**
	 * States if the module is enabled or disabled
	 * 
	 * @return true if module is enabled, false otherwise
	 */
	public boolean isEnabled();
	
	/**
	 * Gets called when the server starts/reloads or the plugin gets enabled
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean onEnable();
	
	/**
	 * Gets called whenever server stops/restarts/reloads or the plugin gets disabled
	 */
	public void onDisable();
}
