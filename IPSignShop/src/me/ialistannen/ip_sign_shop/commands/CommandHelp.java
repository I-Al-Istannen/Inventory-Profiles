package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

import me.ialistannen.ip_sign_shop.IPSignShop;

/**
 * Sends the help
 */
public class CommandHelp extends CommandPreset {

	/**
	 * Constructs a default instance
	 */
	public CommandHelp() {
		super("", false);
	}

	@Override
	public List<String> getTabCompletionChoices(int index, String[] message) {
		return Collections.emptyList();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		sender.sendMessage(tr(getIndentifier() + " header"));
		for (CommandPreset commandPreset : IPSignShop.getCommandManager().getAll()) {
			sender.sendMessage(commandPreset.getDescription());
			sender.sendMessage(commandPreset.getUsageMessage());
		}
		
		return true;
	}

}
