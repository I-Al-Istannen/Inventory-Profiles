package me.ialistannen.ip_sign_shop.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

/**
 * Sets the owner of the shop
 */
class CommandSetOwner extends DefaultCommand {

	/**
	 * Constructs an instance
	 */
	public CommandSetOwner() {
		super(IPSignShop.getInstance().getLanguage(), "command_set_owner",
				tr("command_set_owner_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if (indexRelativeToYou == 0) {
			return Bukkit.getOnlinePlayers().stream()
					.filter(target -> !(sender instanceof Player) || ((Player) sender).canSee(target))
					.map(Player::getName)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		if (args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}

		Block targetBlock = player.getTargetBlock((Set<Material>) null, 100);

		if (targetBlock == null || targetBlock.getType() == Material.AIR) {
			player.sendMessage(tr("no block in sight"));
			return CommandResultType.SUCCESSFUL;
		}

		if (targetBlock.getType() != Material.WALL_SIGN) {
			player.sendMessage(tr("block not a sign", trItem(targetBlock.getType())));
			return CommandResultType.SUCCESSFUL;
		}

		if (!IPSignShop.getShopManager().hasShopAtLocation(targetBlock.getLocation())) {
			player.sendMessage(tr("not a shop", trItem(targetBlock.getType())));
			return CommandResultType.SUCCESSFUL;
		}

		Shop shop = IPSignShop.getShopManager().getShopForLocation(targetBlock.getLocation());

		if (!shop.getOwner().equals(player.getDisplayName()) && !player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX
				+ ".alterOther")) {
			player.sendMessage(tr("no permission to alter other players shop", shop.getOwner()));
			return CommandResultType.SUCCESSFUL;
		}

		String newOwnerName = args[0];

		if (!InventoryProfiles.getProfileManager().hasProfile(newOwnerName)) {
			player.sendMessage(tr("player not known", newOwnerName));
			return CommandResultType.SUCCESSFUL;
		}

		String oldOwnerName = shop.getOwner();
		shop.setOwner(newOwnerName);

		player.sendMessage(tr("command_set_owner_transferred_ownership", newOwnerName, oldOwnerName));

		IPSignShopUtil.getPlayerByDisplayOrName(newOwnerName)
				.ifPresent(newOwner ->
						newOwner.sendMessage(tr("command_set_owner_received_ownership",
								oldOwnerName, player.getDisplayName()))
				);

		return CommandResultType.SUCCESSFUL;
	}
}
