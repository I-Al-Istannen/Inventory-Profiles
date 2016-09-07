package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Allows the setting of a playtime modifier
 */
class CommandSetPlaytimeModifier extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandSetPlaytimeModifier() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_set_playtime_modifier",
				Util.tr("command_set_playtime_modifier_permission"), sender -> true);
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
		if (args.length < 3) {
			return CommandResultType.SEND_USAGE;
		}

		if (!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();

		Optional<Duration> duration = Util.parseDurationString(args[1]);

		if (!duration.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return CommandResultType.SUCCESSFUL;
		}

		Optional<Duration> timeValid = Util.parseDurationString(args[2]);

		if (!timeValid.isPresent()) {
			sender.sendMessage(tr("time not valid", args[2]));
			return CommandResultType.SUCCESSFUL;
		}

		if (timeValid.get().toDays() < 1) {
			sender.sendMessage(tr("time too small", timeValid.get().toDays()));
			return CommandResultType.SUCCESSFUL;
		}

		profile.setPlaytimeModifier(duration.get().toMillis(), LocalDate.now().plusDays(timeValid.get().toDays() - 1));

		sender.sendMessage(
				tr("set playtime modifier",
						profile.getName(), Util.formatDuration(duration.get()), timeValid.get().toDays()
				)
		);
		return CommandResultType.SUCCESSFUL;
	}

}
