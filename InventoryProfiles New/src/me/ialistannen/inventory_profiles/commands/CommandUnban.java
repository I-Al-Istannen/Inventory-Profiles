package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Unbans a player
 */
public class CommandUnban extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandUnban() {
		super(tr("subCommandUnban name"), tr("subCommandUnban keyword"),
				Pattern.compile(tr("subCommandUnban pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".unban");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandUnban usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandUnban description", getName());
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
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		if(!profile.isBanned()) {
			sender.sendMessage(tr("player not banned"));
			return true;
		}
		
		profile.unban();
		
		sender.sendMessage(tr("unbanned player", profile.getName()));
		return true;
	}

}
