package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.tree_command_system.CommandNode;
import me.ialistannen.tree_command_system.HelpCommandNode;

/**
 * Shows the help
 */
@HelpCommandNode
public class CommandHelp extends CommandNode {

	/**
	 * New instance
	 */
	public CommandHelp() {
		super(tr("subCommandHelp name"), tr("subCommandHelp keyword"),
				Pattern.compile(tr("subCommandHelp pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandHelp usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandHelp description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		List<String> messages = InventoryProfiles.getInstance().getTreeManager().getAllCommands().stream()
				.sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
				.flatMap(command -> {
					return Stream.of(command.getDescription(), command.getUsage());
				})
				.collect(Collectors.toList());
		
		sender.sendMessage(messages.toArray(new String[messages.size()]));
		return true;
	}
}
