package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Adds a user to a region
 */
public class CommandAddRegion extends CommandNode {

	/**
	 * New instance
	 */
	public CommandAddRegion() {
		super(tr("subCommandAddRegion name"), tr("subCommandAddRegion keyword"),
				Pattern.compile(tr("subCommandAddRegion pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".addRegion");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandAddRegion usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandAddRegion description", getName());
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
		else if(wholeUserChat.size() == 4) {
			Arrays.stream(RegionRole.values()).map(role -> Util.getNiceNameForConstant(role.name())).forEach(toReturn::add);
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 4) {
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
		
		Optional<RegionRole> roleOpt = RegionRole.forTranslatedName(args[3]);
		if (!roleOpt.isPresent()) {
			sender.sendMessage(tr("role not valid", args[3]));
			return true;
		}
		RegionRole role = roleOpt.get();		
				
		profile.addRegionObject(new RegionObject(region, world, role));
		sender.sendMessage(tr("added region", profile.getName(), Util.getNiceNameForConstant(role.name()), region, world.getName()));
		return true;
	}
}
