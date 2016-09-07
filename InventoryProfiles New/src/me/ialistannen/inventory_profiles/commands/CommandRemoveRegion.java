package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Removes the rights from an user
 */
class CommandRemoveRegion extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandRemoveRegion() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_remove_region",
				Util.tr("command_remove_region_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		if (indexRelativeToYou == 0) {
			return getAllProfileNames();
		}
		else if (indexRelativeToYou == 1) {
			Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}


	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if (args.length < 3) {
			return CommandResultType.SEND_USAGE;
		}

		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = optProf.get();

		World world = Bukkit.getWorld(args[1]);

		if (world == null) {
			sender.sendMessage(tr("world not valid", args[1]));
			return CommandResultType.SUCCESSFUL;
		}

		String region = args[2];

		if (!InventoryProfiles.hasRegionHook()) {
			sender.sendMessage(tr("no region hook found"));
			return CommandResultType.SUCCESSFUL;
		}

		if (InventoryProfiles.getRegionHook().hasNoRegion(region, world)) {
			sender.sendMessage(tr("region not valid", region));
			return CommandResultType.SUCCESSFUL;
		}

		Optional<RegionObject> regionObj = profile.getRegionObjects().stream().filter(regionObject ->
				regionObject.getWorld().getUID().equals(world.getUID())
						&& regionObject.getRegionID().equalsIgnoreCase(region)
		).findFirst();

		regionObj.ifPresent(profile::removeRegionObject);

		sender.sendMessage(tr("removed region", profile.getName(), region, world.getName()));

		return CommandResultType.SUCCESSFUL;
	}

}
