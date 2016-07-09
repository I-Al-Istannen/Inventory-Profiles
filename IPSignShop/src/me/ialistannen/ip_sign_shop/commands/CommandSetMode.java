package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.Language.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopMode;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import me.ialistannen.ip_sign_shop.util.Language;

/**
 * Allows the setting of the mode
 */
public class CommandSetMode extends CommandPreset {

	/**
	 * Constructs an instance
	 */
	public CommandSetMode() {
		super("", true);
	}

	@Override
	public List<String> getTabCompletionChoices(int index, String[] message) {
		List<String> list = new ArrayList<>();
		
		if(index == 0) {
			list.add(tr("CommandSetMode sell"));
			list.add(tr("CommandSetMode sell unlimited"));
			list.add(tr("CommandSetMode buy"));
			list.add(tr("CommandSetMode buy unlimted"));
		}
		return list;
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length < 1) {
			return false;
		}
		
		Player player = (Player) sender;
		
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 100);
		
		if(targetBlock == null || targetBlock.getType() == Material.AIR) {
			player.sendMessage(tr("no block in sight"));
			return true;
		}
		
		if(targetBlock.getType() != Material.WALL_SIGN) {
			player.sendMessage(tr("block not a sign", Language.translateItemName(targetBlock.getType())));
			return true;
		}
		
		if(!IPSignShop.getShopManager().hasShopAtLocation(targetBlock.getLocation())) {
			player.sendMessage(tr("not a shop", Language.translateItemName(targetBlock.getType())));
			return true;
		}
		
		Shop shop = IPSignShop.getShopManager().getShopForLocation(targetBlock.getLocation());
		
		if(!shop.getOwner().equals(player.getDisplayName()) && !player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + ".help")) {
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
				player.sendMessage(tr(getIndentifier() + " no permission for unlimted resources", getName()));
				return true;
			}
			newMode = ShopMode.SELL_UNLIMITED;
		}
		else if (mode.equalsIgnoreCase(tr("CommandSetMode buy"))) {
			newMode = ShopMode.BUY;
		}
		else if (mode.equalsIgnoreCase(tr("CommandSetMode buy unlimted"))) {
			if(!player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + " set mode unlimited")) {
				player.sendMessage(tr(getIndentifier() + " no permission for unlimted resources", getName()));
				return true;
			}
			newMode = ShopMode.BUY_UNLIMITED;
		}
		
		String oldMode = shop.getMode().getShopModeName();
		shop.setMode(newMode);
		
		player.sendMessage(tr(getIndentifier() + " set shop mode", shop.getOwner(), newMode.getShopModeName(), oldMode));
		
		return true;
	}

}
