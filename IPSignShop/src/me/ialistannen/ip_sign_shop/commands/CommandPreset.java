package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;

/**
 * A Preset for commands
 */
public abstract class CommandPreset {

	private String permission;
	private boolean mustBePlayer;
	
	/**
	 * @param permission The Permission. An empty String for everybody
	 * @param mustBePlayer If the sender must be a player
	 */
	public CommandPreset(String permission, boolean mustBePlayer) {
		this.permission = permission;
		this.mustBePlayer = mustBePlayer;
	}
	
	/**
	 * @return The Usage message
	 */
	public String getUsageMessage() {
		return tr(getIndentifier() + " usage", getName(), getKeyword());
	}
	
	/**
	 * @return The Description of the command
	 */
	public String getDescription() {
		return tr(getIndentifier() + " description", getName(), getKeyword());
	}
	
	/**
	 * @return The Permission
	 */
	public String getPermission() {
		return permission;
	}
	
	/**
	 * @return The name of the command
	 */
	public final String getName() {
		return tr(getIndentifier() + " name");
	}
	
	/**
	 * @return The unique Identifier for this command. It is getClass().getSimpleName().
	 */
	public final String getIndentifier() {
		return getClass().getSimpleName();
	}
	
	/**
	 * @return The Keyword of the command
	 */
	public String getKeyword() {
		return tr(getIndentifier() + " keyword");
	}
	
	/**
	 * @return True if the user must be a player
	 */
	public boolean isMustBePlayer() {
		return mustBePlayer;
	}
	
	/**
	 * @param sender The {@link CommandSender} to check
	 * @return True if the sender can use the command
	 */
	public boolean canUse(CommandSender sender) {
		if(getPermission().isEmpty()) {
			return true;
		}
		return sender.hasPermission(permission);
	}
	
	/**
	 * @param index The Index the user is currently at. 0 is the first command argument
	 * @param message The previous message
	 * @return A List with all valid tab choices. Null for Players.
	 */
	public abstract List<String> getTabCompletionChoices(int index, String[] message);
	
	/**
	 * @return All the Players' Displaynames
	 */
	protected List<String> getAllPlayerNames() {
		List<String> playerNames = new ArrayList<>();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			playerNames.add(player.getDisplayName());
		}
		
		return playerNames;
	}
	
	/**
	 * @param displayName The display name of the player
	 * @return The Player or null if none online
	 */
	protected Player getPlayer(String displayName) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if(player.getDisplayName().equals(displayName)) {
				return player;
			}
		}
		
		return null;
	}
	
	/**
	 * @param sender The {@link CommandSender}
	 * @param args The additional Argumentes
	 * @return If false, the usage will be send
	 */
	public abstract boolean execute(CommandSender sender, String[] args);
	
	/**
	 * @return A list with the names of all profiles
	 */
	protected List<String> getPrfileNames() {
		List<String> list = new ArrayList<>();
		for (Profile profile : InventoryProfiles.getProfileManager().getAll()) {
			list.add(profile.getName());
		}
		
		return list;
	}
}
