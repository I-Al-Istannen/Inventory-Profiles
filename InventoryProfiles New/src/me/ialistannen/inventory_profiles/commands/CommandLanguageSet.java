package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Sets the currently used language
 */
public class CommandLanguageSet extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandLanguageSet() {
		super(tr("subCommandLanguageSet name"), tr("subCommandLanguageSet keyword"),
				Pattern.compile(tr("subCommandLanguageSet pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".languageSet");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandLanguageSet usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandLanguageSet description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 1) {
			return false;
		}
		
		Locale locale = Locale.forLanguageTag(args[0]);
		
		Locale old = InventoryProfiles.getInstance().getCurrentLocale();
		InventoryProfiles.getInstance().getLanguage().setLanguage(locale);
		sender.sendMessage(Util.tr("language set", InventoryProfiles.getInstance().getCurrentLocale().getDisplayName()));
		

		// reload the commands to reinitialize the keywords (if needed)
		if(!old.equals(InventoryProfiles.getInstance().getLanguage())) {
			InventoryProfiles.getInstance().reloadCommands();
		}
		
		return true;
	}

}
