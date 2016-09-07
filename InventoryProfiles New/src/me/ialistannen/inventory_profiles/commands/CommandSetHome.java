package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Sets the player's home
 */
class CommandSetHome extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandSetHome() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_set_home",
				Util.tr("command_set_home_permission"), sender -> sender instanceof Player);
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

		profile.setHome(player.getLocation());

		player.sendMessage(tr("home was set", player.getLocation().getBlockX(), player.getLocation().getBlockY(),
				player.getLocation().getBlockZ()));

		return CommandResultType.SUCCESSFUL;
	}

}
