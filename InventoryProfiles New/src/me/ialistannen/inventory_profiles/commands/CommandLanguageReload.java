package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.languageSystem.I18N;
import me.ialistannen.languageSystem.MessageProvider;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Reloads the Language files
 */
public class CommandLanguageReload extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandLanguageReload() {
		super(tr("subCommandLanguageReload name"), tr("subCommandLanguageReload keyword"),
				Pattern.compile(tr("subCommandLanguageReload pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".languageReload");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandLanguageReload usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandLanguageReload description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		Locale old = InventoryProfiles.getInstance().getCurrentLocale();
		
		MessageProvider provider = InventoryProfiles.getInstance().getLanguage();
		if(provider instanceof I18N) {
			((I18N) provider).reload();
		}
		
		// reload the commands to reinitialize the keywords (if needed)
		if(!old.equals(InventoryProfiles.getInstance().getCurrentLocale())) {
			InventoryProfiles.getInstance().reloadCommands();
		}
		
		sender.sendMessage(tr("reloaded language files"));
		return true;
	}

}
