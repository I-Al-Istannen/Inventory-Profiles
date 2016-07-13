package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Allows the setting of the mode
 */
public class CommandSetMode extends PlayerCommandNode {

	/**
	 * Constructs an instance
	 */
	public CommandSetMode() {
		super(tr("CommandSetMode name"), tr("CommandSetMode keyword"),
				Pattern.compile(tr("CommandSetMode keyword"), Pattern.CASE_INSENSITIVE), "");
	}
	
	@Override
	public String getUsage() {
		return tr("CommandSetMode usage", getName(), getKeyword());
	}
	
	@Override
	public String getDescription() {
		return tr("CommandSetMode description", getName(), getKeyword());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		List<String> list = new ArrayList<>();
		
		if(wholeUserChat.size() == 2) {
			list.add(tr("CommandSetMode sell"));
			list.add(tr("CommandSetMode sell unlimited"));
			list.add(tr("CommandSetMode buy"));
			list.add(tr("CommandSetMode buy unlimted"));
		}
		return list;
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
		
		String mode = args[0];
		ShopMode newMode = shop.getMode();
		if(mode.equalsIgnoreCase(tr("CommandSetMode sell"))) {
			newMode = ShopMode.SELL;
		}
		else if (mode.equalsIgnoreCase(tr("CommandSetMode sell unlimited"))) {
			if(!player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + " set mode unlimited")) {
				player.sendMessage(tr("CommandSetMode no permission for unlimted resources", getName()));
				return true;
			}
			newMode = ShopMode.SELL_UNLIMITED;
		}
		else if (mode.equalsIgnoreCase(tr("CommandSetMode buy"))) {
			newMode = ShopMode.BUY;
		}
		else if (mode.equalsIgnoreCase(tr("CommandSetMode buy unlimted"))) {
			if(!player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + " set mode unlimited")) {
				player.sendMessage(tr("CommandSetMode no permission for unlimted resources", getName()));
				return true;
			}
			newMode = ShopMode.BUY_UNLIMITED;
		}
		
		String oldMode = shop.getMode().getShopModeName();
		shop.setMode(newMode);
		
		player.sendMessage(tr("CommandSetMode set shop mode", shop.getOwner(), newMode.getShopModeName(), oldMode));
		
		return true;
	}

}
