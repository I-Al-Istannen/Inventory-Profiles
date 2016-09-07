package me.ialistannen.inventory_profiles.runnables;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Automatically saves the shops, to prevent corruption due to crashes.
 */
public class AutoSaver extends BukkitRunnable {

	private static final String BACKUP_DIR_NAME = "backup";
	private static final String LOGGER_PREFIX = " [AutoSave]";

	@Override
	public void run() {
		try {
			saveSigns();
			saveProfiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the signs
	 * 
	 * @throws IOException
	 *             If an IOError occured
	 */
	private void saveSigns() throws IOException {
		Path baseDir = InventoryProfiles.getInstance().getDataFolder().toPath().resolve(BACKUP_DIR_NAME)
				.resolve("signs");

		Path writeTo = getSaveFileAndDeletePrevious(baseDir);

		// save it
		InventoryProfiles.getSignManager().save(writeTo);

		InventoryProfiles.getInstance().getLogger().info(LOGGER_PREFIX + " Saved a backup to '" + writeTo.getFileName()
				+ "'. Saved " + InventoryProfiles.getSignManager().getAll().size() + " signs.");
	}

	/**
	 * Saves the signs
	 * 
	 * @throws IOException
	 *             If an IOError occured
	 */
	private void saveProfiles() throws IOException {
		Path baseDir = InventoryProfiles.getInstance().getDataFolder().toPath().resolve(BACKUP_DIR_NAME)
				.resolve("profiles");

		Path writeTo = getSaveFileAndDeletePrevious(baseDir);

		// save it
		InventoryProfiles.getProfileManager().save(writeTo);

		InventoryProfiles.getInstance().getLogger().info(LOGGER_PREFIX + " Saved a backup to '" + writeTo.getFileName()
				+ "'. Saved " + InventoryProfiles.getProfileManager().getAll().size() + " profiles.");
	}

	private Path getSaveFileAndDeletePrevious(Path baseDir) throws IOException {

		// create it if needed
		if (Files.notExists(baseDir)) {
			Files.createDirectories(baseDir);
		}

		long amountOfSaves = Files.list(baseDir).count();

		// delete the oldest
		if (amountOfSaves >= InventoryProfiles.getInstance().getConfig().getInt("maximum backup amount")) {
			// lowest will be the first. Time gets bigger ==> Oldest the only
			// remaining one
			Files.list(baseDir).sorted((o1, o2) -> getLastModifiedTime(o1).compareTo(getLastModifiedTime(o2))).limit(1)
					.forEach(path -> {
						delete(path);
						InventoryProfiles.getInstance().getLogger()
								.info(LOGGER_PREFIX + " Deleted the oldest save: '" + path.getFileName() + "'");
					});
		}

		return baseDir.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".yml");
	}

	private FileTime getLastModifiedTime(Path path) {
		try {
			return Files.getLastModifiedTime(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// too bad... Can't do anything here, except returning some arbitrary
		// value.
		return null;
	}

	private void delete(Path path) {
		try {
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
