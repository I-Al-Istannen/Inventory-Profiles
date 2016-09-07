package me.ialistannen.ip_sign_shop.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

/**
 * Removes all chests without stock
 */
class CommandClean extends DefaultCommand {

	/**
	 * Constructs an instance
	 */
	public CommandClean() {
		super(IPSignShop.getInstance().getLanguage(), "command_clean",
				tr("command_clean_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		int counter = 0;
		for (Shop shop : IPSignShop.getShopManager().getAllShops()) {
			if (shop.hasNoItemsLeft()) {
				IPSignShop.getShopManager().removeShop(shop.getSignLocation());
				counter++;
			}
		}

		sender.sendMessage(tr("command_clean_removed_shops", counter));

		return CommandResultType.SUCCESSFUL;
	}

}
