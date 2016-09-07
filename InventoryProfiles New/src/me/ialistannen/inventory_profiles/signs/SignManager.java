package me.ialistannen.inventory_profiles.signs;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the {@link BuySign}s
 */
public class SignManager {

	private final Map<Location, BuySign> signMap = new HashMap<>();

	private final Path saveFile;

	/**
	 * @param saveFile
	 *            The Path to the save file for this Manager
	 */
	public SignManager(Path saveFile) {
		this.saveFile = saveFile;

		if (Files.exists(saveFile) && !Files.isDirectory(saveFile)) {
			loadFromFile(saveFile);
		}
	}

	/**
	 * Checks if there is a buysign at the given Location
	 * 
	 * @param loc
	 *            The location. Requires a Block location (yaw and pitch = 0)
	 * @return True if there is a buy sign at the given Location.
	 */
	public boolean hasSignAtLocation(Location loc) {
		return signMap.containsKey(loc);
	}

	/**
	 * Adds a {@link BuySign} to this manager
	 * 
	 * @param sign
	 *            The {@link BuySign} to add
	 */
	public void addSign(BuySign sign) {
		signMap.put(sign.getLocation(), sign);
		sign.update();
	}

	/**
	 * Removes the {@link BuySign} At the given Location. Fails silently if
	 * there is none. <br>
	 * Also calls {@link BuySign#delete()}, to delete the sign entirely.
	 * 
	 * @param sign
	 *            The {@link BuySign} to remove. Must not be null.
	 */
	public void removeSign(BuySign sign) {
		signMap.remove(sign.getLocation());
		sign.delete();
	}

	/**
	 * Retrieves a {@link BuySign} by it's location.
	 * 
	 * @param loc
	 *            The {@link Location} of the {@link BuySign}
	 * @return The {@link BuySign} or an empty optional if none found
	 */
	public Optional<BuySign> getBuySign(Location loc) {
		return Optional.ofNullable(signMap.get(loc));
	}

	/**
	 * Updates all signs
	 */
	public void updateAllSigns() {
		getAll().forEach(BuySign::update);
	}

	/**
	 * All the signs in an unmodifiable collection
	 * 
	 * @return An unmodifiable collection with all signs
	 */
	public Collection<BuySign> getAll() {
		return Collections.unmodifiableCollection(signMap.values());
	}

	/**
	 * @param path
	 *            The Path to load from
	 */
	private void loadFromFile(Path path) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		if (!config.contains("signs")) {
			return;
		}
		config.getList("signs").stream().filter(obj -> obj instanceof BuySign).map(obj -> (BuySign) obj)
				.forEach(this::addSign);
	}

	/**
	 * Saves the {@link SignManager} to the the savefile specified upon creation
	 * 
	 * @return True if the file was saved, false otherwise
	 */
	public boolean save() {
		return save(saveFile);
	}

	/**
	 * Saves the {@link SignManager} to the the savefile specified upon creation
	 * 
	 * @param path
	 *            The path to save to
	 * 
	 * @return True if the file was saved, false otherwise
	 */
	public boolean save(Path path) {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.set("signs", new ArrayList<>(getAll()));
			config.save(path.toFile());

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
