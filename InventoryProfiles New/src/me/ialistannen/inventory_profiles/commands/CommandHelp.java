package me.ialistannen.inventory_profiles.commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;

/**
 * Shows the help
 */
public class CommandHelp extends CommandPreset {

	/**
	 * A Help command instance
	 */
	public CommandHelp() {
		super("", false);
	}

	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		List<String> messages = InventoryProfiles.getCommandManager().getCommands().stream()
				.sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
				.flatMap(command -> {
					return Stream.of(command.getDescription(), command.getUsage());
				})
				.collect(Collectors.toList());
		
		sender.sendMessage(messages.toArray(new String[messages.size()]));
		return true;
	}
}
