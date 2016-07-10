package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Deletes a profile
 */
public class CommandDelete extends CommandPreset {

	/**
	 * Deletes a profile
	 */
	public CommandDelete() {
		super(Util.PERMISSION_PREFIX + ".delete", false);
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
		if(args.length < 1) {
			return false;
		}
		
		Optional<Profile> profileOpt = InventoryProfiles.getProfileManager().getProfile(args[0]);
		
		if(!profileOpt.isPresent()) {
			sender.sendMessage(tr("username unknown"));
			return true;
		}
		
		InventoryProfiles.getProfileManager().removeProfile(profileOpt.get());
		
		sender.sendMessage(tr("deleted profile", profileOpt.get().getName()));		
		return true;
	}

}
