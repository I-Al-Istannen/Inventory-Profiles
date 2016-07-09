package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.language.IPLanguage;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Reloads the Language files
 */
public class CommandLanguageReload extends CommandPreset {

	/**
	 * Reloads the language files
	 */
	public CommandLanguageReload() {
		super(Util.PERMISSION_PREFIX + ".languageReload", false);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		IPLanguage.setLocale(IPLanguage.getLocale());
		
		sender.sendMessage(tr("reloaded language files"));
		return true;
	}

}
