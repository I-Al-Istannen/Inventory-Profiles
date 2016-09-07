package me.ialistannen.ip_sign_shop.commands;

import me.ialistannen.bukkitutil.commandsystem.implementation.RelayCommandNode;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.ip_sign_shop.IPSignShop;

/**
 * The main command
 */
public class CommandIpSignShop extends RelayCommandNode {

	public CommandIpSignShop() {
		super(IPSignShop.getInstance().getLanguage(), "command_ip_sign_shop",
				Util.tr("command_ip_sign_shop_permission"), sender -> true);

		addChild(new CommandSetOwner());
		addChild(new CommandSetMode());
		addChild(new CommandSetPrice());
		addChild(new CommandClean());
		addChild(new CommandRemove());
		addChild(new CommandFind());
	}
}
