package me.ialistannen.ip_sign_shop.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

/**
 * Allows you setting the price
 */
class CommandSetPrice extends DefaultCommand {

	/**
	 * Constructs an instance
	 */
	public CommandSetPrice() {
		super(IPSignShop.getInstance().getLanguage(), "command_set_price",
				tr("command_set_price_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		if(args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}
		
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 100);
		
		if(targetBlock == null || targetBlock.getType() == Material.AIR) {
			player.sendMessage(tr("no block in sight"));
			return CommandResultType.SUCCESSFUL;
		}
		
		if(targetBlock.getType() != Material.WALL_SIGN) {
			player.sendMessage(tr("block not a sign", trItem(targetBlock.getType())));
			return CommandResultType.SUCCESSFUL;
		}
		
		if(!IPSignShop.getShopManager().hasShopAtLocation(targetBlock.getLocation())) {
			player.sendMessage(tr("not a shop", trItem(targetBlock.getType())));
			return CommandResultType.SUCCESSFUL;
		}
		
		Shop shop = IPSignShop.getShopManager().getShopForLocation(targetBlock.getLocation());
		
		if(!shop.getOwner().equals(player.getDisplayName()) && !player.hasPermission(IPSignShopUtil.PERMISSION_PREFIX + ".alterOther")) {
			player.sendMessage(tr("no permission to alter other players shop", shop.getOwner()));
			return CommandResultType.SUCCESSFUL;
		}
		
		Double price = getDouble(args[0]);
		
		if(price == null) {
			player.sendMessage(tr("not a number", args[0]));
			return CommandResultType.SUCCESSFUL;
		}
		
		double oldPrice = shop.getPrice();
		shop.setPrice(price);
		
		player.sendMessage(tr("command_set_price_set_shop_price", shop.getOwner(), price, oldPrice));
		
		return CommandResultType.SUCCESSFUL;
	}

	private Double getDouble(String input) {
		try {
			return NumberFormat.getNumberInstance(IPSignShop.getInstance().getLanguage().getLanguage()).parse(input).doubleValue();
		} catch (ParseException e) {
			return null;
		}
	}
	
}
