package me.ialistannen.inventory_profiles.commands;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.language.IPLanguage;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Sets the currently used language
 */
public class CommandLanguageSet extends CommandPreset {

	/**
	 * A new instance
	 */
	public CommandLanguageSet() {
		super(Util.PERMISSION_PREFIX + ".languageSet", false);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length < 1) {
			return false;
		}
		
		Locale locale = Locale.forLanguageTag(args[0]);
		
		IPLanguage.setLocale(locale);
		sender.sendMessage(IPLanguage.tr("language set", IPLanguage.getLocale().getDisplayName()));
		
		return true;
	}

}
