package me.ialistannen.ip_sign_shop.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.ialistannen.ip_sign_shop.IPSignShop;

/**
 * Some util methods
 */
public class IPSignShopUtil {

	/**
	 * The Formatter used for Currency things in the plugin
	 */
	public static final NumberFormat CURRENCY_FORMATTER = new DecimalFormat("$#.00");
	
	/**
	 * The Prefix for all permissions
	 */
	public static final String PERMISSION_PREFIX = "ipsignshop";
	
	/**
	 * Formats enum constants according to the german language
	 * 
	 * @param input The Input String
	 * @return The name in lowercase, but with every word starting with an uppercase char
	 */
	public static String getNiceNameForConstant(String input) {
		StringBuilder builder = new StringBuilder();
		
		boolean upperCase = true;
		for (char c : input.toCharArray()) {
			if(c == '_') {
				upperCase = true;
				builder.append(" ");
				continue;
			}
			
			if(upperCase) {
				builder.append(Character.toUpperCase(c));
				upperCase = false;
			}
			else {
				builder.append(Character.toLowerCase(c));
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * @param xDiff The Difference on the x axis
	 * @param yDiff The Difference on the y axis
	 * @param zDiff The Difference on the z axis
	 * @return The corresponding blockFace or null if none found
	 */
	public static BlockFace getBlockFace(int xDiff, int yDiff, int zDiff) {
		for (BlockFace blockFace : BlockFace.values()) {
			if(blockFace.getModX() == xDiff && blockFace.getModY() == yDiff && blockFace.getModZ() == zDiff) {
				return blockFace;
			}
		}
		return null;
	}

	/**

	 * @param difference The difference
	 * @return The corresponding blockFace or null if none found
	 */
	public static BlockFace getBlockFace(Vector difference) {
		for (BlockFace blockFace : BlockFace.values()) {
			if(blockFace.getModX() == difference.getBlockX() && blockFace.getModY() == difference.getBlockY() && blockFace.getModZ() == difference.getBlockZ()) {
				return blockFace;
			}
		}
		return null;
	}
	
	/**
	 * @param toRepeat The String to repeat
	 * @param amount The amount to repeat it for
	 * @param delimeter The delimeter between them
	 * @return The resulting String
	 */
	public static String repeat(String toRepeat, int amount, String delimeter) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < amount; i++) {
			if(i > 0) {
				builder.append(delimeter);
			}
			
			builder.append(toRepeat);
		}
		
		return builder.toString();
	}
	
	/**
	 * @return The seperator for a chatline. Before and after the messages of this plugin.
	 */
	public static String getChatLineSeperator() {
		return ChatColor.DARK_PURPLE + "+" + repeat("-", 50, "");
	}
	
	/**
	 * Uses '&' as the color char
	 * 
	 * @param input The text to color
	 * @return The colored text
	 */
	public static String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	
	/**
	 * @param inventory The inventory to search in
	 * @param item The item to check for
	 * @return The amount of items in the shop
	 */
	public static int getItemAmount(Inventory inventory, ItemStack item) {
		int totalAmount = 0;
		
		for (ItemStack itemStack : inventory.getContents()) {
			if(itemStack != null && itemStack.isSimilar(item)) {
				totalAmount += itemStack.getAmount();
			}
		}
		
		return totalAmount;
	}
	
	/**
	 * @param inventory The Inventory to search in
	 * @param item The item to search for
	 * @return The free space for this item. 0 if there is none
	 */
	public static int getFreeSpaceForItem(Inventory inventory, ItemStack item) {
		int totalSpace = 0;
		for (ItemStack itemStack : inventory.getContents()) {
			if(itemStack == null || itemStack.getType() == Material.AIR) {
				totalSpace += item.getMaxStackSize();
			}
			else if(itemStack.isSimilar(item) && itemStack.getAmount() != item.getMaxStackSize()) {
				totalSpace += item.getMaxStackSize() - itemStack.getAmount();
			}
		}
		
		return totalSpace;
	}
	
	/**
	 * @param inventory The inventory to remove from
	 * @param item The item to remove 
	 * @param amount The amount of items to remove
	 * @return True if an item was removed
	 */
	public static boolean removeItems(Inventory inventory, ItemStack item, int amount) {
		if(getItemAmount(inventory, item) == 0) {
			return false;
		}
				
		int totalAmount = 0;
		Map<Integer, ItemStack> itemMap = new TreeMap<>();
		
		int counter = 0;
		for (ItemStack itemStack : inventory.getContents()) {
			if(itemStack != null && itemStack.isSimilar(item)) {
				totalAmount += itemStack.getAmount();
				itemMap.put(counter, itemStack);
			}
			counter++;
		}
				
		if(totalAmount < amount) {
			return false;
		}
		else {
			int amountToTake = amount;
			for (Entry<Integer, ItemStack> entry : itemMap.entrySet()) {
				if(entry.getValue().getAmount() < amountToTake) {
					amountToTake -= entry.getValue().getAmount();
					inventory.setItem(entry.getKey(), null);
				}
				else if(entry.getValue().getAmount() == amountToTake) {
					inventory.setItem(entry.getKey(), null);
					
					break;
				}
				else {
					ItemStack tmpItem = entry.getValue();
					tmpItem.setAmount(tmpItem.getAmount() - amountToTake);
					inventory.setItem(entry.getKey(), tmpItem);
					
					break;
				}
			}
		}
				
		return true;
	}
	
	/**
	 * @param choices A collection with all the Strings to check
	 * @param prefix The prefix the Strings should have
	 * @return A List with all the Strings having the prefix
	 */
	public static List<String> getStartingWith(Collection<String> choices, String prefix) {
		if(prefix == null || prefix.isEmpty()) {
			return new ArrayList<>(choices);
		}
		
		List<String> list = new ArrayList<>();
		
		for (String string : choices) {
			if(string.toLowerCase().startsWith(prefix.toLowerCase())) {
				list.add(string);
			}
		}
		
		return list;
	}
	
	/**
	 * Uses the default category, "Messages"
	 * 
	 * @param key The key to translate
	 * @param formattingObjects The formatting objects
	 * @return The translated String
	 */
	public static String tr(String key, Object... formattingObjects) {
		return color(IPSignShop.getInstance().getLanguage().tr(key, formattingObjects));
	}

	/**
	 * Translates an item name (uses "Items" category)
	 * 
	 * @param material The material to translate
	 * @return The translated String
	 */
	public static String trItem(Material material) {
		return color(IPSignShop.getInstance().getLanguage().translate(material.name(), "Items"));
	}
	
	/**
	 * Searches an <b>online</b> player by his name or display name
	 * 
	 * @param name The name or display name of the player
	 * @return The player if found
	 */
	public static Optional<? extends Player> getPlayerByDisplayOrName(String name) {
		return Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.getDisplayName().equalsIgnoreCase(name) || player.getName().equalsIgnoreCase(name))
				.findAny();
	}
}
