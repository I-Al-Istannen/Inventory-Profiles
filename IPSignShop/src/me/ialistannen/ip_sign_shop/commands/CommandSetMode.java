package me.ialistannen.ip_sign_shop.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

/**
 * Allows the setting of the mode
 */
class CommandSetMode extends DefaultCommand {

	/**
	 * Constructs an instance
	 */
	public CommandSetMode() {
		super(IPSignShop.getInstance().getLanguage(), "command_set_mode",
				tr("command_set_mode_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if (indexRelativeToYou == 0) {
			List<String> list = new ArrayList<>();
			list.add(tr("command_set_mode_sell"));
			list.add(tr("command_set_mode_sell_unlimited"));
			list.add(tr("command_set_mode_buy"));
			list.add(tr("command_set_mode_buy_unlimited"));    // damn that typo
			return list;
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

		String mode = args[0];
		ShopMode newMode = shop.getMode();
		if (mode.equalsIgnoreCase(tr("command_set_mode_sell"))) {
			newMode = ShopMode.SELL;
		}
		else if (mode.equalsIgnoreCase(tr("command_set_mode_sell_unlimited"))) {
			if (!player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + " set mode unlimited")) {
				player.sendMessage(tr("command_set_mode_no_permission_for_unlimited_resources", getName()));
				return CommandResultType.SUCCESSFUL;
			}
			newMode = ShopMode.SELL_UNLIMITED;
		}
		else if (mode.equalsIgnoreCase(tr("command_set_mode_buy"))) {
			newMode = ShopMode.BUY;
		}
		else if (mode.equalsIgnoreCase(tr("command_set_mode_buy_unlimited"))) {
			if (!player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + " set mode unlimited")) {
				player.sendMessage(tr("command_set_mode_no_permission_for_unlimited_resources", getName()));
				return CommandResultType.SUCCESSFUL;
			}
			newMode = ShopMode.BUY_UNLIMITED;
		}

		String oldMode = shop.getMode().getShopModeName();
		shop.setMode(newMode);

		player.sendMessage(tr("command_set_mode_set_shop_mode", shop.getOwner(), newMode.getShopModeName(), oldMode));

		return CommandResultType.SUCCESSFUL;
	}

}
