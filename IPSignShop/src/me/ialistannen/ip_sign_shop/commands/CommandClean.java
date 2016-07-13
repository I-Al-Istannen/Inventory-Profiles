package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Removes all chests without stock
 */
public class CommandClean extends CommandNode {

	/**
	 * Constructs an instance
	 */
	public CommandClean() {
		super(tr("CommandClean name"), tr("CommandClean keyword"),
				Pattern.compile(tr("CommandClean keyword"), Pattern.CASE_INSENSITIVE),
				IPSignShopUtil.PERMISSION_PREFIX + ".clean");
	}
	
	@Override
	public String getUsage() {
		return tr("CommandClean usage", getName(), getKeyword());
	}
	
	@Override
	public String getDescription() {
		return tr("CommandClean description", getName(), getKeyword());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		return Collections.emptyList();
	}

	@Override
	public boolean execute(CommandSender sender, String... args) {
		int counter = 0;
		for (Shop shop : IPSignShop.getShopManager().getAllShops()) {
			if(!shop.hasItemsLeft()) {
				IPSignShop.getShopManager().removeShop(shop.getSignLocation());
				counter++;
			}
		}
		
		sender.sendMessage(tr("CommandClean removed shops", counter));
		
		return true;
	}

}
