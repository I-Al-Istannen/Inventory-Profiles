package me.ialistannen.ip_sign_shop.datastorage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A manager for {@link Shop}s. Let's you perform some lookups and stuff.
 */
public class ShopManager {

	private final Map<Location, Shop> signLocShopMap = new HashMap<>();
	
	/**
	 * Just there to allow the default constructor to be used
	 */
	public ShopManager() {		
	}
	
	/**
	 * To allow it to be fed with Shops from a save
	 * 
	 * @param signLocationShopMap All the known shops
	 */
	private ShopManager(Map<Location, Shop> signLocationShopMap) {
		signLocShopMap.putAll(signLocationShopMap);
	}
	
	/**
	 * @param loc The location. Yaw and Pitch should be the same as calling Location.getBlock().getLocation(), so probably 0;
	 * @return The Shop or null if none found
	 */
	public Shop getShopForLocation(Location loc) {
		return signLocShopMap.get(loc);
	}
	
	/**
	 * @param owner The owner to get the shops from
	 * @return All the shops for the owner or an empty list if none.
	 */
	public List<Shop> getShopsForOwner(String owner) {
		List<Shop> shops = signLocShopMap.values().stream()
				.filter(shop -> shop.getOwner().equals(owner))
				.collect(Collectors.toCollection(LinkedList::new));

		return shops;
	}
	
	/**
	 * @param mat The Material to look for
	 * @return All the shops that sell the given Material
	 */
	public List<Shop> getShopsForMaterial(Material mat) {
		List<Shop> shops = signLocShopMap.values().stream()
				.filter(shop -> shop.getItem().getType() == mat)
				.collect(Collectors.toCollection(LinkedList::new));

		return shops;
	}
	
	/**
	 * @param item The item to look for. Ignores the amount.
	 * @return All the shops that sell the given item.
	 */
	public List<Shop> getShopsForItem(ItemStack item) {
		List<Shop> shops = signLocShopMap.values().stream()
				.filter(shop -> shop.getItem().isSimilar(item))
				.collect(Collectors.toCollection(LinkedList::new));

		return shops;
	}
	
	/**
	 * Finds a Shop by it's chest coordinates
	 * 
	 * @param location The Location to check at
	 * @return The Shop that has a Chest at the given location or null if there is none
	 */
	public Shop getShopForChestLocation(Location location) {
		for (Shop shop : signLocShopMap.values()) {
			if(shop.isYourChest(location)) {
				return shop;
			}
		}
		
		return null;
	}
	
	/**
	 * @param inventory The {@link Inventory} to check
	 * @return The Shop with the given Inventory or null if none found
	 */
	public Shop getShopForInventory(Inventory inventory) {
		for (Shop shop : getAllShops()) {
			if(shop.getShopInventory().equals(inventory)) {
				return shop;
			}
		}
		
		return null;
	}
	
	/**
	 * @return All the shops.
	 */
	public List<Shop> getAllShops() {
		return new ArrayList<>(signLocShopMap.values());
	}
	
	/**
	 * Adds the shop and overwrites any shop at the location.
	 * 
	 * @param shop The shop to add
	 */
	public void addShop(Shop shop) {
		signLocShopMap.put(shop.getSignLocation(), shop);
	}
	
	/**
	 * Removes the Shop if present
	 * 
	 * @param signLocation The location of the shop sign
	 */
	public void removeShop(Location signLocation) {
		Shop old = signLocShopMap.remove(signLocation);
		if(old != null) {
			old.delete();
		}
	}
	
	/**
	 * @param signLoc The sign location to check at
	 * @return True if there is a shop at the location
	 */
	public boolean hasShopAtLocation(Location signLoc) {
		return signLocShopMap.containsKey(signLoc);
	}
	
	/**
	 * @param loc The location of the Chest
	 * @return True if this location is a Shop-Chest
	 */
	public boolean isShopChest(Location loc) {
		for (Shop shop : getAllShops()) {
			if(shop.isYourChest(loc)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * The sane as {@link #hasShopAtLocation(Location)}
	 * 
	 * @param loc The Location to check
	 * @return True if the sign at the given location is a shop sign
	 */
	public boolean isNoShopSign(Location loc) {
		return !hasShopAtLocation(loc);
	}
	
	/**
	 * @param item The item to check
	 * @return True if the item is a display item for a shop
	 */
	public boolean isShopDisplayItem(Item item) {
		for (Shop shop : getAllShops()) {
			if(shop.isYourItem(item)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Despawns the shop items for every shop. To be used in onDisable to prevent item duplication at the next restart.
	 */
	public void despawnAllItems() {
		getAllShops().forEach(Shop::despawnItem);
	}
	
	/**
	 * Updates all shops
	 */
	public void updateAllShops() {
		getAllShops().forEach(Shop::updateShop);
	}
	
	/**
	 * @param path The Path to read from
	 * @return The {@link ShopManager}
	 * 
	 * @throws FileNotFoundException If the file couldn't be found
	 * @throws IllegalArgumentException If the File is a directory
	 */
	public static ShopManager fromFile(Path path) throws FileNotFoundException, IllegalArgumentException {
		if(Files.isDirectory(path)) {
			throw new IllegalArgumentException("The file is a directoy");
		}
		if(!Files.exists(path)) {
			throw new FileNotFoundException("The file " + path + " couldn't be found!");
		}
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
		Map<Location, Shop> map = new HashMap<>();

		config.getValues(false).entrySet().stream()
				.filter(entry -> entry.getValue() instanceof Shop)
				.map(entry -> (Shop) entry.getValue())
				.forEach(shop -> map.put(shop.getSignLocation(), shop));
		
		return new ShopManager(map);
	}
	
	/**
	 * @param path The Path to write it to
	 * @param copyOption Only respected one is {@link StandardCopyOption#REPLACE_EXISTING}.
	 * @return True if the file was saved successfully, false if an IOError occured.
	 * @throws FileAlreadyExistsException If the CopyOption isn't REPLACE_EXISTING and the file already exists
	 */
	public boolean toFile(Path path, StandardCopyOption copyOption) throws FileAlreadyExistsException {
		if(Files.exists(path) && !(copyOption == StandardCopyOption.REPLACE_EXISTING)) {
			throw new FileAlreadyExistsException("The file " + path + " already exists and it was not requested to overwrite it.");
		}
		
		YamlConfiguration config = new YamlConfiguration();
		for (int i = 0; i < getAllShops().size(); i++) {
			Shop shop = getAllShops().get(i);
			
			// save some space ==> hexadecimal
			config.set(Integer.toHexString(i), shop);
		}
		
		try {
			config.save(path.toFile());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
