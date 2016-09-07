package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.LanguageEventReceiverHook.ChangeType;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.languageSystem.I18N;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Reloads the Language files
 */
class CommandLanguageReload extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandLanguageReload() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_language_reload",
				Util.tr("command_language_reload_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		MessageProvider provider = InventoryProfiles.getInstance().getLanguage();
		if (provider instanceof I18N) {
			((I18N) provider).reload();
		}

		InventoryProfiles.getInstance().reloadCommands();
		InventoryProfiles.getInstance().getHookManager()
				.callLanguageChangeEvent(
						ChangeType.RELOADED, InventoryProfiles.getInstance().getCurrentLocale()
				);

		sender.sendMessage(tr("reloaded language files"));
		return CommandResultType.SUCCESSFUL;
	}

}
