package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

import java.text.NumberFormat;
import java.text.ParseException;
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
 * Allows you setting the price
 */
public class CommandSetPrice extends PlayerCommandNode {

	/**
	 * Constructs an instance
	 */
	public CommandSetPrice() {
		super(tr("CommandSetPrice name"), tr("CommandSetPrice keyword"),
				Pattern.compile(tr("CommandSetPrice keyword"), Pattern.CASE_INSENSITIVE), "");
	}
	
	@Override
	public String getUsage() {
		return tr("CommandSetPrice usage", getName(), getKeyword());
	}
	
	@Override
	public String getDescription() {
		return tr("CommandSetPrice description", getName(), getKeyword());
	}
	
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
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
		
		Double price = getDouble(args[0]);
		
		if(price == null) {
			player.sendMessage(tr("not a number", args[0]));
			return true;
		}
		
		double oldPrice = shop.getPrice();
		shop.setPrice(price);
		
		player.sendMessage(tr("CommandSetPrice set shop price", shop.getOwner(), price, oldPrice));
		
		return true;
	}

	private Double getDouble(String input) {
		try {
			return NumberFormat.getNumberInstance(IPSignShop.getInstance().getLanguage().getLanguage()).parse(input).doubleValue();
		} catch (ParseException e) {
			return null;
		}
	}
	
}
