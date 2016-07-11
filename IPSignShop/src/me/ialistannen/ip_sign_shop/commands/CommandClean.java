package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;

/**
 * Removes all chests without stock
 */
public class CommandClean extends CommandPreset {

	/**
	 * Constructs an instance
	 */
	public CommandClean() {
		super(IPSignShopUtil.PERMISSION_PREFIX + ".clean", false);
	}

	@Override
	public List<String> getTabCompletionChoices(int index, String[] message) {
		return Collections.emptyList();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		int counter = 0;
		for (Shop shop : IPSignShop.getShopManager().getAllShops()) {
			if(!shop.hasItemsLeft()) {
				IPSignShop.getShopManager().removeShop(shop.getSignLocation());
				counter++;
			}
		}
		
		sender.sendMessage(tr(getIndentifier() + " removed shops", counter));
		
		return true;
	}

}
