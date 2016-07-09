package me.ialistannen.inventory_profiles.hooks;

/**
 * The general layout of a hook
 */
public interface Hook {
	
	/**
	 * @return True if the hook is working
	 */
	public boolean isWorking();
	
	/**
	 * @return The name of the Economy method
	 */
	public String getName();
	
	/**
	 *  Only retrieved when isWorking() is false
	 * 
	 * @return The error message.
	 */
	public String getErrorMessage();
}