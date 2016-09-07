package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Allows the user to see his playtime / the playtime of another user
 */
class CommandShowPlaytime extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandShowPlaytime() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_show_playtime",
				Util.tr("command_show_playtime_permission"), sender -> true);
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

		// show own playtime
		if (sender instanceof Player && !sender.hasPermission(Util.PERMISSION_PREFIX + ".showPlaytime")) {
			Player player = (Player) sender;
			Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());

			if (!profile.isPresent()) {
				sender.sendMessage(tr("not logged in"));
				return CommandResultType.SUCCESSFUL;
			}

			sender.sendMessage(tr("show own playtime",
					Util.formatDuration(profile.get().getPlaytimeLeft()),
					Util.formatDuration(Duration.ofMillis(profile.get().getPlaytimeModifier()))));
			return CommandResultType.SUCCESSFUL;
		}

		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = optProf.get();

		sender.sendMessage(
				tr("show playtime for other user",
						profile.getName(),
						Util.formatDuration(profile.getPlaytimeLeft()),
						Util.formatDuration(Duration.ofMillis(profile.getPlaytimeModifier()))));

		return CommandResultType.SUCCESSFUL;
	}

}
