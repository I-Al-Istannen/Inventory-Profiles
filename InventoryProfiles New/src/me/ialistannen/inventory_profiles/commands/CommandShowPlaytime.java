package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Allows the user to see his playtime / the playtime of another user
 */
public class CommandShowPlaytime extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandShowPlaytime() {
		super(tr("subCommandShowPlaytime name"), tr("subCommandShowPlaytime keyword"),
				Pattern.compile(tr("subCommandShowPlaytime pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandShowPlaytime usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandShowPlaytime description", getName());
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
		
		// show own playtime
		if(sender instanceof Player && !sender.hasPermission(Util.PERMISSION_PREFIX + ".showPlaytime")) {
			Player player = (Player) sender;
			Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());
			
			if(!profile.isPresent()) {
				sender.sendMessage(tr("not logged in"));
				return true;
			}
			
			sender.sendMessage(tr("show own playtime",
					Util.formatDuration(profile.get().getPlaytimeLeft()),
					Util.formatDuration(Duration.ofMillis(profile.get().getPlaytimeModifier()))));
			return true;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		Profile profile = optProf.get();
		
		sender.sendMessage(
				tr("show playtime for other user", 
						profile.getName(), 
						Util.formatDuration(profile.getPlaytimeLeft()),
						Util.formatDuration(Duration.ofMillis(profile.getPlaytimeModifier()))));
		
		return true;
	}

}
