package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Logs a player out
 */
public class CommandLogout extends PlayerCommandNode {
	
	/**
	 * New instance
	 */
	public CommandLogout() {
		super(tr("subCommandLogout name"), tr("subCommandLogout keyword"),
				Pattern.compile(tr("subCommandLogout pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandLogout usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandLogout description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean execute(Player player, String... args) {		
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
