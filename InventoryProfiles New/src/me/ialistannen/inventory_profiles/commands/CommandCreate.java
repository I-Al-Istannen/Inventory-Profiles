package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.CommandNode;

/**
 * let's you create a profile
 */
public class CommandCreate extends CommandNode {

	/**
	 * New instance
	 */
	public CommandCreate() {
		super(tr("subCommandCreate name"), tr("subCommandCreate keyword"),
				Pattern.compile(tr("subCommandCreate pattern"), Pattern.CASE_INSENSITIVE),
				Util.PERMISSION_PREFIX + ".create");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandCreate usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandCreate description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, CommandSender tabCompleter) {
		int position = wholeUserChat.size() - 1;
		List<String> toReturn = new ArrayList<>();
		// 0 command name
		// 1 == name
		// 2 == Password
		// 3 == Op
		// 4 == World
		if(position == 3) {
			toReturn.add("true");
			toReturn.add("false");
		}
		if(position == 4) {
			Bukkit.getWorlds().stream().map(world -> world.getName()).forEach(toReturn::add);
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String... args) {
		if(args.length < 4) {
			return false;
		}
		
		String name = args[0];
		String password = args[1];
		
		Optional<Boolean> isOp = Util.getBoolean(args[2]);
		
		if(!isOp.isPresent()) {
			sender.sendMessage(tr("boolean not valid", args[2]));
			return true;
		}
		
		String worldName = args[3];
		
		if(Bukkit.getWorld(worldName) == null) {
			sender.sendMessage(tr("world not valid", worldName));
			return true;
		}
		
		Profile profile = new Profile(name, password, isOp.get(), Bukkit.getWorld(worldName));
		
		InventoryProfiles.getProfileManager().addProfile(profile);
		
		sender.sendMessage(tr("created user", profile.getName()));
		
		return true;
	}
	
}
