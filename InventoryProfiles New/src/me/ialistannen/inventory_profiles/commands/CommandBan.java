package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Lets you ban a player
 */
public class CommandBan extends CommandPreset {

	/**
	 * A new {@link CommandBan} instance
	 */
	public CommandBan() {
		super(Util.PERMISSION_PREFIX + ".ban", false);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		List<String> toReturn = new ArrayList<>();
		if(position == 0) {
			toReturn.addAll(getAllProfileNames());
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
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
