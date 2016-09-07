package me.ialistannen.inventory_profiles.hooks;

/**
 * The general layout of a hook
 */
interface Hook {

	/**
	 * @return True if the hook is working
	 */
	boolean isWorking();

	/**
	 * @return The name of the Economy method
	 */
	String getName();

	/**
	 * Only retrieved when isWorking() is false
	 *
	 * @return The error message.
	 */
	String getErrorMessage();
}