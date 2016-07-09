package me.ialistannen.inventory_profiles.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * A serializable version of the {@link Location}. Needed for < 1.8
 */
public class LocationSerializable implements ConfigurationSerializable {

	private double x,y,z;
	private float yaw, pitch;
	private String world;
	
	
	/**
	 * @param x The X - Coordinate
	 * @param y The Y - Coordinate
	 * @param z The Z - Coordinate
	 * @param yaw The Yaw
	 * @param pitch The Pitch
	 * @param world The Worldname
	 */
	public LocationSerializable(double x, double y, double z, float yaw, float pitch, String world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.world = world;
	}
	
	/**
	 * @param loc The Location to create it from
	 */
	public LocationSerializable(Location loc) {
		this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName());
	}
	
	/**
	 * @param map The {@link ConfigurationSerializable} map
	 */
	public LocationSerializable(Map<String, Object> map) {
		this((double) map.get("x"), (double) map.get("y"), (double) map.get("z"),
				((Double) map.get("yaw")).floatValue(), ((Double) map.get("pitch")).floatValue(),
				(String) map.get("world"));
	}
	
	/**
	 * @return The {@link Location} representation
	 */
	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		map.put("yaw", yaw);
		map.put("pitch", pitch);
		map.put("world", world);
		return map;
	}
	
}
