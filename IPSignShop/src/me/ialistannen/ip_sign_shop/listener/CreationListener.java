package me.ialistannen.ip_sign_shop.listener;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.conversations.GetTradingPriceConversation;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.conversations.Conversation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

/**
 * Listens for the creation if a shop
 */
public class CreationListener implements Listener {
	
	/**
	 * HIGH just so that most protection plugins will have already reacted
	 * 
	 * @param e The {@link PlayerInteractEvent}
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(final PlayerInteractEvent e) {
		// just to make sure
		if(e.isCancelled()) {
			return;
		}
		
		if(e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		
		if(!e.hasItem() || e.getItem().getType() == Material.AIR || !e.hasBlock() || !(e.getClickedBlock().getType() == Material.CHEST)) {
			return;
		}
		
		// player must sneak
		if(!e.getPlayer().isSneaking()) {
			return;
		}
		
		// is already a shop
		if(IPSignShop.getShopManager().isShopChest(e.getClickedBlock().getLocation())) {
			e.getPlayer().sendMessage(tr("creation listener shop already exists"));
			return;
		}
		
		Chest chest = (Chest) e.getClickedBlock().getState();
		if(chest.getInventory().getSize() != 27) {
			e.getPlayer().sendMessage(tr("creation listener is double chest"));
			return;
		}
		
		if(!isEmpty(chest.getBlockInventory())) {
			e.getPlayer().sendMessage(tr("creation listener chest not empty"));
			return;
		}
		
		String itemName = (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName())
				? e.getItem().getItemMeta().getDisplayName()
				: IPSignShopUtil.trItem(e.getItem().getType());
		
		final Location signLoc = e.getClickedBlock().getRelative(e.getBlockFace()).getLocation();

		if(signLoc.getBlock().getType() != Material.AIR || e.getClickedBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
			e.getPlayer().sendMessage(tr("creation listener not enough space"));
			return;
		}
		if(signLoc.getBlockY() != e.getClickedBlock().getLocation().getBlockY()) {
			e.getPlayer().sendMessage(tr("creation listener click on valid side"));
			return;
		}
		
		if(e.getPlayer().isConversing()) {
			e.getPlayer().sendMessage(tr("already conversing"));
			return;
		}
		
		Conversation conversation = IPSignShop.getConversationCreator().getConversationFactory()
				.withFirstPrompt(new GetTradingPriceConversation(itemName))
				.buildConversation(e.getPlayer());

		conversation.addConversationAbandonedListener(event -> {
			if(event.gracefulExit()) {
				Double amount = (Double) event.getContext().getSessionData("amount");

				Shop shop = new Shop(e.getPlayer().getDisplayName(), e.getItem(), signLoc,
						e.getClickedBlock().getLocation(), (ShopMode) event.getContext().getSessionData("mode"), amount);

				if(IPSignShop.getShopManager().hasShopAtLocation(signLoc)) {
					e.getPlayer().sendMessage(tr("creation listener shop already exists"));
					return;
				}

				if(shop.getChestLocation().getBlock().getType() != Material.CHEST) {
					e.getPlayer().sendMessage(tr("creation listener chest was destroyed"));
					return;
				}

				IPSignShop.getShopManager().addShop(shop);
			}
			else {
				event.getContext().getForWhom().sendRawMessage(tr("conversation cancelled"));
			}
		});
		conversation.begin();
		
		e.setCancelled(true);
	}
		
	/**
	 * @param inv The {@link Inventory} to check
	 * @return True if the inventory is empty
	 */
	private boolean isEmpty(Inventory inv) {
		for (ItemStack itemStack : inv.getContents()) {
			if(itemStack != null && itemStack.getType() != Material.AIR) {
				return false;
			}
		}
		
		return true;
	}
}
