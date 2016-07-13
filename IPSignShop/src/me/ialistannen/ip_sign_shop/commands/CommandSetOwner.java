package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Sets the owner of the shop
 */
public class CommandSetOwner extends PlayerCommandNode {

	/**
	 * Constructs an instance
	 */
	public CommandSetOwner() {
		super(tr("CommandSetOwner name"), tr("CommandSetOwner keyword"),
				Pattern.compile(tr("CommandSetOwner keyword"), Pattern.CASE_INSENSITIVE), "");
	}
	
	@Override
	public String getUsage() {
		return tr("CommandSetOwner usage", getName(), getKeyword());
	}
	
	@Override
	public String getDescription() {
		return tr("CommandSetOwner description", getName(), getKeyword());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		if(wholeUserChat.size() == 2) {
			return Bukkit.getOnlinePlayers().stream()
					.filter(pl -> player.canSee(pl))
					.map(Player::getDisplayName)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	
	@Override
	public boolean execute(Player player, String... args) {
		
		if(args.length < 1) {
			return false;
		}
			
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
		
		String newOwnerName = args[0];
		
		if(!InventoryProfiles.getProfileManager().hasProfile(newOwnerName)) {
			player.sendMessage(tr("player not known", newOwnerName));
			return true;
		}
		
		String oldOwnerName = shop.getOwner();
		shop.setOwner(newOwnerName);
		
		player.sendMessage(tr("CommandSetOwner transferred ownership", newOwnerName, oldOwnerName));
		
		IPSignShopUtil.getPlayerByDisplayOrName(newOwnerName).ifPresent(newOwner -> {
			newOwner.sendMessage(tr("CommandSetOwner received ownership", oldOwnerName, player.getDisplayName()));
		});
		
		return true;
	}
}
