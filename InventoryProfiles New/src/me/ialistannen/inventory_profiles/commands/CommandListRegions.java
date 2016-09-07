package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Lists all the regions a player owns
 */
class CommandListRegions extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandListRegions() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_list_regions",
				Util.tr("command_list_regions_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if(indexRelativeToYou == 0) {
			return Arrays
					.stream(RegionRole.values())
					.map(RegionRole::getTranslatedName)
					.collect(Collectors.toList());
		}
		else if(indexRelativeToYou == 1) {
			return getAllProfileNames();
		}
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if(args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}
		
		if(!InventoryProfiles.hasRegionHook()) {
			sender.sendMessage(tr("no region hook found"));
			return CommandResultType.SUCCESSFUL;
		}

		Optional<RegionRole> roleOpt = RegionRole.forTranslatedName(args[0]);
		if (!roleOpt.isPresent()) {
			sender.sendMessage(tr("role not valid", args[0]));
			return CommandResultType.SUCCESSFUL;
		}
		RegionRole role = roleOpt.get();

		if((sender instanceof Player) && (!sender.hasPermission(Util.PERMISSION_PREFIX + ".listRegions") || args.length == 1)) {
			Player player = (Player) sender;
			if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
				player.sendMessage(tr("not logged in"));
				return CommandResultType.SUCCESSFUL;
			}
			
			Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
			
			Collection<String> regions = InventoryProfiles.getRegionHook().getAllRegions(profile, role);
			
			player.sendMessage(tr("list regions", role.getTranslatedName(), regions.stream().collect(Collectors.joining(", ")), regions.size()));
			return CommandResultType.SUCCESSFUL;
		}
		
		if(args.length < 2) {
			return CommandResultType.SEND_USAGE;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[1]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[1]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = optProf.get();
		
		Collection<String> regions = profile.getRegionObjects().stream().map(RegionHook.RegionObject::getRegionID).collect(Collectors.toList());
		
		sender.sendMessage(tr("list regions other", profile.getName(), role.getTranslatedName(), regions.stream().collect(Collectors.joining(", ")), regions.size()));
		
		return CommandResultType.SUCCESSFUL;
	}

}
