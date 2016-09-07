package me.ialistannen.ip_sign_shop.datastorage;

import me.ialistannen.inventory_profiles.util.LocationSerializable;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

/**
 * Represents a single shop
 */
public class Shop implements ConfigurationSerializable {
	
	private String owner;
	private final ItemStack item;
	private final Location signLocation;
	private final Location chestLocation;
	private ShopMode mode;
	private double price;
	private Item displayItem;
	
	/**
	 * @param owner The owner of the shop. Must be a valid profile from InventoryProfiles
	 * @param item The Item to sell
	 * @param signLocation The Location the shop sign is at.
	 * @param chestLocation The Location of the chest for this shop
	 * @param mode The mode the shop is in
	 * @param price The Price of the item
	 */
	public Shop(String owner, ItemStack item, Location signLocation, Location chestLocation, ShopMode mode, double price) {
		this.owner = owner;
		this.item = item.clone();
		this.signLocation = signLocation;
		this.chestLocation = chestLocation;
		this.mode = mode;
		this.price = price;
		
		this.item.setAmount(1);
		updateShop();
	}
	
	/**
	 * @param map The Map for {@link ConfigurationSerializable}
	 */
	public Shop(Map<String, Object> map) {
		this(
				(String) map.get("owner"),
				(ItemStack) map.get("item"),
				((LocationSerializable) map.get("signLocation")).toLocation(),
				((LocationSerializable) map.get("chestLocation")).toLocation(),
				ShopMode.valueOf(((String) map.get("mode"))),
				getDoubleFromNumberObject(map.get("price"))
			);
	}
	
	private static Double getDoubleFromNumberObject(Object obj) {
		if(obj instanceof Number) {
			Number number = (Number) obj;
			return number.doubleValue();
		}
		return null;
	}
	
	/**
	 * @return The owner
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * @return A clone of zhe itemstack this shop works with
	 */
	public ItemStack getItem() {
		return item.clone();
	}
	
	/**
	 * @return A clone of the location of the shop sign
	 */
	public Location getSignLocation() {
		return signLocation.clone();
	}
	
	/**
	 * @return The Location of the chest belongig to this shop
	 */
	public Location getChestLocation() {
		return chestLocation.clone();
	}
	
	
	/**
	 * @return The mode the shop is at
	 */
	public ShopMode getMode() {
		return mode;
	}
	
	/**
	 * @return The Price of the item
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * @return The inventory. <b>YOU SHOULD NOT MODIFY IT!</b>
	 */
	public Inventory getShopInventory() {
		return getChest().getBlockInventory();
	}

	/**
	 * @param owner The owner of the shop
	 */
	public void setOwner(String owner) {
		this.owner = owner;
		updateShop();
	}

	/**
	 * @param mode The {@link ShopMode} to set the shop to
	 */
	public void setMode(ShopMode mode) {
		this.mode = mode;
		updateShop();
	}
	
	/**
	 * @param price The new price
	 */
	public void setPrice(double price) {
		this.price = price;
		updateShop();
	}
	
	/**
	 * @param location The Location to check at
	 * @return True if the block at the givben location is this shops chest
	 */
	public boolean isYourChest(Location location) {
		return location.toVector().equals(chestLocation.toVector());
	}
	
	/**
	 * @param item The Item to check
	 * @return True if the item is the {@link #displayItem} of this shop
	 */
	public boolean isYourItem(Item item) {
		if(item.equals(displayItem)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Performs all the necessary actions to create the shop. Places the sign and changes the lines.
	 */
	public void updateShop() {
		if(!(signLocation.getBlock().getType() == Material.WALL_SIGN)) {
			signLocation.getBlock().setType(Material.WALL_SIGN);
		}
		
		Sign sign = (Sign) signLocation.getBlock().getState();
		
		String[] lines = new String[4];
		// owner, mode, item name, price
		lines[0] = tr("sign line 1", getOwner(), getMode().getShopModeName(), getItemName(), getPrice());
		lines[1] = tr("sign line 2", getOwner(), getMode().getShopModeName(), getItemName(), getPrice());
		lines[2] = tr("sign line 3", getOwner(), getMode().getShopModeName(), getItemName(), getPrice());
		lines[3] = tr("sign line 4", getOwner(), getMode().getShopModeName(), getItemName(), getPrice());

		sign.setLine(0, lines[0]);
		sign.setLine(1, lines[1]);
		sign.setLine(2, lines[2]);
		sign.setLine(3, lines[3]);
		
		org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) sign.getData();
		signMat.setFacingDirection(IPSignShopUtil.getBlockFace(signLocation.toVector().subtract(chestLocation.toVector())));
		sign.setData(signMat);
		
		sign.update();
		
		if(!IPSignShop.getInstance().getConfig().getBoolean("use item display")) {
			despawnItem();
		}
		else {
			if(displayItem == null || !displayItem.isValid()) {
				spawnItem();
			}
			despawnOtherItems();
		}
	}
	
	private void spawnItem() {
		displayItem = chestLocation.getWorld().dropItem(chestLocation.clone().add(0.5, 1, 0.5), getItem());
		displayItem.setVelocity(new Vector());
		displayItem.setPickupDelay(Integer.MAX_VALUE);
		
		if(IPSignShop.getInstance().getConfig().getBoolean("show item displayname")) {
			displayItem.setCustomName(tr("shop item name", getItemName(), getOwner(), getPrice()));
			displayItem.setCustomNameVisible(true);
		}
	}
	
	private void despawnOtherItems() {
		if(displayItem != null) {
			for (Entity entity : displayItem.getNearbyEntities(1, 1, 1)) {
				if(!(entity instanceof Item)) {
					continue;
				}
				
				Item item = (Item) entity;
				if(item.getCustomName() == null || item.equals(displayItem)) {
					continue;
				}
				if(item.getItemStack().isSimilar(getItem())) {
					item.remove();
				}
			}
		}
	}
	
	/**
	 * Despawns the item
	 */
	public void despawnItem() {
		if(displayItem != null) {
			displayItem.remove();
			despawnOtherItems();
		}
	}
	
	/**
	 * @return True if this shop has at least one item left
	 */
	public boolean hasNoItemsLeft() {
		return getItemAmount() == 0;
	}
	
	/**
	 * @param amount The amount of items to remove
	 * @return True if an item was removed
	 */
	public boolean removeItems(int amount) {
		if(hasNoItemsLeft()) {
			return false;
		}
		
		Chest chest = getChest();
		Inventory chestInv = chest.getBlockInventory();

		IPSignShopUtil.removeItems(chestInv, getItem(), amount);
		
		chest.update();
		
		return true;
	}
	
	/**
	 * @param amount The amount of items to add
	 * @return True if the items were successfully added
	 */
	public boolean addItems(int amount) {
		if(IPSignShopUtil.getFreeSpaceForItem(getShopInventory(), getItem()) < amount) {
			return false;
		}
		
		Chest chest = getChest();
		ItemStack itemToAdd = getItem();
		itemToAdd.setAmount(amount);
		chest.getBlockInventory().addItem(itemToAdd);
		
		return chest.update();
	}
	
	/**
	 * @return The amount of items in the shop
	 */
	public int getItemAmount() {
		return IPSignShopUtil.getItemAmount(getChest().getBlockInventory(), getItem());
	}
	
	private Chest getChest() {
		return (Chest) getChestLocation().getBlock().getState();
	}

	/**
	 * @return The name of the item. Either the displayname or a nice version of the Material
	 */
	public String getItemName() {	
		return getItem().hasItemMeta() && getItem().getItemMeta().hasDisplayName()
				? getItem().getItemMeta().getDisplayName()
				: trItem(getItem().getType());
	}
	
	/**
	 * Deletes the shop. Removes the item and sign
	 */
	public void delete() {
		despawnItem();
		signLocation.getBlock().setType(Material.AIR);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("owner", owner);
		map.put("mode", mode.name());
		map.put("price", price);
		map.put("signLocation", new LocationSerializable(signLocation));
		map.put("chestLocation", new LocationSerializable(chestLocation));
		map.put("item", item);
		return map;
	}
}
