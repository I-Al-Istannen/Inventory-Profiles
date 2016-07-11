package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;

/**
 * Removes a shop
 */
public class CommandRemove extends CommandPreset {

	/**
	 * Constructs an instance
	 */
	public CommandRemove() {
		super("", true);
	}

	@Override
	public List<String> getTabCompletionChoices(int index, String[] message) {
		return Collections.emptyList();
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 100);
		
		if(targetBlock == null || targetBlock.getType() == Material.AIR) {
			player.sendMessage(tr("no block in sight"));
			return true;
		}
		
		if(targetBlock.getType() != Material.WALL_SIGN) {
			player.sendMessage(tr("block not a sign", trItem(targetBlock.getType())));
			return true;
		}
		
		if(!IPSignShop.getShopManager().hasShopAtLocation(targetBlock.getLocation())) {
			player.sendMessage(tr("not a shop", trItem(targetBlock.getType())));
			return true;
		}
		
		Shop shop = IPSignShop.getShopManager().getShopForLocation(targetBlock.getLocation());
		
		if(!shop.getOwner().equals(player.getDisplayName()) && !player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + ".help")) {
			player.sendMessage(tr("no permission to alter other players shop", shop.getOwner()));
			return true;
		}
		
		IPSignShop.getShopManager().removeShop(shop.getSignLocation());
		
		player.sendMessage(tr(getIndentifier() + " removed shop", shop.getOwner(), shop.getItemAmount(), shop.getItemName()));
		
		return true;
	}

}
