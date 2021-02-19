package me.flourick.fmc.utils;

/**
 * Interface that all modules have to implement.
 * 
 * @author Flourick
 */
public interface IModule
{
	/**
	 * States if the module is enabled or disabled.
	 * 
	 * @return {@code true} if module is enabled, {@code false} otherwise
	 */
	public boolean isEnabled();
	
	/**
	 * Gets called when the server starts/reloads or the plugin gets enabled.
	 * 
	 * @return {@code true} if successful, {@code false} otherwise
	 */
	public boolean onEnable();
	
	/**
	 * Gets called whenever server stops/restarts/reloads or the plugin gets disabled.
	 */
	public void onDisable();
	
	/**
	 * @return name of the module
	 */
	public String getName();
}
