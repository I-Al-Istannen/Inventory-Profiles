package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.conversations.ConversationManager.ConversationType;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.signs.BuySign;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Allows the user to sell a region they have
 */
class CommandSellRegion extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandSellRegion() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_sell_region",
				Util.tr("command_sell_region_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		if (indexRelativeToYou == 1) {
			Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}


	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;
		if (args.length < 2) {
			return CommandResultType.SEND_USAGE;
		}

		if (!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();

		World world = Bukkit.getWorld(args[0]);

		if (world == null) {
			player.sendMessage(tr("world not valid", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		String region = args[1];

		if (!InventoryProfiles.hasRegionHook()) {
			player.sendMessage(tr("no region hook found"));
			return CommandResultType.SUCCESSFUL;
		}

		if (InventoryProfiles.getRegionHook().hasNoRegion(region, world)) {
			player.sendMessage(tr("region not valid", region));
			return CommandResultType.SUCCESSFUL;
		}

		Optional<RegionObject> regionObjOpt = profile.getRegionObjects().stream().filter(regionObject ->
				regionObject.getWorld().getUID().equals(world.getUID())
						&& regionObject.getRegionID().equalsIgnoreCase(region)
		).findFirst();

		if (!regionObjOpt.isPresent() || regionObjOpt.get().getRole() != RegionRole.OWNER) {
			player.sendMessage(tr("not your region"));
			return CommandResultType.SUCCESSFUL;
		}


		RegionObject regionObject = regionObjOpt.get();
		double refundMoney = regionObject.getPrice()
				* InventoryProfiles.getInstance().getConfig().getDouble("region refund percentage");

		InventoryProfiles.getConversationManager().startConversation(player, ConversationType.CONFIRMATION,
				event -> {
					if (!event.gracefulExit()) {
						return;
					}
					if (!(boolean) event.getContext().getSessionData("result")) {
						return;
					}
					profile.removeRegionObject(regionObject);

					profile.setMoney(profile.getMoney(true) + refundMoney);

					player.sendMessage(tr("sold region", regionObject.getRegionID(), refundMoney));

					if (!regionObject.getSignLocation().isPresent()) {
						player.sendMessage(tr("could not create buy sign"));
					}

					BuySign sign = new BuySign(regionObject.getSignLocation().get(), regionObject.getRegionID(),
							regionObject.getPrice(), regionObject.isWallSign(), regionObject.getSignFacingDirection()
							.orElse(BlockFace.SOUTH));

					InventoryProfiles.getSignManager().addSign(sign);
				}, tr("sell region confirmation prompt text", regionObject.getRegionID(), refundMoney));

		return CommandResultType.SUCCESSFUL;
	}

}
