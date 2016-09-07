package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Deletes a profile
 */
class CommandDelete extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandDelete() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_delete",
				Util.tr("command_delete_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if (indexRelativeToYou == 0) {
			return getAllProfileNames();
		}
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if (args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}

		Optional<Profile> profileOpt = InventoryProfiles.getProfileManager().getProfile(args[0]);

		if (!profileOpt.isPresent()) {
			sender.sendMessage(tr("username unknown"));
			return CommandResultType.SUCCESSFUL;
		}

		InventoryProfiles.getProfileManager().removeProfile(profileOpt.get());

		sender.sendMessage(tr("deleted profile", profileOpt.get().getName()));
		return CommandResultType.SUCCESSFUL;
	}

}
