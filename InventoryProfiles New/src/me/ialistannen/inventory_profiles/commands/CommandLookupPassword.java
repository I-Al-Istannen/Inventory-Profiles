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
 * Looks up the password of a player
 */
public class CommandLookupPassword extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandLookupPassword() {
		super(tr("subCommandLookupPassword name"), tr("subCommandLookupPassword keyword"),
				Pattern.compile(tr("subCommandLookupPassword pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".lookupPassword");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandLookupPassword usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandLookupPassword description", getName());
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
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		sender.sendMessage(tr("password lookuped", optProf.get().getName(), optProf.get().getPassword()));
		return true;
	}

}
