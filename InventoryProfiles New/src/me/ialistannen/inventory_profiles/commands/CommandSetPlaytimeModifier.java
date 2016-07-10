package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Allows the setting of a playtime modifier
 */
public class CommandSetPlaytimeModifier extends CommandPreset {

	/**
	 * Nothing to see here
	 */
	public CommandSetPlaytimeModifier() {
		super(Util.PERMISSION_PREFIX + ".setPlaytimeModifier", false);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		if(position == 0) {
			return getAllProfileNames();
		}
		return Collections.emptyList();
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
		
		Optional<Duration> duration = Util.parseDurationString(args[1]);
		
		if(!duration.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return true;
		}
		
		Optional<Duration> timeValid = Util.parseDurationString(args[2]);
		
		if(!timeValid.isPresent()) {
			sender.sendMessage(tr("time not valid", args[2]));
			return true;
		}
		
		if(timeValid.get().toDays() < 1) {
			sender.sendMessage(tr("time too small", timeValid.get().toDays()));
			return true;
		}
		
		profile.setPlaytimeModifier(duration.get().toMillis(), LocalDate.now().plusDays(timeValid.get().toDays() - 1));
		
		sender.sendMessage(tr("set playtime modifier", profile.getName(), Util.formatDuration(duration.get()), timeValid.get().toDays()));
		return true;
	}

}
