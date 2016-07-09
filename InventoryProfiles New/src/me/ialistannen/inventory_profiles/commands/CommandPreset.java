package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.ialistannen.inventory_profiles.InventoryProfiles;

/**
 * A Preset for a command
 */
public abstract class CommandPreset {

	private String permission;
	private boolean mustBePlayer;
	
	/**
	 * @param permission The Permission needed for this command. "" for none
	 * @param mustBePlayer If the {@link CommandSender} must be a player
	 */
	public CommandPreset(String permission, boolean mustBePlayer) {
		this.permission = permission;
		this.mustBePlayer = mustBePlayer;
	}

	/**
	 * @return The name of this command
	 */
	public String getName() {
		return tr(getIdentifier() + " name");
	}
	
	/**
	 * @return The keyword of the command
	 */
	public String getKeyword() {
		return tr(getIdentifier() + " keyword");
	}
	
	/**
	 * @return The Usage of the command
	 */
	public String getUsage() {
		return tr(getIdentifier() + " usage", getName());
	}

	/**
	 * @return The patter of this command
	 */
	public Pattern getPattern() {
		return Pattern.compile(tr(getIdentifier() + " pattern"), Pattern.CASE_INSENSITIVE);
	}
	
	/**
	 * @return The description of the command
	 */
	public String getDescription() {
		return tr(getIdentifier() + " description", getName());
	}
	
	/**
	 * @return The identifier ("sub" + getClass().getSimpleName())
	 */
	private String getIdentifier() {
		return "sub" + getClass().getSimpleName();
	}
	
	/**
	 * @return True if the sender must be a player
	 */
	public boolean isMustBePlayer() {
		return mustBePlayer;
	}
	
	/**
	 * @return The permission needed to execute this command. "" means none
	 */
	public String getPermission() {
		return permission;
	}
	
	/**
	 * @param sender The CommandSender to check
	 * @return True if he can use the command
	 */
	public boolean canUse(CommandSender sender) {
		return getPermission().isEmpty() ? true : sender.hasPermission(permission);
	}
	
	/**
	 * @param input The input to match against
	 * @return True if it matches the pattern
	 */
	public boolean matchesPattern(String input) {
		return getPattern().matcher(input).matches();
	}
	
	/**
	 * Returns the possible tab completions for this command.
	 * 
	 * @param position The Position the user currently tab completes at
	 * @param messages The Things the user has written beofe
	 * @return A List with all Possible tab completions. Null for player names and empty for nothing.
	 */
	public abstract List<String> onTabComplete(int position, List<String> messages);
	
	/**
	 * @param sender The {@link CommandSender}
	 * @param args The Arguments of the command
	 * @return True if the usage should be shown. False otherwise
	 */
	public abstract boolean execute(CommandSender sender, String[] args);
	
	/**
	 * To be used in the {@link #onTabComplete(int, List)}
	 * 
	 * @return A List with the names of all Profiles
	 */
	protected List<String> getAllProfileNames() {
		return InventoryProfiles.getProfileManager().getAll().stream().map(prof -> prof.getName()).collect(Collectors.toList());
	}
}
