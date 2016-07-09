package me.ialistannen.ip_sign_shop.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;

/**
 * The tab completer for the main command
 */
public class MyCommandTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {	
		if(args.length == 1) {
			return IPSignShopUtil.getStartingWith(IPSignShop.getCommandManager().getAllCommandKeywords(), args[0]);
		}
		
		CommandPreset customCommand = IPSignShop.getCommandManager().getCommandFromKeyword(args[0]);
				
		if(customCommand == null) {
			return Collections.emptyList();
		}
		
		String[] argsToPass = new String[args.length - 1];
		System.arraycopy(args, 1, argsToPass, 0, argsToPass.length);

		List<String> choices = customCommand.getTabCompletionChoices(argsToPass.length - 1, argsToPass);
		if(choices == null) {
			return null;
		}
		
		return IPSignShopUtil.getStartingWith(choices, args[args.length - 1]);
	}
}
