package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Teleports you home
 */
class CommandHome extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandHome() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_home",
				Util.tr("command_home_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		if (!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();

		Optional<Location> homeOpt = profile.getHome();

		if (!homeOpt.isPresent()) {
			player.sendMessage(tr("no home set"));
			return CommandResultType.SUCCESSFUL;
		}

		player.teleport(homeOpt.get());

		player.sendMessage(tr("teleported home", player.getLocation().getBlockX(), player.getLocation().getBlockY(),
				player.getLocation().getBlockZ()));

		return CommandResultType.SUCCESSFUL;
	}

}
