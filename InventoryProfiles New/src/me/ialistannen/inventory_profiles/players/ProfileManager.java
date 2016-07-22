package me.ialistannen.inventory_profiles.players;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.conversations.ConversationManager.ConversationType;
import me.ialistannen.inventory_profiles.listener.PlayerListener;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * manages the profiles
 */
public class ProfileManager {

	private Map<String, Profile> profileMap = new HashMap<>();
	private Path saveFile;

	/**
	 * @param saveFile
	 *            The Path to the save file for this Manager
	 */
	public ProfileManager(Path saveFile) {
		this.saveFile = saveFile;

		if (Files.exists(saveFile) && !Files.isDirectory(saveFile)) {
			loadFromFile(saveFile);
		}
	}

	/**
	 * @param profile
	 *            The profile to add
	 */
	public void addProfile(Profile profile) {
		profileMap.put(profile.getName(), profile);
	}

	/**
	 * @param profile
	 *            The profile to remove
	 */
	public void removeProfile(Profile profile) {
		profileMap.remove(profile.getName());
	}

	/**
	 * @param name
	 *            The name of the profile
	 * @return True if it has a profile with the name
	 */
	public boolean hasProfile(String name) {
		return profileMap.get(name) != null;
	}

	/**
	 * Optional or not, that is the question
	 * 
	 * @param name
	 *            The name of the profile
	 * @return The profile or an empty optional if none found
	 */
	public Optional<Profile> getProfile(String name) {
		return Optional.ofNullable(profileMap.get(name));
	}

	/**
	 * Returns all the profiles
	 * 
	 * @return An unmodifiable collection with all profile
	 */
	public Collection<Profile> getAll() {
		return Collections.unmodifiableCollection(profileMap.values());
	}

	/**
	 * @param player
	 *            The player who joined
	 */
	public void handlePlayerJoin(Player player) {
		PlayerListener.setFrozen(player, true);

		// teleport to world spawn
		Bukkit.getScheduler().runTask(InventoryProfiles.getInstance(), () -> {
			player.teleport(player.getWorld().getSpawnLocation());
		});

		if (InventoryProfiles.hasRegionHook()) {
			InventoryProfiles.getRegionHook().removeUserFromAllRegions(player);
		}

		InventoryProfiles.getConversationManager().startConversation(player, ConversationType.LOGIN, (event) -> {
			if (!event.gracefulExit()) {
				player.kickPlayer(tr("not logged in"));
				return;
			}

			Profile profile = (Profile) event.getContext().getSessionData("profile");
			profile.applyToPlayer(player);

			PlayerListener.setFrozen(player, false);
			checkAndKickPlayerPlaytime(profile);
			checkAndKickPlayerBanned(profile);
		});
	}

	/**
	 * Kicks the player if the playtime has run out. Doesn't set
	 * {@link Profile#setPlaytimeWentOut(LocalDateTime)}
	 * 
	 * @param profile
	 *            The profile
	 */
	public void checkAndKickPlayerPlaytime(Profile profile) {
		if (!profile.hasPlaytimeLeft()) {
			LocalDateTime playtimeOut = profile.getPlaytimeWentOut().orElse(LocalDateTime.now());
			Duration resetTime = Util.getPlaytimeResetDelay().orElseGet(() -> {
				InventoryProfiles.getInstance().getLogger().log(Level.WARNING, "The playtime reset delay is invalid: "
						+ InventoryProfiles.getInstance().getConfig().getString("playtime reset delay"));

				return Duration.ofHours(-1);
			});

			profile.getPlayer().ifPresent(player -> {
				player.kickPlayer(tr("no playtime left",
						Util.formatDuration(Duration.between(LocalDateTime.now(), playtimeOut.plus(resetTime)))));
			});
		}
	}

	/**
	 * Checks if the player is banned and kicks him if he is
	 * 
	 * @param profile
	 *            The profile to check
	 */
	public void checkAndKickPlayerBanned(Profile profile) {
		if (profile.isBanned()) {
			Duration banTime = Duration.between(LocalDateTime.now(), profile.getBannedUntil());

			profile.getPlayer().ifPresent(player -> {
				player.kickPlayer(tr("banned message", Util.formatDuration(banTime), profile.getBanReason()));
			});
		}
	}

	/**
	 * @param player
	 *            The Player who quit
	 */
	public void handlePlayerQuit(Player player) {
		PlayerListener.setFrozen(player, false);
		InventoryProfiles.getConversationManager().cancelConversation(player);
		Util.updateSavedPlayerData(player);
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);

		// Remove the user from all regions
		InventoryProfiles.getProfileManager().getProfile(player.getDisplayName())
				.ifPresent(Profile::removeFromAllRegions);

		if (InventoryProfiles.getInstance().getConfig().getBoolean("log logins and logouts")) {
			Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());
			profile.ifPresent(prof -> {
				InventoryProfiles.getInstance().getLogger()
						.info(tr("player logged out console message", prof.getName(), prof.isBanned(), prof.isOp()));
			});
		}

		player.setDisplayName(player.getName());
		player.setPlayerListName(player.getName());
	}

	/**
	 * @param path
	 *            The Path to load from
	 */
	private void loadFromFile(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		ConfigurationSection section = config.getConfigurationSection("map");
		section.getValues(false).values().stream().map(obj -> (Profile) obj).forEach(this::addProfile);
	}

	/**
	 * Saves the ProfileManager to the the savefile specified upon creation
	 * 
	 * @return True if the file was saved, false otherwise
	 */
	public boolean save() {
		return save(saveFile);
	}

	/**
	 * Saves the ProfileManager to the the savefile specified upon creation
	 * 
	 * @param path
	 *            The path to save to
	 * 
	 * @return True if the file was saved, false otherwise
	 */
	public boolean save(Path path) {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.set("map", profileMap);
			config.save(path.toFile());

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
