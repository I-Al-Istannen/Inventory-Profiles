package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Deletes a profile
 */
public class CommandDelete extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandDelete() {
		super(tr("subCommandDelete name"), tr("subCommandDelete keyword"),
				Pattern.compile(tr("subCommandDelete pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".delete");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandDelete usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandDelete description", getName());
	}
	
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		List<String> toReturn = new ArrayList<>();
		if(wholeUserChat.size() == 2) {
			toReturn.addAll(Util.getAllProfileNames());
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 1) {
			return false;
		}
		
		Optional<Profile> profileOpt = InventoryProfiles.getProfileManager().getProfile(args[0]);
		
		if(!profileOpt.isPresent()) {
			sender.sendMessage(tr("username unknown"));
			return true;
		}
		
		InventoryProfiles.getProfileManager().removeProfile(profileOpt.get());
		
		sender.sendMessage(tr("deleted profile", profileOpt.get().getName()));		
		return true;
	}

}
