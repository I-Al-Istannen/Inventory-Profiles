package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.languageSystem.I18N;
import me.ialistannen.languageSystem.MessageProvider;

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
		MessageProvider provider = InventoryProfiles.getInstance().getLanguage();
		if(provider instanceof I18N) {
			((I18N) provider).reload();
		}
		
		sender.sendMessage(tr("reloaded language files"));
		return true;
	}

}
