package me.ialistannen.ip_sign_shop.datastorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.bukkit.scheduler.BukkitRunnable;

import me.ialistannen.ip_sign_shop.IPSignShop;


/**
 * Automatically saves the shops, to prevent corruption due to crashes.
 */
public class AutoSaver extends BukkitRunnable {

	private static final String BACKUP_DIR_NAME = "backup";
	private static final String LOGGER_PREFIX = " [AutoSave]";
	
	@Override
	public void run() {
		try {
			runExceptional();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void runExceptional() throws IOException {
		Path baseDir = IPSignShop.getInstance().getSaveFile().getParent().resolve(BACKUP_DIR_NAME);
		
		// create it if needed
		if(Files.notExists(baseDir)) {
			Files.createDirectories(baseDir);
		}
		
		long amountofSaves = Files.list(baseDir).count();
		
		// delete the oldest
		if(amountofSaves >= IPSignShop.getInstance().getConfig().getInt("maximum backup amount")) {
			// lowest will be the first. Time gets bigger ==> Oldest the only remaining one
			Files.list(baseDir).sorted((o1, o2) -> getLastModifiedTime(o1).compareTo(getLastModifiedTime(o2))).limit(1).forEach(path -> {
				delete(path);
				IPSignShop.getInstance().getLogger().info(LOGGER_PREFIX + " Deleted the oldest save: '" + path.getFileName() + "'");
			});
		}
		
		Path writeTo = baseDir.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".yml");
		
		IPSignShop.getShopManager().toFile(writeTo, StandardCopyOption.REPLACE_EXISTING);
		IPSignShop.getInstance().getLogger().info(LOGGER_PREFIX + " Saved a backup to '" + writeTo.getFileName()
				+ "'. Saved " + IPSignShop.getShopManager().getAllShops().size() + " shops.");
	}
	
	private FileTime getLastModifiedTime(Path path) {
		try {
			return Files.getLastModifiedTime(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// too bad... Can't do anything here, except returning some arbitrary value.
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
