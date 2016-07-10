package me.ialistannen.inventory_profiles.signs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.util.LocationSerializable;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * A Sign to buy a region
 */
public class BuySign implements ConfigurationSerializable {

	private final Location location;
	private final String regionID;
	private final double price;
	private BlockFace facingDirection;
	private boolean wallSign;
	
	/**
	 * The signs will be placed and the previous block <b>replaced</b>
	 * <br>The Region won't be validated
	 * 
	 * @param location The location of the sign
	 * @param regionID The ID of the region
	 * @param price The price of the sign
	 * @param wallSign If the block is a wall sign
	 */
	public BuySign(Location location, String regionID, double price, boolean wallSign) {
		this.location = location;
		this.regionID = regionID;
		this.price = price;
		this.wallSign = wallSign;
		update();
	}
	
	/**
	 * The signs will be placed and the previous block <b>replaced</b>
	 * <br>The Region won't be validated
	 * 
	 * @param location The location of the sign
	 * @param regionID The ID of the region
	 * @param price The price of the sign
	 * @param wallSign If the block is a wall sign
	 * @param initialFacingDirection The direction the sign should face at.
	 */
	public BuySign(Location location, String regionID, double price, boolean wallSign, BlockFace initialFacingDirection) {
		this(location, regionID, price, wallSign);
		this.facingDirection = initialFacingDirection;
		// update a second time. Not nice, but hey, what do you want to do. (total will be THREE TIMES)
		Bukkit.getScheduler().runTaskLater(InventoryProfiles.getInstance(), () -> update(), 1);
	}
		
	/**
	 * @param map The Map for {@link ConfigurationSerializable}
	 */
	public BuySign(Map<String, Object> map) {
		this(((LocationSerializable) map.get("location")).toLocation(), (String) map.get("regionID"), ((Number) map.get("price")).doubleValue(),
				map.containsKey("wallSign") ? (boolean) map.get("wallSign") : false,
				map.containsKey("facingDirection") ? BlockFace.valueOf((String) map.get("facingDirection")) : BlockFace.SOUTH);
	}
	
	/**
	 * Places the sign, <b>replacing</b> the existing block and updates the sign text.
	 */
	public void update() {
		// if it is not placed or it should be a wall sign
		if((location.getBlock().getType() != Material.SIGN_POST && location.getBlock().getType() != Material.WALL_SIGN)
				|| (isWallSign() && location.getBlock().getType() != Material.WALL_SIGN)) {
			
			if(isWallSign()) {
				// prevent the first, pointless checking caused by the RegionObject creating a new sign with the second constructor.
				// Wait for the one where the facing is set.
				if(facingDirection == null) {
					return;
				}

				Block supporter = getLocation().getBlock().getRelative(facingDirection.getOppositeFace());
				if(supporter.getType() == Material.AIR) {
					supporter.setType(Material.STONE);
				}
				location.getBlock().setType(Material.WALL_SIGN);
			}
			else {
				Block supporter = getLocation().getBlock().getRelative(BlockFace.DOWN);
				if(supporter.getType() == Material.AIR) {
					supporter.setType(Material.STONE);
				}
				location.getBlock().setType(Material.SIGN_POST);
			}
		}
				
		Sign sign = (Sign) location.getBlock().getState();
		if(facingDirection != null) {
			org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();
			signData.setFacingDirection(facingDirection);
			sign.setData(signData);
		}
		
		for(int i = 0; i < 4; i++) {
			sign.setLine(i, getLine(i));
		}
		
		Bukkit.getScheduler().runTask(InventoryProfiles.getInstance(), () -> sign.update(true));
	}
	
	/**
	 * @param index The index of the line (0-3)
	 * @return The line from the language system, placeholder replaced
	 */
	private String getLine(int index) {
		if(index < 0 || index > 3) {
			return "INDEX TOO HIGH";
		}
		
		return Util.tr("buy sign line " + (index + 1), getRegionID(), getPrice());
	}

	/**
	 * Replaces the sign with {@link Material#AIR}, deleting it.
	 */
	public void delete() {
		getLocation().getBlock().setType(Material.AIR);
	}
	
	/**
	 * The sign location
	 * 
	 * @return The Location the sign is at
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * Retunrs the ID of the region this sign sells
	 * 
	 * @return The ID of the region
	 */
	public String getRegionID() {
		return regionID;
	}
	
	/**
	 * Returns the price of the Region
	 * 
	 * @return The Price if the Region
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * @return True if this should be a wall sign
	 */
	public boolean isWallSign() {
		return wallSign;
	}
	
	/**
	 * Returns the facing direction of the sign. Must be called before the delete() method
	 * 
	 * @return The {@link BlockFace} of this sign or an empty optional if the sign no longer exists
	 */
	public Optional<BlockFace> getBlockFace() {
		if(location.getBlock().getType() != Material.SIGN_POST && location.getBlock().getType() != Material.WALL_SIGN) {
			return Optional.empty();
		}
		return Optional.of(((org.bukkit.material.Sign) ((Sign) location.getBlock().getState()).getData()).getFacing());
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("regionID", getRegionID());
		map.put("price", getPrice());
		map.put("location", new LocationSerializable(getLocation()));
		if(facingDirection != null) {
			map.put("facingDirection", facingDirection.name());
		}
		map.put("wallSign", isWallSign());
		return map;
	}
}
