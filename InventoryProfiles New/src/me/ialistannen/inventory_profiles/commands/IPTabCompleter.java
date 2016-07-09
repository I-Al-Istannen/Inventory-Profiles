package me.ialistannen.inventory_profiles.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import me.ialistannen.inventory_profiles.InventoryProfiles;

/**
 * Allows for the tab completion of Commands
 */
public class IPTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {		
		if(args.length <= 1) {
			return InventoryProfiles.getCommandManager().getCommands().stream()
					.filter(commandPr -> commandPr.canUse(sender))
					.map(com -> com.getKeyword())
					.filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
					.collect(Collectors.toList());
		}
		
		Optional<CommandPreset> commandPreset = InventoryProfiles.getCommandManager().getCommand(args[0]);
		
		if(!commandPreset.isPresent() || !commandPreset.get().canUse(sender)) {
			return Collections.emptyList();
		}
				
		List<String> choices = commandPreset.get().onTabComplete(args.length - 2, Arrays.asList(args).stream().skip(1).collect(Collectors.toList()));
		
		// player names or nothing
		if(choices == null || choices.isEmpty()) {
			return choices;
		}
		
		return choices.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
	}

}
