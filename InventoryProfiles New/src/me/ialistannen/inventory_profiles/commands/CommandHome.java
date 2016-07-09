package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;

/**
 * Teleports you home
 */
public class CommandHome extends CommandPreset {

	/**
	 * An instance of the home command
	 */
	public CommandHome() {
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
