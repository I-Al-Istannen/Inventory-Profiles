package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Removes the rights from an user
 */
public class CommandRemoveRegion extends CommandPreset {

	/**
	 * Constructor
	 */
	public CommandRemoveRegion() {
		super(Util.PERMISSION_PREFIX + ".removeRegion", false);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		List<String> toReturn = new ArrayList<>();
		
		if(position == 0) {
			toReturn.addAll(getAllProfileNames());
		}
		else if(position == 1) {
			Bukkit.getWorlds().stream().map(world -> world.getName()).forEach(toReturn::add);
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length < 3) {
			return false;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		Profile profile = optProf.get();
		
		World world = Bukkit.getWorld(args[1]);

		if(world == null) {
			sender.sendMessage(tr("world not valid", args[1]));
			return true;
		}
		
		String region = args[2];
		
		if(!InventoryProfiles.hasRegionHook()) {
			sender.sendMessage(tr("no region hook found"));
			return true;
		}
		
		if(!InventoryProfiles.getRegionHook().hasRegion(region, world)) {
			sender.sendMessage(tr("region not valid", region));
			return true;
		}
		
		Optional<RegionObject> regionObj = profile.getRegionObjects().stream().filter(regionObject -> {
			return regionObject.getWorld().getUID().equals(world.getUID())
					&& regionObject.getRegionID().equalsIgnoreCase(region);
		}).findFirst(); 
		
		regionObj.ifPresent(obj -> {
			profile.removeRegionObject(obj);
		});
		
		sender.sendMessage(tr("removed region", profile.getName(), region, world.getName()));
		
		return true;
	}

}
