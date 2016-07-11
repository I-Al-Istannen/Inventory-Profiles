package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Sets the player's home
 */
public class CommandSetHome extends PlayerCommandNode {
	
	/**
	 * New instance
	 */
	public CommandSetHome() {
		super(tr("subCommandSetHome name"), tr("subCommandSetHome keyword"),
				Pattern.compile(tr("subCommandSetHome pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandSetHome usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandSetHome description", getName());
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
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
		
		profile.setHome(player.getLocation());
		
		player.sendMessage(tr("home was set", player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
				
		return true;
	}

}
