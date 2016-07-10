package me.ialistannen.inventory_profiles.listener;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Sign;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.conversations.ConversationManager.ConversationType;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.signs.BuySign;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * The Listener to create Buy Signs
 */
public class BuySignListener implements Listener {

	/**
	 * Listens to sign changes to detect the creation of a {@link BuySign}
	 * 
	 * @param e The {@link SignChangeEvent}
	 */
	@EventHandler
	public void onSignChange(SignChangeEvent e) {

		// check if it is a BuySign
		if(!e.getLine(0).equalsIgnoreCase(tr("sign line 1"))) {
			return;
		}
		
		if(!e.getPlayer().hasPermission(Util.PERMISSION_PREFIX + ".createBuySign")) {
			e.getPlayer().sendMessage(tr("no permission"));
			return;
		}
		
		if(!InventoryProfiles.hasRegionHook()) {
			e.getPlayer().sendMessage(tr("no region hook found"));
			return;
		}
		
		String region = e.getLine(1);
		if(!InventoryProfiles.getRegionHook().hasRegion(region, e.getBlock().getWorld())) {
			e.getPlayer().sendMessage(tr("region not valid", e.getLine(1)));
			return;
		}
		
		Optional<Double> price = Util.getDouble(e.getLine(2));
		
		if(!price.isPresent()) {
			e.getPlayer().sendMessage(tr("not a double", e.getLine(2)));
			return;
		}

		Sign signMaterialData = (Sign) e.getBlock().getState().getData();
		BuySign sign = new BuySign(e.getBlock().getLocation(), region, price.get(), e.getBlock().getType() == Material.WALL_SIGN, signMaterialData.getFacing());
		
		InventoryProfiles.getSignManager().addSign(sign);
		
		e.getPlayer().sendMessage(tr("created buy sign", region, price.get()));		
	}
	
	
	/**
	 * Listens for players clicking on a buy sign and handles that.
	 * 
	 * @param e The {@link PlayerInteractEvent}
	 */
	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {
		if(!(e.getAction() == Action.RIGHT_CLICK_BLOCK) || !e.hasBlock()) {
			return;
		}
		
		if(!InventoryProfiles.getSignManager().hasSignAtLocation(e.getClickedBlock().getLocation())) {
			return;
		}
				
		if(!InventoryProfiles.hasRegionHook()) {
			e.getPlayer().sendMessage(tr("no region hook found"));
			return;
		}
		
		if(!InventoryProfiles.getProfileManager().hasProfile(e.getPlayer().getDisplayName())) {
			e.getPlayer().sendMessage(tr("not logged in"));
			return;
		}
		
		BuySign sign = InventoryProfiles.getSignManager().getBuySign(e.getClickedBlock().getLocation()).get();
		Profile profile = InventoryProfiles.getProfileManager().getProfile(e.getPlayer().getDisplayName()).get();
		
		if(InventoryProfiles.getRegionHook().isOwner(sign.getRegionID(), sign.getLocation().getWorld(), profile)) {
			e.getPlayer().sendMessage(tr("already owns region"));
			return;
		}
		
		double playerMoney = profile.getMoney(true);
		
		if(playerMoney < sign.getPrice()) {
			e.getPlayer().sendMessage(tr("not enough money", sign.getPrice() - playerMoney));
			return;
		}

		InventoryProfiles.getConversationManager().startConversation(e.getPlayer(), ConversationType.CONFIRMATION,
				(event) -> {
					if (!event.gracefulExit()) {
						return;
					}

					if ((boolean) event.getContext().getSessionData("result")) {

						profile.setMoney(profile.getMoney(true) - sign.getPrice());
						profile.addRegionObject(new RegionObject(sign.getRegionID(), sign.getLocation().getWorld(),
								RegionRole.OWNER, sign.getPrice(), sign.getLocation(), sign.getBlockFace().get(),
								sign.isWallSign()));

						InventoryProfiles.getSignManager().removeSign(sign);

						e.getPlayer().sendMessage(tr("bought region", sign.getRegionID(), sign.getPrice()));
					}

				}, tr("confirmation buy region", sign.getRegionID(), sign.getPrice()));
	}
	
	
	/**
	 * Listens for blockBreak events and removes the sign if the block is broken and a {@link BuySign}.
	 * 
	 * @param e The {@link BlockBreakEvent}
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e) {
		// Just make sure
		if(e.isCancelled() || (e.getBlock().getType() != Material.SIGN_POST && e.getBlock().getType() != Material.WALL_SIGN)) {
			return;
		}
		
		// remove the sign if there is one
		InventoryProfiles.getSignManager().getBuySign(e.getBlock().getLocation()).ifPresent(InventoryProfiles.getSignManager()::removeSign);
	}
}
