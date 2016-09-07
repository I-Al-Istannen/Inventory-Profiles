package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Adds a user to a region
 */
class CommandAddRegion extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandAddRegion() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_add_region",
				Util.tr("command_add_region_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if (indexRelativeToYou == 0) {
			return Util.getAllProfileNames();
		}
		else if (indexRelativeToYou == 1) {
			return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
		}
		else if (indexRelativeToYou == 2) {
			Arrays
					.stream(RegionRole.values())
					.map(role -> Util.getNiceNameForConstant(role.name()))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if (args.length < 4) {
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

		Optional<RegionRole> roleOpt = RegionRole.forTranslatedName(args[3]);
		if (!roleOpt.isPresent()) {
			sender.sendMessage(tr("role not valid", args[3]));
			return CommandResultType.SUCCESSFUL;
		}
		RegionRole role = roleOpt.get();

		profile.addRegionObject(new RegionObject(region, world, role));
		sender.sendMessage(tr("added region", profile.getName(), Util.getNiceNameForConstant(role.name()), region,
				world.getName()));
		return CommandResultType.SUCCESSFUL;
	}
}
