package me.ialistannen.inventory_profiles.hooks;

import java.util.logging.Level;

import me.ialistannen.inventory_profiles.InventoryProfiles;

/**
 * Manages the hooks
 */
public class HookManager {

	/**
	 * @return The money hook. Working or not.
	 */
	public static MoneyHook getMoneyHook() {
		VaultMoneyHook vault = new VaultMoneyHook();
		
		if(vault.isWorking()) {
			InventoryProfiles.getInstance().getLogger().info("Hooked into " + vault.getName() + " through vault");
		}
		else {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, "Error with money hook: " + vault.getErrorMessage());
		}
		
		return vault;
	}
	
	/**
	 * @return The {@link RegionHook}. Working or not.
	 */
	public static RegionHook getRegionHook() {
		WorldGuardHook worldGuard = new WorldGuardHook();
		
		if(worldGuard.isWorking()) {
			InventoryProfiles.getInstance().getLogger().info("Hooked into " + worldGuard.getName() + ".");
		}
		else {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, "Error with money hook: " + worldGuard.getErrorMessage());
		}
		
		return worldGuard;
	}
}
