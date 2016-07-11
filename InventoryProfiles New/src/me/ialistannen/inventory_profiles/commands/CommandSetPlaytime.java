package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.time.Duration;
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
 * Sets or shows the playtime for a user
 */
public class CommandSetPlaytime extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandSetPlaytime() {
		super(tr("subCommandSetPlaytime name"), tr("subCommandSetPlaytime keyword"),
				Pattern.compile(tr("subCommandSetPlaytime pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".setPlaytime");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandSetPlaytime usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandSetPlaytime description", getName());
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
		if(args.length < 2) {
			return false;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[0]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}

		Profile profile = optProf.get();
		
		Optional<Duration> durationOpt = Util.parseDurationString(args[1]);
		
		if(!durationOpt.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return true;
		}
		
		profile.setAvaillablePlaytime(durationOpt.get());
		sender.sendMessage(tr("set playtime", profile.getName(), Util.formatDuration(profile.getPlaytimeLeft())));
		return true;
	}

}
