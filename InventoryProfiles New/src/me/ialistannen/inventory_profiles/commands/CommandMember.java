package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Adds a User as a Member of a Region
 */
public class CommandMember extends PlayerCommandNode {
	
	/**
	 * New instance
	 */
	public CommandMember() {
		super(tr("subCommandMember name"), tr("subCommandMember keyword"),
				Pattern.compile(tr("subCommandMember pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandMember usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandMember description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		List<String> list = new ArrayList<>();
		if(wholeUserChat.size() == 2) {
			list.addAll(Util.getAllProfileNames());
		}
		else if(wholeUserChat.size() == 3) {	// keywords for adding/removing
			list.add(tr("member remove pattern").contains("|") ? tr("member remove pattern").split("\\|")[0] : tr("member remove pattern"));
			list.add(tr("member add pattern").contains("|") ? tr("member add pattern").split("\\|")[0] : tr("member add pattern"));
		}
		else if(wholeUserChat.size() == 4) {	// world
			list.addAll(Bukkit.getWorlds().stream().map(world -> world.getName()).collect(Collectors.toList()));
		}
		
		return list;
	}

	@Override
	public boolean execute(Player player, String... args) {
		// target, <add|remove>, world, region name
		if(args.length < 4) {
			return false;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return true;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			player.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		
		Profile targetProfile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		Profile playerProfile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
		
		World world = Bukkit.getWorld(args[2]);

		if(world == null) {
			player.sendMessage(tr("world not valid", args[2]));
			return true;
		}
		
		String region = args[3];
		
		if(!InventoryProfiles.hasRegionHook()) {
			player.sendMessage(tr("no region hook found"));
			return true;
		}
		
		if(!InventoryProfiles.getRegionHook().hasRegion(region, world)) {
			player.sendMessage(tr("region not valid", region));
			return true;
		}

		
		Optional<RegionObject> regionObjOpt = playerProfile.getRegionObjects().stream().filter(regionObject -> {
			return regionObject.getWorld().getUID().equals(world.getUID())
					&& regionObject.getRegionID().equalsIgnoreCase(region);
		}).findFirst();
		
		if(!regionObjOpt.isPresent() || regionObjOpt.get().getRole() != RegionRole.OWNER) {
			player.sendMessage(tr("not your region"));
			return true;
		}
		
		// remove the member
		if(args[1].toLowerCase().matches(tr("member remove pattern").toLowerCase())) {
			targetProfile.removeRegionObject(new RegionObject(region, world, RegionRole.MEMBER));
			
			player.sendMessage(tr("member successfully removed", targetProfile.getName(), world.getName(), region));
			return true;
		}
		
		// add the member
		else if(args[1].toLowerCase().matches(tr("member add pattern").toLowerCase())) {
			targetProfile.addRegionObject(new RegionObject(region, world, RegionRole.MEMBER));
			
			player.sendMessage(tr("member successfully added", targetProfile.getName(), world.getName(), region));
			return true;
		}
		
		else {
			player.sendMessage(tr("member pattern not known", args[1]));
			return true;
		}
	}

}
