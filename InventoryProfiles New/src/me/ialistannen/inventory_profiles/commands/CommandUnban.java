package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Unbans a player
 */
public class CommandUnban extends CommandPreset {

	/**
	 * A new instance
	 */
	public CommandUnban() {
		super(Util.PERMISSION_PREFIX + ".unban", false);
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
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		if(!profile.isBanned()) {
			sender.sendMessage(tr("player not banned"));
			return true;
		}
		
		profile.unban();
		
		sender.sendMessage(tr("unbanned player", profile.getName()));
		return true;
	}

}
