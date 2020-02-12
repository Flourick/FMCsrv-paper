package flour.fmc.utils;

/**
 * Interface that all modules should implement
 * 
 * @author Flourick
 */
public interface IModule
{
	/**
	* Gets called whenever server stops/restarts/reloads or the plugin gets disabled
	*/
	public void onDisable();
}
