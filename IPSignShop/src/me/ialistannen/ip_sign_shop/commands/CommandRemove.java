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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

/**
 * Removes a shop
 */
class CommandRemove extends DefaultCommand {

	/**
	 * Constructs an instance
	 */
	public CommandRemove() {
		super(IPSignShop.getInstance().getLanguage(), "command_remove",
				tr("command_remove_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;
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
		
		IPSignShop.getShopManager().removeShop(shop.getSignLocation());
		
		player.sendMessage(tr("command_remove_removed_shop", shop.getOwner(), shop.getItemAmount(), shop.getItemName()));
		
		return CommandResultType.SUCCESSFUL;
	}

}
