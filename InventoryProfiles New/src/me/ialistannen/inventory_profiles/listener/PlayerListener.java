package me.ialistannen.inventory_profiles.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import me.ialistannen.inventory_profiles.InventoryProfiles;

/**
 * The Player listener
 */
public class PlayerListener implements Listener {
	
	/**
	 * The Frozen key for the metadata
	 */
	public static final String FROZEN_KEY = "not logged in frozen player";
	
	/**
	 * Listens for player join and logs them in
	 * 
	 * @param e The {@link PlayerJoinEvent}
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		InventoryProfiles.getProfileManager().handlePlayerJoin(e.getPlayer());
	}
	
	/**
	 * Cancels the conversation and unfreezes the player
	 * 
	 * @param e The {@link PlayerQuitEvent}
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		InventoryProfiles.getProfileManager().handlePlayerQuit(e.getPlayer());
	}
	
	/**
	 * Prevent the player from interacting while not logged in
	 * 
	 * @param e The {@link PlayerInteractEvent}
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(isFrozen(e.getPlayer())) {
			e.setCancelled(true);
		}		
	}
	
	/**
	 * Prevent the player from moving when he isn't logged in
	 * 
	 * @param e The Player move event
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(isFrozen(e.getPlayer()) && !e.getFrom().toVector().equals(e.getTo().toVector())) {
			e.getPlayer().teleport(e.getFrom());
		}
	}
	
	/**
	 * Prevent the player from Opening an inventory while not logged in
	 * 
	 * @param e The {@link InventoryOpenEvent}
	 */
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if(!(e.getPlayer() instanceof Player)) {
			return;
		}
		
		if(isFrozen((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Prevent the player from using any inventory while frozen
	 * 
	 * @param e The {@link InventoryClickEvent}
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		
		if(isFrozen((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Prevent the player from Dropping items while frozen
	 * 
	 * @param e The {@link PlayerDropItemEvent}
	 */ 
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if(isFrozen(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Prevent the player from picking up items while frozen
	 * 
	 * @param e The {@link PlayerPickupItemEvent}
	 */
	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		if(isFrozen(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * @param player The Player to check
	 * @return True if the player should be able to move
	 */
	public static boolean isFrozen(Player player) {
		return player.hasMetadata(FROZEN_KEY);
	}
	
	/**
	 * @param player The Player
	 * @param frozen If the player should be frozen or not
	 */
	public static void setFrozen(Player player, boolean frozen) {
		if(!frozen) {
			player.removeMetadata(FROZEN_KEY, InventoryProfiles.getInstance());
		}
		else {
			player.setMetadata(FROZEN_KEY, new FixedMetadataValue(InventoryProfiles.getInstance(), true));
		}
	}
}
