package me.ialistannen.ip_sign_shop.listener;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.conversations.ConfirmationConversation;
import me.ialistannen.ip_sign_shop.conversations.GetBuyAmountConversation;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;

/**
 * Listens for interactions with a shop
 */
public class InteractWithShopListener implements Listener {
	
	/**
	 * Listens for players rightclicking a shop
	 * 
	 * @param e The {@link PlayerInteractEvent}
	 */
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasBlock()) {
			return;
		}
		
		if(!IPSignShop.getShopManager().isShopSign(e.getClickedBlock().getLocation())) {
			return;
		}
		
		if(e.getPlayer().isConversing()) {
			e.getPlayer().sendMessage(tr("already conversing"));
			return;
		}
		
		final Shop shop = IPSignShop.getShopManager().getShopForLocation(e.getClickedBlock().getLocation());
		sendShopInformation(e.getPlayer(), shop);
		
		if(shop.getOwner().equals(e.getPlayer().getDisplayName())) {
			e.getPlayer().sendMessage(tr("interact with shop is your shop"));
			return;
		}
		
		String shopMode = IPSignShopUtil.getNiceNameForConstant(shop.getMode().getOpposite().name());
		String suffix = "";
		if(shopMode.toLowerCase().contains("sell")) {
			shopMode = ChatColor.AQUA + shop.getMode().getOpposite().getShopModeName();
			suffix = tr("interact with shop selling items available suffix", IPSignShopUtil.getItemAmount(e.getPlayer().getInventory(), shop.getItem()));
		}
		else {
			shopMode = ChatColor.DARK_PURPLE + shop.getMode().getOpposite().getShopModeName();
		}
		
		Conversation conversation = IPSignShop.getConversationCreator().getConversationFactory()
				.withFirstPrompt(new GetBuyAmountConversation(shop.getMode().getOpposite(), suffix))
				.buildConversation(e.getPlayer());
		
		conversation.addConversationAbandonedListener(new ConversationAbandonedListener() {
			
			@Override
			public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
				handlePlayerGetBuyAmountConversationEnd(abandonedEvent, shop);
			}
		});
		
		conversation.begin();
	}
	
	
	// The lack of a blocking variant makes the code much more complicated than needed. No easy way found by me yet.
	private void handlePlayerGetBuyAmountConversationEnd(ConversationAbandonedEvent abandonedEvent, Shop shop) {
		if(abandonedEvent.gracefulExit()) {
			Integer amount = (Integer) abandonedEvent.getContext().getSessionData("amount");
			Player player = (Player) abandonedEvent.getContext().getForWhom();
			showAndHandleConfirmationDialog(player, shop, amount);
		}
		else {
			abandonedEvent.getContext().getForWhom().sendRawMessage(tr("conversation cancelled"));
		}
	}
	
	private void showAndHandleConfirmationDialog(final Player player, final Shop shop, final int amount) {
		String action = shop.getMode().getOpposite().name();
		if(action.contains("SELL")) {
			action = tr("buy amount conversation sell");
		}
		else {
			action = tr("buy amount conversation buy");			
		}
		
		String promptMessage = tr("interact with shop confirmation message", shop.getOwner(), shop.getItemAmount(), shop.getItemName(),
				amount, shop.getPrice(), amount * shop.getPrice(), action);
		
		Conversation conversation = IPSignShop.getConversationCreator().getConversationFactory()
				.withFirstPrompt(new ConfirmationConversation(promptMessage))
				.buildConversation(player);
		
		conversation.addConversationAbandonedListener(new ConversationAbandonedListener() {
			
			@Override
			public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
				if(!abandonedEvent.gracefulExit()) {
					abandonedEvent.getContext().getForWhom().sendRawMessage(tr("conversation cancelled"));
					return;
				}
				
				if((Boolean) abandonedEvent.getContext().getSessionData("result")) {
					if(shop.getMode() == ShopMode.BUY || shop.getMode() == ShopMode.BUY_UNLIMITED) {
						handleSellToShop(shop, player, amount);
					}
					else if(shop.getMode() == ShopMode.SELL || shop.getMode() == ShopMode.SELL_UNLIMITED) {
						handleBuyFromShop(shop, player, amount);
					}						
				}
				else {
					abandonedEvent.getContext().getForWhom().sendRawMessage(tr("interact with shop cancelled trade"));
				}
			}
		});
		
		conversation.begin();

	}
	
	private void handleBuyFromShop(Shop shop, Player player, int amount) {
		if(amount > shop.getItemAmount() && !(shop.getMode() == ShopMode.SELL_UNLIMITED)) {
			player.sendMessage(tr("interact with shop buying shop not enough items", amount - shop.getItemAmount(), shop.getItemAmount(), amount));
			return;
		}
		
		if(IPSignShopUtil.getFreeSpaceForItem(player.getInventory(), shop.getItem()) < amount) {
			player.sendMessage(tr("interact with shop buying not enough space in inventory"
					, IPSignShopUtil.getFreeSpaceForItem(player.getInventory(), shop.getItem())
					, amount));
			return;
		}
		
		if(InventoryProfiles.hasMoneyHook()) {
			double playerBalance = InventoryProfiles.getMoneyHook().getBalance(player);
			if(playerBalance < (amount * shop.getPrice())) {
				player.sendMessage(tr("interact with shop buying not enough money", amount * shop.getPrice() - playerBalance, playerBalance, amount * shop.getPrice()));
				return;
			}
			
			InventoryProfiles.getProfileManager().getProfile(shop.getOwner()).get()
					.setMoney(InventoryProfiles.getProfileManager().getProfile(shop.getOwner()).get().getMoney(true)
							+ (amount * shop.getPrice()));
			InventoryProfiles.getMoneyHook().removeMoney(player, amount * shop.getPrice());
		}
				
		ItemStack itemsToAdd = new ItemStack(shop.getItem());
		itemsToAdd.setAmount(amount);
		player.getInventory().addItem(itemsToAdd);
		
		shop.removeItems(amount);
		
		sendPurchasedMessage(player, shop, amount);
	}
	
	private void handleSellToShop(Shop shop, Player player, int amount) {
		int playerItemAmount = IPSignShopUtil.getItemAmount(player.getInventory(), shop.getItem());
		
		int freeSpace = IPSignShopUtil.getFreeSpaceForItem(shop.getShopInventory(), shop.getItem());
		
		if(freeSpace < amount) {
			player.sendMessage(tr("interact with shop selling not enough space in shop", freeSpace, amount - freeSpace));
			return;
		}
		
		if(playerItemAmount < amount) {			
			player.sendMessage(tr("interact with shop selling not enough items", playerItemAmount, amount - playerItemAmount, amount));
			return;
		}
		
		double moneyToCredit = amount * shop.getPrice();
		if(InventoryProfiles.hasMoneyHook()) {
			double moneyOfShopOwner = InventoryProfiles.getProfileManager().getProfile(shop.getOwner()).get().getMoney(true);
			if(moneyOfShopOwner < moneyToCredit && shop.getMode() != ShopMode.BUY_UNLIMITED) {
				player.sendMessage(tr("interact with shop selling shop owner not enough money", moneyOfShopOwner, moneyToCredit - moneyOfShopOwner, shop.getOwner()));
				return;
			}
			
			InventoryProfiles.getProfileManager().getProfile(shop.getOwner()).get().setMoney(moneyOfShopOwner - moneyToCredit);
			InventoryProfiles.getMoneyHook().addMoney(player, moneyToCredit);
		}
		
		IPSignShopUtil.removeItems(player.getInventory(), shop.getItem(), amount);
		
		// just get them deleted if used on an admin shop
		if(shop.getMode() == ShopMode.BUY) {
			shop.addItems(amount);
		}
		
		sendSelledMessage(player, shop, amount);
	}
	
	private void sendShopInformation(CommandSender sender, Shop shop) {
		sender.sendMessage(tr("interact with shop line 1", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 2", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 3", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 4", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 5", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 6", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 7", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
		sender.sendMessage(tr("interact with shop line 8", shop.getOwner(), shop.getItemName(), shop.getItemAmount(), shop.getPrice(), shop.getMode().getShopModeName()));
	}
	
	private void sendPurchasedMessage(CommandSender sender, Shop shop, int amount) {
		sender.sendMessage(tr("interact with shop successfully purchased line 1", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
		sender.sendMessage(tr("interact with shop successfully purchased line 2", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
		sender.sendMessage(tr("interact with shop successfully purchased line 3", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
		sender.sendMessage(tr("interact with shop successfully purchased line 4", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
	}

	private void sendSelledMessage(CommandSender sender, Shop shop, int amount) {
		sender.sendMessage(tr("interact with shop successfully sold line 1", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
		sender.sendMessage(tr("interact with shop successfully sold line 2", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
		sender.sendMessage(tr("interact with shop successfully sold line 3", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
		sender.sendMessage(tr("interact with shop successfully sold line 4", shop.getOwner(), shop.getItemName(),
				shop.getItemAmount(), shop.getPrice(), amount, amount * shop.getPrice()));
	}
}
