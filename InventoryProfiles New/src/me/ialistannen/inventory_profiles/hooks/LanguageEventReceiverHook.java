package me.ialistannen.inventory_profiles.hooks;

import java.util.Locale;

/**
 * Allows other plugins to get notified if the language gets reloaded or newly set.
 */
public interface LanguageEventReceiverHook {

	/**
	 * Called when the language was changed or another {@link ChangeType} was met.
	 *
	 * @param language The language after the event
	 */
	void onLanguageEvent(ChangeType changeType, Locale language);

	/**
	 * The type of the change
	 */
	enum ChangeType {
		RELOADED,
		LOCALE_SET
	}
}
