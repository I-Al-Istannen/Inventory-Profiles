package me.ialistannen.inventory_profiles.hooks;

import org.bukkit.entity.Player;

/**
 * The money hook
 */
public interface MoneyHook extends Hook {

	/**
	 * @param player The player to set the money for
	 * @param amount The amount to set it to
	 */
	public void setMoney(Player player, double amount);
	
	/**
	 * @param player The player to add the money to
	 * @param amount The amount to add
	 */
	public void addMoney(Player player, double amount);
	
	/**
	 * @param player The player to add remove money from
	 * @param amount The amount to remove
	 */
	public void removeMoney(Player player, double amount);
	
	/**
	 * Returns the current balance of the player
	 * 
	 * @param player The player to check the balance for
	 * @return The Balance of the player
	 */
	public double getBalance(Player player);
}
