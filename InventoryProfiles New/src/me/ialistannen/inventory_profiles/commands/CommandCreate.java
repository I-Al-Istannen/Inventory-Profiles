package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * let's you create a profile
 */
public class CommandCreate extends CommandPreset {

	/**
	 * Create command
	 */
	public CommandCreate() {
		super(Util.PERMISSION_PREFIX + ".create", false);
	}

	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		List<String> toReturn = new ArrayList<>();
		if(position == 2) {
			toReturn.add("true");
			toReturn.add("false");
		}
		if(position == 3) {
			Bukkit.getWorlds().stream().map(world -> world.getName()).forEach(toReturn::add);
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length < 4) {
			return false;
		}
		
		String name = args[0];
		String password = args[1];
		
		Optional<Boolean> isOp = Util.getBoolean(args[2]);
		
		if(!isOp.isPresent()) {
			sender.sendMessage(tr("boolean not valid", args[2]));
			return true;
		}
		
		String worldName = args[3];
		
		if(Bukkit.getWorld(worldName) == null) {
			sender.sendMessage(tr("world not valid", worldName));
			return true;
		}
		
		Profile profile = new Profile(name, password, isOp.get(), Bukkit.getWorld(worldName));
		
		InventoryProfiles.getProfileManager().addProfile(profile);
		
		sender.sendMessage(tr("created user", profile.getName()));
		
		return true;
	}
	
}
