package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * let's you create a profile
 */
class CommandCreate extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandCreate() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_create",
				Util.tr("command_create_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if(indexRelativeToYou == 2) {
			return Arrays.asList("true", "false");
		}
		else if(indexRelativeToYou == 3) {
			return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	
	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		if(args.length < 4) {
			return CommandResultType.SEND_USAGE;
		}
		
		String name = args[0];
		String password = args[1];
		
		Optional<Boolean> isOp = Util.getBoolean(args[2]);
		
		if(!isOp.isPresent()) {
			sender.sendMessage(tr("boolean not valid", args[2]));
			return CommandResultType.SUCCESSFUL;
		}
		
		String worldName = args[3];
		
		if(Bukkit.getWorld(worldName) == null) {
			sender.sendMessage(tr("world not valid", worldName));
			return CommandResultType.SUCCESSFUL;
		}
		
		Profile profile = new Profile(name, password, isOp.get(), Bukkit.getWorld(worldName));
		
		InventoryProfiles.getProfileManager().addProfile(profile);
		
		sender.sendMessage(tr("created user", profile.getName()));
		
		return CommandResultType.SUCCESSFUL;
	}
	
}
