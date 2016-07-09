package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;

/**
 * Logs a player out
 */
public class CommandLogout extends CommandPreset {

	/**
	 * Constructs an instance.
	 */
	public CommandLogout() {
		super("", true);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return true;
		}
		
		player.sendMessage(tr("logged out"));
		
		InventoryProfiles.getProfileManager().handlePlayerQuit(player);
		InventoryProfiles.getProfileManager().handlePlayerJoin(player);
		
		return true;
	}
}
