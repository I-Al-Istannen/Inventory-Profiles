package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Lets you ban a player
 */
public class CommandBan extends CommandNode {

	
	/**
	 * New instance
	 */
	public CommandBan() {
		super(tr("subCommandBan name"), tr("subCommandBan keyword"),
				Pattern.compile(tr("subCommandBan pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".ban");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandBan usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandBan description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		List<String> toReturn = new ArrayList<>();
		if(wholeUserChat.size() == 2) {
			toReturn.addAll(Util.getAllProfileNames());
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 3) {
			return false;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		Optional<Duration> banTime = Util.parseDurationString(args[1]);
		
		if(!banTime.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return true;
		}
		
		String reason = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
		
		profile.ban(Util.color(reason), banTime.get());
		
		InventoryProfiles.getProfileManager().checkAndKickPlayerBanned(profile);
		
		sender.sendMessage(tr("banned player", profile.getName(), Util.formatDuration(banTime.get()), reason));
		
		return true;
	}

}
