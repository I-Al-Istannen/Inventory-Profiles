package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Sets or shows the playtime for a user
 */
public class CommandSetPlaytime extends CommandPreset {

	/**
	 * ... Javadoc warnings NOT needed in this case.
	 */
	public CommandSetPlaytime() {
		super(Util.PERMISSION_PREFIX + ".setPlaytime", false);
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
		if(args.length < 2) {
			return false;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		Profile profile = optProf.get();
		
		Optional<Duration> durationOpt = Util.parseDurationString(args[1]);
		
		if(!durationOpt.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return true;
		}
		
		profile.setAvaillablePlaytime(durationOpt.get());
		sender.sendMessage(tr("set playtime", profile.getName(), Util.formatDuration(profile.getPlaytimeLeft())));
		return true;
	}

}
