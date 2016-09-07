package me.ialistannen.inventory_profiles.hooks;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.ialistannen.inventory_profiles.players.Profile;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A hook for WorldGuard
 */
class WorldGuardHook implements RegionHook {

	private final WorldGuardPlugin worldguard;
	private String errorMessage;

	/**
	 * Creates a new Worldguard hook. Only in this package!
	 */
	WorldGuardHook() {
		worldguard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (worldguard == null) {
			errorMessage = "World Guard Plugin not found!";
		}
	}

	@Override
	public boolean isWorking() {
		return worldguard != null;
	}

	@Override
	public String getName() {
		return "WorldGUard";
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void removeUserFromAllRegions(Profile profile) {
		profile.getPlayer().ifPresent(this::removeUserFromAllRegions);
	}

	@Override
	public void removeUserFromAllRegions(Player player) {
		for (World world : Bukkit.getWorlds()) {
			RegionManager regionManager = worldguard.getRegionManager(world);
			if (regionManager == null) {
				continue;
			}

			regionManager.getRegions().values().stream().filter(Objects::nonNull).forEach(region -> {
				region.getOwners().removePlayer(worldguard.wrapPlayer(player));
				region.getMembers().removePlayer(worldguard.wrapPlayer(player));
			});
		}
	}

	@Override
	public void removeUserFromRegion(String regionID, World world, Profile profile) {
		if (hasNoRegion(regionID, world)) {
			return;
		}
		profile.getPlayer().ifPresent(player -> {
			ProtectedRegion region = worldguard.getRegionManager(world).getRegion(regionID);
			assert region != null;
			region.getMembers().removePlayer(worldguard.wrapPlayer(player));
			region.getOwners().removePlayer(worldguard.wrapPlayer(player));
		});
	}

	@Override
	public void removeUserFromRegion(Profile profile, RegionObject regionObject) {
		removeUserFromRegion(regionObject.getRegionID(), regionObject.getWorld(), profile);
	}

	@Override
	public void addUserToRegion(String regionID, World world, Profile profile, RegionRole role) {
		profile.getPlayer().ifPresent(player -> {
			RegionManager regionManager = worldguard.getRegionManager(world);
			if (hasNoRegion(regionID, world)) {
				return;
			}
			ProtectedRegion region = regionManager.getRegion(regionID);
			if (region == null) {
				return;
			}
			if (role == RegionRole.MEMBER) {
				region.getMembers().addPlayer(worldguard.wrapPlayer(player));
			}
			else if (role == RegionRole.OWNER) {
				region.getOwners().addPlayer(worldguard.wrapPlayer(player));
			}
		});
	}

	@Override
	public void addUserToRegion(Profile profile, RegionObject regionObject) {
		addUserToRegion(regionObject.getRegionID(), regionObject.getWorld(), profile, regionObject.getRole());
	}

	@Override
	public Optional<RegionRole> getRegionRole(String regionID, World world, Profile profile) {
		if (!profile.getPlayer().isPresent() || hasNoRegion(regionID, world)) {
			return Optional.empty();
		}

		@SuppressWarnings("OptionalGetWithoutIsPresent") // is checked above.
				LocalPlayer player = worldguard.wrapPlayer(profile.getPlayer().get());

		ProtectedRegion region = worldguard.getRegionManager(world).getRegion(regionID);
		if (region == null) {
			return Optional.empty();
		}
		if (region.getOwners().contains(player)) {
			return Optional.of(RegionRole.OWNER);
		}
		else if (region.getMembers().contains(player)) {
			return Optional.of(RegionRole.MEMBER);
		}
		return Optional.empty();
	}

	@Override
	public boolean hasNoRegion(String regionID, World world) {
		RegionManager regionManager = worldguard.getRegionManager(world);
		return regionManager == null || !regionManager.hasRegion(regionID);
	}

	@Override
	public boolean isOwner(String regionID, World world, Profile profile) {
		Optional<RegionRole> role = getRegionRole(regionID, world, profile);
		return role.isPresent() && role.get() == RegionRole.OWNER;
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent") // "profile).get() == role" is checked before
	@Override
	public Collection<String> getAllRegions(Profile profile, RegionRole role) {
		List<String> regions = new ArrayList<>();
		for (World world : Bukkit.getWorlds()) {
			RegionManager regionManager = worldguard.getRegionManager(world);
			if (regionManager == null) {
				continue;
			}

			regionManager.getRegions().entrySet().stream()
					.filter(entry ->
							getRegionRole(entry.getKey(), world, profile).isPresent()
									&& getRegionRole(entry.getKey(), world, profile).get() == role
					)
					.map(Map.Entry::getKey)
					.forEach(regions::add);
		}

		return regions;
	}

}
