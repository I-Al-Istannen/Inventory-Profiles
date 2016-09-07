package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.LanguageEventReceiverHook.ChangeType;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Sets the currently used language
 */
class CommandLanguageSet extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandLanguageSet() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_language_set",
				Util.tr("command_language_set_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if (args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}

		Locale locale = Locale.forLanguageTag(args[0]);

		Locale old = InventoryProfiles.getInstance().getCurrentLocale();
		InventoryProfiles.getInstance().getLanguage().setLanguage(locale);
		sender.sendMessage(Util.tr("language set",
				InventoryProfiles.getInstance().getCurrentLocale().getDisplayName()));


		// reload the commands to reinitialize the keywords (if needed)
		if (!old.equals(InventoryProfiles.getInstance().getCurrentLocale())) {
			InventoryProfiles.getInstance().reloadCommands();
			InventoryProfiles.getInstance().getHookManager()
					.callLanguageChangeEvent(
							ChangeType.LOCALE_SET, InventoryProfiles.getInstance().getCurrentLocale()
					);
		}

		return CommandResultType.SUCCESSFUL;
	}

}
