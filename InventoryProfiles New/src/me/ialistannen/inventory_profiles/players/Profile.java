package me.ialistannen.inventory_profiles.players;

import static me.ialistannen.inventory_profiles.language.IPLanguage.tr;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.util.ExpUtil;
import me.ialistannen.inventory_profiles.util.LocationSerializable;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * Holds information about a player
 */
public class Profile implements ConfigurationSerializable {

	private final String name;
	private String password;
	private String banReason;
	private LocalDateTime bannedUntil;
	private long usedPlaytime;
	private long playtimeModifier;
	private LocalDate playtimeModifierRunsOut;
	private LocalDateTime playtimeWentOut;
	private boolean op;
	private double money;
	private Location lastLogoutLocation;
	private List<ItemStack> items;
	private Set<RegionObject> regionObjects;
	private int xp;
	private GameMode gameMode;
	private Location home;
	
	/**
	 * @param name The name of the profile
	 * @param password The password of the profile
	 * @param banReason The reason the profile is banned.
	 * @param bannedUntil Until when the player is banned
	 * @param op If the player is op
	 * @param money The money of the player
	 * @param lastLogoutLocation The location the player was at when he logged out
	 * @param items The items the player had
	 * @param xp The Experience the player had upon logging out.
	 * @param regionObjects The Region objects of this player
	 * @param gameMode The gamemode of the player
	 * @param playtimeModifier The playtime modifier in ms. > 0 for more, < 0 for less
	 * @param playtimeModifierRunsOut The date the playtime modifier runs out
	 * @param home The home location. May be null to indicate no home set
	 */
	public Profile(String name, String password, String banReason, LocalDateTime bannedUntil, boolean op,
			double money, Location lastLogoutLocation, List<ItemStack> items, int xp, Set<RegionObject> regionObjects, GameMode gameMode
			, long playtimeModifier, LocalDate playtimeModifierRunsOut, Location home) {
		this.name = name;
		this.password = password;
		this.banReason = banReason;
		this.bannedUntil = bannedUntil;
		this.op = op;
		this.money = money;
		this.lastLogoutLocation = lastLogoutLocation;
		this.items = items;
		this.xp = xp;
		this.regionObjects = regionObjects;
		this.gameMode = gameMode;
		this.home = home;
		
		if(LocalDate.now().isAfter(playtimeModifierRunsOut)) {
			setPlaytimeModifier(0);
			setPlaytimeModifierRunsOut(LocalDate.now());
		}
		else {
			this.playtimeModifier = playtimeModifier;
			this.playtimeModifierRunsOut = playtimeModifierRunsOut;
		}
	}
	
	/**
	 * @param map The Map for {@link ConfigurationSerializable}
	 */
	@SuppressWarnings("unchecked")
	public Profile(Map<String, Object> map) {
		this((String) map.get("name"),
				(String) map.get("password"), 
				(String) map.get("banReason"),
				LocalDateTime.ofEpochSecond(((Number) map.get("bannedUntil")).longValue(), 0, ZoneOffset.UTC), 
				(Boolean) map.get("op"),
				((Number) map.get("money")).doubleValue(), 
				((LocationSerializable) map.get("logoutLocation")).toLocation(),
				(List<ItemStack>) map.get("items"), 
				((Number) map.get("xp")).intValue(),
				(Set<RegionObject>) map.get("regionObjects"),
				map.containsKey("gameMode") ? GameMode.valueOf((String) map.get("gameMode")) : GameMode.SURVIVAL,
				map.containsKey("playtimeModifier") ? ((Number) map.get("playtimeModifier")).longValue() : 0L,
				map.containsKey("playtimeModifierRunsOut") ? LocalDate.ofEpochDay(((Number) map.get("playtimeModifierRunsOut")).longValue()) : LocalDate.now(),
				map.containsKey("home") ? ((LocationSerializable) map.get("home")).toLocation() : null);
	}
	
	/**
	 * @param name The name of the player
	 * @param password The password of the player
	 * @param op If the player is op
	 * @param world The world the player should spawn in
	 */
	public Profile(String name, String password, boolean op, World world) {
		this(name, password, "", LocalDateTime.now(), op, 0, world.getSpawnLocation(), new ArrayList<ItemStack>(), 0,
				new HashSet<>(), GameMode.SURVIVAL, 0, LocalDate.now(), null);
	}

	/**
	 * @param player The {@link Player} to apply it to.
	 */
	public void applyToPlayer(Player player) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				player.setDisplayName(Util.trimToSize(getName(), 16));
				player.setPlayerListName(Util.trimToSize(getName(), 16));
				
				ExpUtil.setXp(player, getXp());
				player.setOp(isOp());
				player.sendMessage(tr("login complete", getName()));
				
				if(isOp()) {
					player.sendMessage(tr("admin logged in", player.getDisplayName()));
				}
				
				for (int i = 0; i < getItems().size(); i++) {
					ItemStack itemStack = getItems().get(i);
					player.getInventory().setItem(i, itemStack);
				}
				
				if(!getItems().isEmpty()) {
					player.getInventory().setArmorContents(getItems().subList(getItems().size() - 4, getItems().size()).toArray(new ItemStack[4]));
				}
				
				if(InventoryProfiles.getInstance().getConfig().getBoolean("teleport to old location upon login")) {
					player.teleport(getLastLogoutLocation());
				}
				else {
					player.teleport(getLastLogoutLocation().getWorld().getSpawnLocation());
				}
				
				if(InventoryProfiles.hasMoneyHook()) {
					InventoryProfiles.getMoneyHook().setMoney(player, getMoney(false));
				}
				
				player.setGameMode(getGameMode());
				
				if(!hasPlaytimeLeft() && Util.getPlaytimeResetDelay().isPresent()) {
					if(ChronoUnit.SECONDS.between(getPlaytimeWentOut().get(), LocalDateTime.now()) >= Util.getPlaytimeResetDelay().get().getSeconds()) {
						setUsedPlaytime(0);
						setPlaytimeWentOut(null);
					}
				}
				
				// Add the user to all regions
				getRegionObjects().forEach(regionObject -> {
					if(InventoryProfiles.hasRegionHook()) {
						InventoryProfiles.getRegionHook().addUserToRegion(Profile.this, regionObject);
					}
				});
				
				if(InventoryProfiles.getInstance().getConfig().getBoolean("log logins and logouts")) {
					InventoryProfiles.getInstance().getLogger().info(tr("player logged in console message", getName(), isBanned(), isOp()));
				}				
			}
		}.runTask(InventoryProfiles.getInstance());
	}
	
	/**
	 * @return True if the player is banned
	 */
	public boolean isBanned() {
		return getBannedUntil().isAfter(LocalDateTime.now());
	}
	
	/**
	 * Returns true if {@link #isOp()} returns true
	 * 
	 * @return True if the player has playtime left.
	 */
	public boolean hasPlaytimeLeft() {
		if(isOp()) {
			return true;
		}
				
		return !getPlaytimeLeft().isNegative();
	}
	
	/**
	 * INCLUDES the {@link #getPlaytimeModifier()}!
	 * 
	 * @return The playtime the user has left.
	 */
	public Duration getPlaytimeLeft() {
		if(isOp()) {
			return Duration.ofDays(1);
		}
		
		Optional<Duration> maxPlayTime = getMaxPlaytime();
		if(!maxPlayTime.isPresent()) {
			return Duration.ofDays(1);
		}
		
		// add the playtime modifier and then subtract the current playtime
		return Duration.of((getPlaytimeModifier() + maxPlayTime.get().toMillis()) - getUsedPlaytime(), ChronoUnit.MILLIS);
	}
	
	private Optional<Duration> getMaxPlaytime() {
		Optional<Duration> maxPlayTime = Util.parseDurationString(InventoryProfiles.getInstance().getConfig().getString("max playtime"));
		if(!maxPlayTime.isPresent()) {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, "The max playtime is invalid: "
					+ InventoryProfiles.getInstance().getConfig().getString("max playtime"));
		}
		
		return maxPlayTime;
	}
	
	/**
	 * @param reason The reason of the ban
	 * @param duration The duration of the ban
	 */
	public void ban(String reason, Duration duration) {
		setBannedUntil(LocalDateTime.now().plus(duration));
		setBanReason(reason);
	}
	
	/**
	 * Unbans a player
	 */
	public void unban() {
		setBannedUntil(LocalDateTime.now());
		setBanReason("");
	}
	
	/**
	 * @param time The time the user should have
	 */
	public void setAvaillablePlaytime(Duration time) {
		Optional<Duration> maxPlayTime = getMaxPlaytime();
		maxPlayTime.ifPresent(maxTime -> {
			setUsedPlaytime(maxPlayTime.get().toMillis() - time.toMillis());
		});
	}
	
	/**
	 * @param regionObject The {@link RegionObject} to add to this profile
	 */
	public void addRegionObject(RegionObject regionObject) {
		regionObjects.add(regionObject);
		if(InventoryProfiles.hasRegionHook()) {
			InventoryProfiles.getRegionHook().addUserToRegion(this, regionObject);
		}
	}
	
	/**
	 * @param regionObject The {@link RegionObject} to remove from this profile
	 */
	public void removeRegionObject(RegionObject regionObject) {
		regionObjects.remove(regionObject);
		if(InventoryProfiles.hasRegionHook()) {
			InventoryProfiles.getRegionHook().removeUserFromRegion(this, regionObject);
		}
	}
	
	/**
	 * Removes the user from all regions
	 */
	public void removeFromAllRegions() {
		if (InventoryProfiles.hasRegionHook()) {
			InventoryProfiles.getRegionHook().removeUserFromAllRegions(this);
		}
	}
	
	/**
	 * @param playtimeModifier The Playtime modifier in Milliseconds. > 0 ==> more, < 0 ==> Less
	 * @param runsOutAt The Date the modifier runs out at. Negative will be converted to 0. 0 is today, 1 is today and tomorrow
	 */
	public void setPlaytimeModifier(long playtimeModifier, LocalDate runsOutAt) {
		if(ChronoUnit.DAYS.between(LocalDate.now(), runsOutAt) < 0) {
			setPlaytimeModifier(0);
			setPlaytimeModifierRunsOut(LocalDate.now());
		}
		else {
			setPlaytimeModifier(playtimeModifier);
			setPlaytimeModifierRunsOut(runsOutAt);
		}
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the ban reason
	 */
	public String getBanReason() {
		return banReason;
	}
	/**
	 * @param banReason the ban reason
	 */
	public void setBanReason(String banReason) {
		this.banReason = banReason;
	}
	/**
	 * @return Until when he is banned
	 */
	public LocalDateTime getBannedUntil() {
		return bannedUntil;
	}
	/**
	 * @param bannedUntil Until when he is banned
	 */
	public void setBannedUntil(LocalDateTime bannedUntil) {
		this.bannedUntil = bannedUntil;
	}
	/**
	 * Returns 0 if {@link #isOp()} returns true
	 * 
	 * @return the usedPlaytime
	 */
	public long getUsedPlaytime() {
		return isOp() ? 0 : usedPlaytime;
	}
	/**
	 * @param usedPlaytime The playtime he used
	 */
	public void setUsedPlaytime(long usedPlaytime) {
		this.usedPlaytime = usedPlaytime;
	}
	/**
	 * @return The time the playtime went out or an empty optional if it didn't
	 */
	public Optional<LocalDateTime> getPlaytimeWentOut() {
		return Optional.ofNullable(playtimeWentOut);
	}
	/**
	 * @param playtimeWentOut The time the playtime went out
	 */
	public void setPlaytimeWentOut(LocalDateTime playtimeWentOut) {
		this.playtimeWentOut = playtimeWentOut;
	}
	/**
	 * @param fromEconomy If true the money will be fetched from the economy system if possible.
	 * @return the money of the player
	 */
	public double getMoney(boolean fromEconomy) {	
		if(fromEconomy && InventoryProfiles.hasMoneyHook()) {
			Optional<Player> player = getPlayer();
			if(player.isPresent()) {
				return InventoryProfiles.getMoneyHook().getBalance(player.get());
			}
		}
		
		return money;
	}
	/**
	 * @param money the money of the player
	 */
	public void setMoney(double money) {
		this.money = money;
		if(InventoryProfiles.hasMoneyHook()) {
			getPlayer().ifPresent(player -> InventoryProfiles.getMoneyHook().setMoney(player, money));
		}
	}
	/**
	 * @return the Location the player was at when he logged out
	 */
	public Location getLastLogoutLocation() {
		return lastLogoutLocation.clone();
	}
	/**
	 * @param lastLogoutLocation the Location the player was at when he logged out
	 */
	public void setLastLogoutLocation(Location lastLogoutLocation) {
		this.lastLogoutLocation = lastLogoutLocation.clone();
	}
	/**
	 * @return the items of the player
	 */
	public List<ItemStack> getItems() {
		return items;
	}
	/**
	 * @param items the items the player has
	 */
	public void setItems(List<ItemStack> items) {
		this.items = items;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return If the player is op
	 */
	public boolean isOp() {
		return op;
	}
	
	/**
	 * @return the xp the player has
	 */
	public int getXp() {
		return xp;
	}
	/**
	 * @param xp the xp to set
	 */
	public void setXp(int xp) {
		this.xp = xp;
	}
	/**
	 * @return All the Region objects. An Unmodifiable list
	 */
	public Set<RegionObject> getRegionObjects() {
		return Collections.unmodifiableSet(regionObjects);
	}
	/**
	 * @param regionObjects The Region objects
	 */
	public void setRegionObjects(Set<RegionObject> regionObjects) {
		this.regionObjects = regionObjects;
	}
	/**
	 * @return The {@link GameMode} of the player
	 */
	public GameMode getGameMode() {
		return gameMode;
	}
	/**
	 * @param gameMode The {@link GameMode} of the player
	 */
	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}
	/**
	 * @return The playtimemodifier in Milliseconds
	 */
	public long getPlaytimeModifier() {
		return playtimeModifier;
	}
	/**
	 * You should also set the {@link #setPlaytimeModifierRunsOut(LocalDate)} or use the convenient method {@link #setPlaytimeModifierRunsOut(LocalDate)}
	 * 
	 * @param playtimeModifier The Playtime modifier in Milliseconds
	 */
	public void setPlaytimeModifier(long playtimeModifier) {
		this.playtimeModifier = playtimeModifier;
	}
	/**
	 * @return The date the playtime modifier was given
	 */
	public LocalDate getPlaytimeModifierRunsOut() {
		return playtimeModifierRunsOut;
	}
	/**
	 * You should also set the {@link #setPlaytimeModifier(long)} or use the convenient method {@link #setPlaytimeModifierRunsOut(LocalDate)}
	 * 
	 * @param playtimeModifierRunsOut The time the Playtime modifer runs out
	 */
	public void setPlaytimeModifierRunsOut(LocalDate playtimeModifierRunsOut) {
		this.playtimeModifierRunsOut = playtimeModifierRunsOut;
	}
	/**
	 * @return The home or an empty Optional if not set
	 */
	public Optional<Location> getHome() {
		if(home == null) {
			return Optional.empty();
		}
		return Optional.of(home.clone());
	}
	/**
	 * @param home The new home
	 */
	public void setHome(Location home) {
		this.home = home.clone();
	}
	
	/**
	 * @return The player from this profile
	 */
	public Optional<Player> getPlayer() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if(player.getDisplayName().equals(getName())) {
				return Optional.of(player);
			}
		}
		
		return Optional.empty();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", getName());
		map.put("password", getPassword());
		map.put("banReason", getBanReason());		
		map.put("bannedUntil", getBannedUntil().toEpochSecond(ZoneOffset.UTC));
		map.put("op", isOp());
		map.put("money", getMoney(true));
		map.put("logoutLocation", new LocationSerializable(getLastLogoutLocation()));
		map.put("items", getItems());
		map.put("xp", getXp());
		map.put("regionObjects", getRegionObjects());
		map.put("gameMode", getGameMode().name());
		map.put("playtimeModifier", getPlaytimeModifier());
		map.put("playtimeModifierRunsOut", getPlaytimeModifierRunsOut().toEpochDay());
		if(home != null) {
			map.put("home", new LocationSerializable(home));
		}
		return map;
	}
}
