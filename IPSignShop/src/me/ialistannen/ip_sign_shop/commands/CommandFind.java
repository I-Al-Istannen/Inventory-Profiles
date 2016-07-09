package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.Language.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import me.ialistannen.ip_sign_shop.util.Language;

/**
 * Find the nearest shop selling the item
 */
public class CommandFind extends CommandPreset {

	/**
	 * Constructs an instance
	 */
	public CommandFind() {
		super("", true);
	}

	@Override
	public List<String> getTabCompletionChoices(int index, String[] message) {
		List<String> list = new ArrayList<>();
		if(index == 0) {
			list.addAll(getMaterialNames());
		}
		return list;
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length < 1) {
			return false;
		}
		
		Player player = (Player) sender;
		
		List<String> choices = IPSignShopUtil.getStartingWith(getMaterialNames(), clean(args[0]));
		
		Collections.sort(choices);
		
		if(choices.isEmpty()) {
			player.sendMessage(tr(getIndentifier() + " material unknown", args[0]));
			return true;
		}
		
		boolean ignoreEmtpy = IPSignShop.getInstance().getConfig().getBoolean("find command ignore empty shops");
		
		// the for loop to find any shop with a matching material. If there is a shop for a Diamond Hoe, but not for an Axe, /find diamond should find it
		for (String string : choices) {
			Material material = getFromTranslation(string);
			Shop nearestShop = null;
			double nearestDistance = Double.MAX_VALUE;
			for (Shop shop : IPSignShop.getShopManager().getShopsForMaterial(material)) {
				if(!shop.getSignLocation().getWorld().equals(player.getWorld())) {
					continue;
				}
				
				if(ignoreEmtpy && !shop.hasItemsLeft()) {
					continue;
				}
				
				double distance = shop.getChestLocation().distance(player.getLocation());
				if(distance < nearestDistance) {
					nearestDistance = distance;
					nearestShop = shop;
				}
			}
			
			if(nearestShop == null) {
				continue;
			}
			
			Vector direction = nearestShop.getChestLocation().add(0.5, -1, 0.5).toVector().subtract(player.getLocation().toVector()).normalize();
			Location teleportTo = player.getLocation();
			teleportTo.setDirection(direction);
			
			player.teleport(teleportTo);
			
			player.sendMessage(tr(getIndentifier() + " found shop", nearestShop.getOwner(), nearestDistance,
					nearestShop.getItemName(), nearestShop.getItemAmount(), nearestShop.getPrice()));
			
			return true;
		}
		
		player.sendMessage(tr(getIndentifier() + " no shop found", args[0]));
		
		return true;
	}

	private List<String> getMaterialNames() {
		List<String> list = new ArrayList<>();
		for(Material mat : Material.values()) {
			list.add(clean(Language.translateItemName(mat)));
		}
		return list;
	}
	
	/**
	 * Horrible inefficient. I should probably die from shame, but it works
	 * 
	 * @param translated The Translated name
	 * @return The Corresponding Material
	 */
	private Material getFromTranslation(String translated) {
		for (Material material : Material.values()) {
			String trans = Language.translateItemName(material);
			if(clean(trans).equals(clean(translated))) {
				return material;
			}
		}
		return Material.AIR;
	}
	
	/**
	 * Rudimentary cleans the String so that a search will work
	 * 
	 * @param input The String to clean
	 * @return The cleaned String
	 */
	private String clean(String input) {
		return input.toLowerCase().replace(" ", "_").trim();
	}
}
