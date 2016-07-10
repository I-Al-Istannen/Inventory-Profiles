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
 * Looks up the password of a player
 */
public class CommandLookupPassword extends CommandPreset {

	/**
	 * A new instance shall rise
	 */
	public CommandLookupPassword() {
		super(Util.PERMISSION_PREFIX + ".lookupPassword", false);
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
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		sender.sendMessage(tr("password lookuped", optProf.get().getName(), optProf.get().getPassword()));
		return true;
	}

}
