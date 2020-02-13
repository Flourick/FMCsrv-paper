package flour.fmc.utils;

/**
 * Interface that all modules should implement
 * 
 * @author Flourick
 */
public interface IModule
{
	/**
	* Gets called when the server starts/reloads or the plugin gets enabled
	*/
	public void onEnable();
	
	/**
	* Gets called whenever server stops/restarts/reloads or the plugin gets disabled
	*/
	public void onDisable();
}
