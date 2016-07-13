package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.tree_command_system.CommandNode;
import me.ialistannen.tree_command_system.HelpCommandNode;

/**
 * Sends the help
 */
@HelpCommandNode
public class CommandHelp extends CommandNode {
	
	/**
	 * Constructs an instance
	 */
	public CommandHelp() {
		super(tr("CommandHelp name"), tr("CommandHelp keyword"),
				Pattern.compile(tr("CommandHelp keyword"), Pattern.CASE_INSENSITIVE), "");
	}
	
	@Override
	public String getUsage() {
		return tr("CommandHelp usage", getName(), getKeyword());
	}
	
	@Override
	public String getDescription() {
		return tr("CommandHelp description", getName(), getKeyword());
	}

	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		return Collections.emptyList();
	}

	@Override
	public boolean execute(CommandSender sender, String... args) {
		sender.sendMessage(tr("CommandHelp header"));
		for (CommandNode commandPreset : IPSignShop.getInstance().getTreeManager().getAllCommands()) {
			sender.sendMessage(commandPreset.getDescription());
			sender.sendMessage(commandPreset.getUsage());
		}
		
		return true;
	}

}
