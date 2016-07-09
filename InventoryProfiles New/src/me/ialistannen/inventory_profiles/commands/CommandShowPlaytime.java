package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Allows the user to see his playtime / the playtime of another user
 */
public class CommandShowPlaytime extends CommandPreset {

	/**
	 * A new instance of the command
	 */
	public CommandShowPlaytime() {
		super("", false);
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
		
		// show own playtime
		if(sender instanceof Player && !sender.hasPermission(Util.PERMISSION_PREFIX + ".showPlaytime")) {
			Player player = (Player) sender;
			Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());
			
			if(!profile.isPresent()) {
				sender.sendMessage(tr("not logged in"));
				return true;
			}
			
			sender.sendMessage(tr("show own playtime",
					Util.formatDuration(profile.get().getPlaytimeLeft()),
					Util.formatDuration(Duration.ofMillis(profile.get().getPlaytimeModifier()))));
			return true;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		Profile profile = optProf.get();
		
		sender.sendMessage(
				tr("show playtime for other user", 
						profile.getName(), 
						Util.formatDuration(profile.getPlaytimeLeft()),
						Util.formatDuration(Duration.ofMillis(profile.getPlaytimeModifier()))));
		
		return true;
	}

}
