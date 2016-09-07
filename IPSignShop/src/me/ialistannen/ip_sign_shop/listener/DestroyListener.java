package me.ialistannen.ip_sign_shop.listener;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

import java.util.Iterator;

/**
 * Prevents the chest from being destroyed
 */
public class DestroyListener implements Listener {

	/**
	 * Dunno what this event is for
	 * 
	 * @param e The {@link BlockExplodeEvent}
	 */
	@EventHandler
	public void onExplosion(BlockExplodeEvent e) {
		if(shouldBeCancelledBlock(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Checks for tnt and so on
	 * 
	 * @param e The {@link EntityExplodeEvent}
	 */
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {
		for (Iterator<Block> iterator = e.blockList().iterator(); iterator.hasNext();) {
			Block block = iterator.next();
			
			if(shouldBeCancelledBlock(block.getLocation())) {
				iterator.remove();
			}	
		}
	}
	
	/**
	 * Called when a player tries to destroy it
	 * 
	 * @param e The {@link BlockBreakEvent}
	 */
	@EventHandler
	public void onPlayerDestroy(BlockBreakEvent e) {
		if(shouldBeCancelledBlock(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Prevent the item from being exploded
	 * 
	 * @param e The {@link EntityDamageByEntityEvent}
	 */
	@EventHandler
	public void onItemDamageByEntity(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof Item)) {
			return;
		}
		
		if(shouldBeCancelledItem((Item) e.getEntity())) {
			e.setCancelled(true);
		}
	}

	/**
	 * Prevent the item from being destroyed by a block
	 * 
	 * @param e The {@link EntityDamageByEntityEvent}
	 */
	@EventHandler
	public void onItemDamageByBlock(EntityDamageByBlockEvent e) {
		if(!(e.getEntity() instanceof Item)) {
			return;
		}
		
		if(shouldBeCancelledItem((Item) e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Prevents placing a block on the chest and moving the preview item
	 * 
	 * @param e THe {@link BlockPlaceEvent}
	 */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(shouldBeCancelledBlock(e.getBlockPlaced().getLocation().subtract(0, 1, 0))) {
			e.setCancelled(true);
		}
	}
	
	
	/**
	 * Checks if the location is a part of a shop
	 * 
	 * @param loc The location to check at. A BlockLocation, meaning that YAW and PITCH are 0
	 * @return True if the event should be cancelled
	 */
	private boolean shouldBeCancelledBlock(Location loc) {
		if(IPSignShop.getShopManager().isNoShopSign(loc) && !IPSignShop.getShopManager().isShopChest(loc)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param item The item to check
	 * @return True if the event should be cancelled
	 */
	private boolean shouldBeCancelledItem(Item item) {
		if(IPSignShop.getShopManager().isShopDisplayItem(item)) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Prevents the item from despawning
	 * 
	 * @param e The {@link ItemDespawnEvent}
	 */
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		for (Shop shop : IPSignShop.getShopManager().getAllShops()) {
			if(shop.isYourItem(e.getEntity())) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
	/**
	 * Prevents the item from burning or being exploded
	 * 
	 * @param e The {@link EntityDamageEvent}
	 */
	@EventHandler
	public void onItemDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Item)) {
			return;
		}
		
		for (Shop shop : IPSignShop.getShopManager().getAllShops()) {
			if(shop.isYourItem((Item) e.getEntity())) {
				e.setCancelled(true);
				return;
			}
		}
	}
}
