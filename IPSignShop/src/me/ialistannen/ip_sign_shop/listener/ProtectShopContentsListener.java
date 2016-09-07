package me.ialistannen.ip_sign_shop.listener;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;

/**
 * Protects the contents of the chest
 */
public class ProtectShopContentsListener implements Listener {

	/**
	 * Prevent the players from opening the chest
	 * 
	 * @param e The {@link PlayerInteractEvent}
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasBlock()) {
			return;
		}
		
		if(e.getPlayer().hasPermission(IPSignShopUtil.PERMISSION_PREFIX + " open every chest")) {
			return;
		}
		
		if(!IPSignShop.getShopManager().isShopChest(e.getClickedBlock().getLocation())) {
			return;
		}
		
		Shop shop = IPSignShop.getShopManager().getShopForChestLocation(e.getClickedBlock().getLocation());
		
		if(shop == null) {
			return;
		}
		
		// is the owner
		if(shop.getOwner().equals(e.getPlayer().getDisplayName())) {
			return;
		}
				
		// is some other person
		e.setCancelled(true);
	}
	
	/**
	 * @param e The {@link InventoryClickEvent}
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory().getType() != InventoryType.CHEST) {
			return;
		}
		
		Shop shop = IPSignShop.getShopManager().getShopForInventory(e.getInventory());
		
		
		if(shop == null) {
			return;
		}
		
		switch(e.getAction()) {
		case CLONE_STACK:
		case COLLECT_TO_CURSOR:
		case DROP_ALL_CURSOR:
		case DROP_ALL_SLOT:
		case DROP_ONE_CURSOR:
		case DROP_ONE_SLOT:
			return;
			
		case HOTBAR_MOVE_AND_READD:
		case HOTBAR_SWAP: {
			e.setCancelled(true);
			e.getWhoClicked().sendMessage(tr("shop inventory number keys disabled"));
		}
		case MOVE_TO_OTHER_INVENTORY: {
			if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR || e.getCurrentItem().isSimilar(shop.getItem())) {
				return;
			}
			break;
		}
			
		case NOTHING:
		case PICKUP_ALL:
		case PICKUP_HALF:
		case PICKUP_ONE:
		case PICKUP_SOME:
			return;
			
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
		case SWAP_WITH_CURSOR: {
			// clicked in his own inventory
			if(!e.getClickedInventory().equals(e.getInventory())) {
				return;
			}
			if(e.getCursor() == null || e.getCursor().getType() == Material.AIR || e.getCursor().isSimilar(shop.getItem())) {
				return;
			}
			break;
		}
		case UNKNOWN:
			break;
		default:
			break;
		}
		
		e.setCancelled(true);
		e.getWhoClicked().sendMessage(tr("shop inventory you can only store shop items", trItem(e.getCursor().getType()), shop.getItemName()));
	}

}
