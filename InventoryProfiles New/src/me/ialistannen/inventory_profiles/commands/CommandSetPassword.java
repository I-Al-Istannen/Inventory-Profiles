package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Let's a user set his password or the password of someone else
 */
public class CommandSetPassword extends CommandPreset {

	/**
	 * Yea, yea
	 */
	public CommandSetPassword() {
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

		if(args.length < 1) {
			return false;
		}
		
		if(sender instanceof Player && (!sender.hasPermission(Util.PERMISSION_PREFIX + ".setPassword") || args.length == 1)) {
			Player player = (Player) sender;
			Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());
			
			if(!profile.isPresent()) {
				sender.sendMessage(tr("not logged in"));
				return true;
			}
			
			String oldPassword = profile.get().getPassword();
			profile.get().setPassword(args[0]);
			
			sender.sendMessage(tr("replaced own password", oldPassword, profile.get().getPassword()));
			return true;
		}
		
		if(args.length < 2) {
			return false;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if(!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Profile profile = optProf.get();
		
		String oldPassword = profile.getPassword();
		
		profile.setPassword(args[1]);
		
		sender.sendMessage(tr("replaced others password", profile.getName(), oldPassword, profile.getPassword()));
		return true;
	}

}
