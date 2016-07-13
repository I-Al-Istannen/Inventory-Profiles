package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Removes a shop
 */
public class CommandRemove extends PlayerCommandNode {
	
	/**
	 * Constructs an instance
	 */
	public CommandRemove() {
		super(tr("CommandRemove name"), tr("CommandRemove keyword"),
				Pattern.compile(tr("CommandRemove keyword"), Pattern.CASE_INSENSITIVE), "");
	}
	
	@Override
	public String getUsage() {
		return tr("CommandRemove usage", getName(), getKeyword());
	}
	
	@Override
	public String getDescription() {
		return tr("CommandRemove description", getName(), getKeyword());
	}

	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		return Collections.emptyList();
	}

	@Override
	public boolean execute(Player player, String... args) {		
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
		
		if(!shop.getOwner().equals(player.getDisplayName()) && !player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + ".alterOther")) {
			player.sendMessage(tr("no permission to alter other players shop", shop.getOwner()));
			return true;
		}
		
		IPSignShop.getShopManager().removeShop(shop.getSignLocation());
		
		player.sendMessage(tr("CommandRemove removed shop", shop.getOwner(), shop.getItemAmount(), shop.getItemName()));
		
		return true;
	}

}
