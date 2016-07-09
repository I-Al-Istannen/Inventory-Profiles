package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;

/**
 * Sets the player's home
 */
public class CommandSetHome extends CommandPreset {

	/**
	 * A new instance
	 */
	public CommandSetHome() {
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
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
		
		profile.setHome(player.getLocation());
		
		player.sendMessage(tr("home was set", player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
				
		return true;
	}

}
