package me.ialistannen.inventory_profiles.hooks;

import java.util.Locale;

import org.bukkit.Bukkit;

import me.ialistannen.ip_sign_shop.util.Language;

/**
 * A Hook to the IPSignShop plugin
 */
public class IPSignShopHook {

	/**
	 * @param locale The new Locale for IPSignShop
	 */
	public static void updateLanguage(Locale locale) {
		if(Bukkit.getPluginManager().getPlugin("IPSignShop") != null) {
			try {
				Class.forName("me.ialistannen.ip_sign_shop.util.Language");
				// should give an error if not installed
				Language.setLocale(locale);
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
