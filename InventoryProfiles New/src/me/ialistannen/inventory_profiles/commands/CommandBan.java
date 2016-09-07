package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Lets you ban a player
 */
class CommandBan extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandBan() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_ban",
				Util.tr("command_ban_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if(indexRelativeToYou == 0) {
			return Util.getAllProfileNames();
		}
		return Collections.emptyList();
	}
	
	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if(args.length < 3) {
			return CommandResultType.SEND_USAGE;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		Optional<Duration> banTime = Util.parseDurationString(args[1]);
		
		if(!banTime.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return CommandResultType.SUCCESSFUL;
		}
		
		String reason = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
		
		profile.ban(Util.color(reason), banTime.get());
		
		InventoryProfiles.getProfileManager().checkAndKickPlayerBanned(profile);
		
		sender.sendMessage(tr("banned player", profile.getName(), Util.formatDuration(banTime.get()), reason));
		
		return CommandResultType.SUCCESSFUL;
	}

}
