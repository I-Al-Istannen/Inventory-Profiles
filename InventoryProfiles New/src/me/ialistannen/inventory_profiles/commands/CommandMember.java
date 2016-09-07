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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Adds a User as a Member of a Region
 */
class CommandMember extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandMember() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_member",
				Util.tr("command_member_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if (indexRelativeToYou == 0) {
			return getAllProfileNames();
		}
		else if (indexRelativeToYou == 1) {
			List<String> list = new ArrayList<>();
			list.add(tr("member remove pattern").contains("|")
					? tr("member remove pattern").split("\\|")[0]
					: tr("member remove pattern"));
			list.add(tr("member add pattern").contains("|")
					? tr("member add pattern").split("\\|")[0]
					: tr("member add pattern"));
			return list;
		}
		else if (indexRelativeToYou == 2) {
			return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		// target, <add|remove>, world, region name
		if (args.length < 4) {
			return CommandResultType.SEND_USAGE;
		}

		if (!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return CommandResultType.SUCCESSFUL;
		}

		if (!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			player.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}


		Profile targetProfile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		Profile playerProfile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();

		World world = Bukkit.getWorld(args[2]);

		if (world == null) {
			player.sendMessage(tr("world not valid", args[2]));
			return CommandResultType.SUCCESSFUL;
		}

		String region = args[3];

		if (!InventoryProfiles.hasRegionHook()) {
			player.sendMessage(tr("no region hook found"));
			return CommandResultType.SUCCESSFUL;
		}

		if (InventoryProfiles.getRegionHook().hasNoRegion(region, world)) {
			player.sendMessage(tr("region not valid", region));
			return CommandResultType.SUCCESSFUL;
		}


		Optional<RegionObject> regionObjOpt = playerProfile.getRegionObjects().stream()
				.filter(regionObject ->
						regionObject.getWorld().getUID().equals(world.getUID())
								&& regionObject.getRegionID().equalsIgnoreCase(region))
				.findFirst();

		if (!regionObjOpt.isPresent() || regionObjOpt.get().getRole() != RegionRole.OWNER) {
			player.sendMessage(tr("not your region"));
			return CommandResultType.SUCCESSFUL;
		}

		// remove the member
		if (args[1].toLowerCase().matches(tr("member remove pattern").toLowerCase())) {
			targetProfile.removeRegionObject(new RegionObject(region, world, RegionRole.MEMBER));

			player.sendMessage(tr("member successfully removed", targetProfile.getName(), world.getName(), region));
			return CommandResultType.SUCCESSFUL;
		}

		// add the member
		else if (args[1].toLowerCase().matches(tr("member add pattern").toLowerCase())) {
			targetProfile.addRegionObject(new RegionObject(region, world, RegionRole.MEMBER));

			player.sendMessage(tr("member successfully added", targetProfile.getName(), world.getName(), region));
			return CommandResultType.SUCCESSFUL;
		}

		else {
			player.sendMessage(tr("member pattern not known", args[1]));
			return CommandResultType.SUCCESSFUL;
		}
	}

}
