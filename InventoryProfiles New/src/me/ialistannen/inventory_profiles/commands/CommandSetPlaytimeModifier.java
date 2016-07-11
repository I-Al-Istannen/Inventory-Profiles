package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * Allows the setting of a playtime modifier
 */
public class CommandSetPlaytimeModifier extends CommandNode {
	
	/**
	 * New instance
	 */
	public CommandSetPlaytimeModifier() {
		super(tr("subCommandSetPlaytimeModifier name"), tr("subCommandSetPlaytimeModifier keyword"),
				Pattern.compile(tr("subCommandSetPlaytimeModifier pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".setPlaytimeModifier");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandSetPlaytimeModifier usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandSetPlaytimeModifier description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		if(wholeUserChat.size() == 2) {
			return Util.getAllProfileNames();
		}
		return Collections.emptyList();
	}

	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 3) {
			return false;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		Optional<Duration> duration = Util.parseDurationString(args[1]);
		
		if(!duration.isPresent()) {
			sender.sendMessage(tr("time not valid", args[1]));
			return true;
		}
		
		Optional<Duration> timeValid = Util.parseDurationString(args[2]);
		
		if(!timeValid.isPresent()) {
			sender.sendMessage(tr("time not valid", args[2]));
			return true;
		}
		
		if(timeValid.get().toDays() < 1) {
			sender.sendMessage(tr("time too small", timeValid.get().toDays()));
			return true;
		}
		
		profile.setPlaytimeModifier(duration.get().toMillis(), LocalDate.now().plusDays(timeValid.get().toDays() - 1));
		
		sender.sendMessage(tr("set playtime modifier", profile.getName(), Util.formatDuration(duration.get()), timeValid.get().toDays()));
		return true;
	}

}
