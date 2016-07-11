package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Lists all the regions a player owns
 */
public class CommandListRegions extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandListRegions() {
		super(tr("subCommandListRegions name"), tr("subCommandListRegions keyword"),
				Pattern.compile(tr("subCommandListRegions pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandListRegions usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandListRegions description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		if(wholeUserChat.size() == 2) {
			return Arrays.stream(RegionRole.values()).map(role -> role.getTranslatedName()).collect(Collectors.toList());
		}
		if(wholeUserChat.size() == 3) {
			return Util.getAllProfileNames();
		}
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 1) {
			return false;
		}
		
		if(!InventoryProfiles.hasRegionHook()) {
			sender.sendMessage(tr("no region hook found"));
			return true;
		}

		Optional<RegionRole> roleOpt = RegionRole.forTranslatedName(args[0]);
		if (!roleOpt.isPresent()) {
			sender.sendMessage(tr("role not valid", args[0]));
			return true;
		}
		RegionRole role = roleOpt.get();

		if((sender instanceof Player) && (!sender.hasPermission(Util.PERMISSION_PREFIX + ".listRegions") || args.length == 1)) {
			Player player = (Player) sender;
			if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
				player.sendMessage(tr("not logged in"));
				return true;
			}
			
			Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
			
			Collection<String> regions = InventoryProfiles.getRegionHook().getAllRegions(profile, role);
			
			player.sendMessage(tr("list regions", role.getTranslatedName(), regions.stream().collect(Collectors.joining(", ")), regions.size()));
			return true;
		}
		
		if(args.length < 2) {
			return false;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[1]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[1]));
			return true;
		}

		Profile profile = optProf.get();
		
		Collection<String> regions = profile.getRegionObjects().stream().map(reg -> reg.getRegionID()).collect(Collectors.toList());
		
		sender.sendMessage(tr("list regions other", profile.getName(), role.getTranslatedName(), regions.stream().collect(Collectors.joining(", ")), regions.size()));
		
		return true;
	}

}
