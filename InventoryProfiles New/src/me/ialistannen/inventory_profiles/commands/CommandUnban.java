package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Unbans a player
 */
class CommandUnban extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandUnban() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_unban",
				Util.tr("command_unban_permission"), sender -> true);
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

		if (!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		@SuppressWarnings("OptionalGetWithoutIsPresent") // see hasProfile check above
				Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();

		if (!profile.isBanned()) {
			sender.sendMessage(tr("player not banned"));
			return CommandResultType.SUCCESSFUL;
		}

		profile.unban();

		sender.sendMessage(tr("unbanned player", profile.getName()));
		return CommandResultType.SUCCESSFUL;
	}

}
