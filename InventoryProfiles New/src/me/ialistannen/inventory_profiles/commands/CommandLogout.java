package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Logs a player out
 */
class CommandLogout extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandLogout() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_logout",
				Util.tr("command_logout_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return CommandResultType.SUCCESSFUL;
		}
		
		player.sendMessage(tr("logged out"));
		
		InventoryProfiles.getProfileManager().handlePlayerQuit(player);
		InventoryProfiles.getProfileManager().handlePlayerJoin(player);
		
		return CommandResultType.SUCCESSFUL;
	}
}
