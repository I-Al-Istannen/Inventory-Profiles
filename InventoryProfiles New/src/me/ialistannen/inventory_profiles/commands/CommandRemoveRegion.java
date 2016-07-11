package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Removes the rights from an user
 */
public class CommandRemoveRegion extends CommandNode {

	
	/**
	 * New instance
	 */
	public CommandRemoveRegion() {
		super(tr("subCommandRemoveRegion name"), tr("subCommandRemoveRegion keyword"),
				Pattern.compile(tr("subCommandRemoveRegion pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".removeRegion");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandRemoveRegion usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandRemoveRegion description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		List<String> toReturn = new ArrayList<>();
		
		if(wholeUserChat.size() == 2) {
			toReturn.addAll(Util.getAllProfileNames());
		}
		else if(wholeUserChat.size() == 3) {
			Bukkit.getWorlds().stream().map(world -> world.getName()).forEach(toReturn::add);
		}
		return toReturn;
	}

	
	@Override
	public boolean execute(CommandSender sender, String... args) {
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
