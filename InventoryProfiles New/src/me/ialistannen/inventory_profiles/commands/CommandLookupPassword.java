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
 * Looks up the password of a player
 */
class CommandLookupPassword extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandLookupPassword() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_lookup_password",
				Util.tr("command_lookup_password_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		if(indexRelativeToYou == 0) {
			return getAllProfileNames();
		}
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if(args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		sender.sendMessage(tr("password lookuped", optProf.get().getName(), optProf.get().getPassword()));
		return CommandResultType.SUCCESSFUL;
	}

}
