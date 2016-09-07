package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Sets or shows the playtime for a user
 */
class CommandSetPlaytime extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandSetPlaytime() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_set_playtime",
				Util.tr("command_set_playtime_permission"), sender -> true);
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
		if (args.length < 2) {
			return CommandResultType.SEND_USAGE;
		}

		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = optProf.get();

		Optional<Duration> durationOpt = Util.parseDurationString(args[1]);

		if (!durationOpt.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return CommandResultType.SUCCESSFUL;
		}

		profile.setAvaillablePlaytime(durationOpt.get());
		sender.sendMessage(tr("set playtime", profile.getName(), Util.formatDuration(profile.getPlaytimeLeft())));
		return CommandResultType.SUCCESSFUL;
	}

}
