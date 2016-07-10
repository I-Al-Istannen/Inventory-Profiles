package me.ialistannen.inventory_profiles.hooks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.signs.BuySign;
import me.ialistannen.inventory_profiles.util.LocationSerializable;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * A Region hook. Provides support for Region protection Plugins
 */
public interface RegionHook extends Hook {

	/**
	 * Removes the user from all Regions
	 * 
	 * @param profile The Profile to remove
	 */
	public void removeUserFromAllRegions(Profile profile);
	
	/**
	 * Removes the user from all Regions
	 * 
	 * @param player The {@link Player} to remove
	 */
	public void removeUserFromAllRegions(Player player);

	/**
	 * Removes a user from a region
	 * 
	 * @param regionID The Id of the region. Name in worldGuard
	 * @param world The world the region is in
	 * @param profile The Profile to remove
	 */
	public void removeUserFromRegion(String regionID, World world, Profile profile);
	
	/**
	 * @param profile The Profile to remove
	 * @param regionObject The {@link RegionObject} with all the needed information
	 */
	public void removeUserFromRegion(Profile profile, RegionObject regionObject);
	
	/**
	 * Adds a user to a region
	 * 
	 * @param regionID The Id of the region. Name in worldGuard
	 * @param world The world the region is in
	 * @param profile The Profile to add
	 * @param role The Role the user should have
	 */
	public void addUserToRegion(String regionID, World world, Profile profile, RegionRole role);

	/**
	 * @param profile The Profile to add
	 * @param regionObject The {@link RegionObject} with all the needed information
	 */
	public void addUserToRegion(Profile profile, RegionObject regionObject);
	
	/**
	 * Checks if the player owns the region
	 * 
	 * @param regionID The Id of the region. Name in worldGuard
	 * @param world The world the region is in
	 * @param profile The Profile to check
	 * @return True if the player owns the region
	 */
	public boolean isOwner(String regionID, World world, Profile profile);
	
	/**
	 * The RegionRole of the player
	 * 
	 * @param regionID The Id of the region. Name in worldGuard
	 * @param world The world the region is in
	 * @param profile The Profile to check
	 * @return The Region role if the region exists and the player is listed in it. Otherwise an emtpy optional
	 */
	public Optional<RegionRole> getRegionRole(String regionID, World world, Profile profile);
	
	/**
	 * @param regionID The Id of the region. Name in worldGuard
	 * @param world The world the region is in
	 * @return True if the region exists.
	 */
	public boolean hasRegion(String regionID, World world);
	
	/**
	 * @param profile The Profile to get all regions for
	 * @param role The Role of the player
	 * @return All the regions the player, where he has the specified RegionRole
	 */
	public Collection<String> getAllRegions(Profile profile, RegionRole role);
	
	/**
	 * The role in a region.
	 */
	public enum RegionRole {
		/**
		 * Owns the region
		 */
		OWNER,
		/**
		 * is a member in the region
		 */
		MEMBER;
		
		/**
		 * @return The translated name for this region
		 */
		public String getTranslatedName() {
			return Util.tr("region role " + this.name().toLowerCase());
		}
		
		/**
		 * @param name The translated name of the region role
		 * @return The {@link RegionRole} or an empty Optional if the name is not valid
		 */
		public static Optional<RegionRole> forTranslatedName(String name) {
			if(name.equalsIgnoreCase(OWNER.getTranslatedName())) {
				return Optional.of(OWNER);
			}
			else if(name.equalsIgnoreCase(MEMBER.getTranslatedName())) {
				return Optional.of(MEMBER);
			}
			
			return Optional.empty();
		}
	}
	
	/**
	 * Represents a Region that a player owns
	 */
	public static class RegionObject implements ConfigurationSerializable {
		private String regionID;
		private World world;
		private RegionRole role;
		private double price;
		private Location signLocation;
		private BlockFace signFacingDirection;
		private boolean wallSign;
		
		/**
		 * @param regionID The Id of the region
		 * @param world The World the region is in
		 * @param role The Role the player has in the region
		 * @param price The price of the region
		 * @param signLocation The location of the region sign
		 * @param signFace The facing direction of the sign. Shouldn't be null, but the error should be corrected automatically.
		 * @param wallSign Wether the sign should be a wall sign
		 * @throws NullPointerException If the world is null.
		 */
		public RegionObject(String regionID, World world, RegionRole role, double price, Location signLocation, BlockFace signFace,
				boolean wallSign) throws NullPointerException {
			Objects.requireNonNull(world, "The world can't be null. Have you removed it or is it just not loaded?");
			
			this.regionID = regionID;
			this.world = world;
			this.role = role;
			this.price = price;
			this.signLocation = signLocation;
			this.signFacingDirection = signFace;
			this.wallSign = wallSign;
		}

		/**
		 * @param regionID The Id of the region
		 * @param world The World the region is in
		 * @param role The Role the player has in the region
		 * @throws NullPointerException If the world is null.
		 */
		public RegionObject(String regionID, World world, RegionRole role) throws NullPointerException {
			this(regionID, world, role, 0, null, null, false);
		}

		/**
		 * @param map The Map for {@link ConfigurationSerializable}
		 */
		public RegionObject(Map<String, Object> map) {
			this((String) map.get("regionID"), Bukkit.getWorld((String) map.get("world")), RegionRole.valueOf((String) map.get("role"))
					, ((Number) map.get("price")).doubleValue(), 
					map.get("signLocation") == null ? null : ((LocationSerializable) map.get("signLocation")).toLocation()
					, BlockFace.valueOf((String) map.get("signFacingDirection")),
					map.containsKey("wallSign") ? (boolean) map.get("wallSign") : false);
		}
		
		/**
		 * @return The Id of the region
		 */
		public String getRegionID() {
			return regionID;
		}
		
		/**
		 * @return The World the region is in
		 */
		public World getWorld() {
			return world;
		}
		
		/**
		 * @return The {@link RegionRole} of the player
		 */
		public RegionRole getRole() {
			return role;
		}
		
		/**
		 * The price of the region
		 * 
		 * @return The Price of the region. Maybe be 0 if the region wasn't bought
		 */
		public double getPrice() {
			return price;
		}
		
		/**
		 * The Location the old {@link BuySign} was at.
		 * 
		 * @return The Location the {@link BuySign} was at. Might be an empty optional if the user didn't buy the region.
		 */
		public Optional<Location> getSignLocation() {
			return Optional.ofNullable(signLocation);
		}
		
		/**
		 * @return The Facing Direction of the sign or an emtpy optional if it is not set
		 */
		public Optional<BlockFace> getSignFacingDirection() {
			// consider down as not valid for a sign, so that won't ever be the case
			return signFacingDirection == BlockFace.DOWN ? Optional.empty() : Optional.ofNullable(signFacingDirection);
		}
		
		/**
		 * @return True if the sign should be a wall sign
		 */
		public boolean isWallSign() {
			return wallSign;
		}
		
		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<>();
			map.put("regionID", getRegionID());
			map.put("world", getWorld().getName());
			map.put("role", getRole().name());
			map.put("price", getPrice());
			map.put("signLocation", getSignLocation().isPresent() ? new LocationSerializable(getSignLocation().get()) : null);
			map.put("signFacingDirection", signFacingDirection == null ? BlockFace.DOWN.name() : signFacingDirection.name());
			map.put("wallSign", isWallSign());
			return map;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((regionID == null) ? 0 : regionID.hashCode());
			result = prime * result + ((world == null) ? 0 : world.getUID().hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RegionObject other = (RegionObject) obj;
			if (regionID == null) {
				if (other.regionID != null)
					return false;
			} else if (!regionID.equals(other.regionID))
				return false;
			if (world == null) {
				if (other.world != null)
					return false;
			} else if (!world.getUID().equals(other.world.getUID()))
				return false;
			return true;
		}		
	}
}
