package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Teleports you home
 */
public class CommandHome extends PlayerCommandNode {
	
	/**
	 * New instance
	 */
	public CommandHome() {
		super(tr("subCommandHome name"), tr("subCommandHome keyword"),
				Pattern.compile(tr("subCommandHome pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandHome usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandHome description", getName());
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
		
		Optional<Location> homeOpt = profile.getHome();
		
		if(!homeOpt.isPresent()) {
			player.sendMessage(tr("no home set"));
			return true;
		}
		
		player.teleport(homeOpt.get());
		
		player.sendMessage(tr("teleported home", player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
				
		return true;
	}

}
