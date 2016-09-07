package me.ialistannen.inventory_profiles.hooks;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.LanguageEventReceiverHook.ChangeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Manages the hooks
 */
public class HookManager {

	private MoneyHook moneyHook;
	private RegionHook regionHook;

	private final List<LanguageEventReceiverHook> eventReceiverHooks = new ArrayList<>();

	/**
	 * Registers a {@link LanguageEventReceiverHook}
	 *
	 * @param receiverHook The {@link LanguageEventReceiverHook} to register
	 */
	public void registerLanguageReceiverHook(LanguageEventReceiverHook receiverHook) {
		eventReceiverHooks.add(receiverHook);
	}

	/**
	 * Notifies all receivers about the change
	 *
	 * @param changeType The Type of the change
	 * @param language   The language after the event.
	 */
	public void callLanguageChangeEvent(ChangeType changeType, Locale language) {
		eventReceiverHooks.forEach(receiverHook -> receiverHook.onLanguageEvent(changeType, language));
	}

	/**
	 * @return The money hook. Working or not.
	 */
	public MoneyHook getMoneyHook() {
		if (moneyHook == null) {
			moneyHook = new VaultMoneyHook();
		}
		else {
			return moneyHook;
		}

		if (moneyHook.isWorking()) {
			InventoryProfiles.getInstance().getLogger().info("Hooked into " + moneyHook.getName() + " through vault");
		}
		else {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, "Error with money hook: "
					+ moneyHook.getErrorMessage());
		}

		return moneyHook;
	}

	/**
	 * @return The {@link RegionHook}. Working or not.
	 */
	public RegionHook getRegionHook() {
		if (regionHook == null) {
			regionHook = new WorldGuardHook();
		}
		else {
			return regionHook;
		}

		if (regionHook.isWorking()) {
			InventoryProfiles.getInstance().getLogger().info("Hooked into " + regionHook.getName() + ".");
		}
		else {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, "Error with region hook: "
					+ regionHook.getErrorMessage());
		}

		return regionHook;
	}
}
