package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Let's a user set his password or the password of someone else
 */
class CommandSetPassword extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandSetPassword() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_set_password",
				Util.tr("command_set_password_permission"), sender -> true);
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

		if (sender instanceof Player
				&& (!sender.hasPermission(Util.PERMISSION_PREFIX + ".setPassword") || args.length == 1)) {
			Player player = (Player) sender;
			Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());

			if (!profile.isPresent()) {
				sender.sendMessage(tr("not logged in"));
				return CommandResultType.SUCCESSFUL;
			}

			String oldPassword = profile.get().getPassword();
			profile.get().setPassword(args[0]);

			sender.sendMessage(tr("replaced own password", oldPassword, profile.get().getPassword()));
			return CommandResultType.SUCCESSFUL;
		}

		if (args.length < 2) {
			return CommandResultType.SEND_USAGE;
		}

		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = optProf.get();

		String oldPassword = profile.getPassword();

		profile.setPassword(args[1]);

		sender.sendMessage(tr("replaced others password", profile.getName(), oldPassword, profile.getPassword()));
		return CommandResultType.SUCCESSFUL;
	}

}
