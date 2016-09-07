package me.ialistannen.ip_sign_shop.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.ip_sign_shop.IPSignShop;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.util.IPSignShopUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.tr;
import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.trItem;

/**
 * Find the nearest shop selling the item
 */
class CommandFind extends DefaultCommand {

	/**
	 * Constructs an instance
	 */
	public CommandFind() {
		super(IPSignShop.getInstance().getLanguage(), "command_find",
				tr("command_find_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {
		if (indexRelativeToYou == 0) {
			return getMaterialNames();
		}
		return Collections.emptyList();
	}

	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		if (args.length < 1) {
			return CommandResultType.SEND_USAGE;
		}

		List<String> choices = IPSignShopUtil.getStartingWith(getMaterialNames(), clean(args[0]));

		Collections.sort(choices);

		if (choices.isEmpty()) {
			player.sendMessage(tr("command_find_material_unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		boolean ignoreEmtpy = IPSignShop.getInstance().getConfig().getBoolean("find command ignore empty shops");

		// the for loop to find any shop with a matching material. If there is a shop for a Diamond Hoe, but not for
		// an Axe, /find diamond should find it
		for (String string : choices) {
			Material material = getFromTranslation(string);
			Shop nearestShop = null;
			double nearestDistance = Double.MAX_VALUE;
			for (Shop shop : IPSignShop.getShopManager().getShopsForMaterial(material)) {
				if (!shop.getSignLocation().getWorld().equals(player.getWorld())) {
					continue;
				}

				if (ignoreEmtpy && shop.hasNoItemsLeft()) {
					continue;
				}

				double distance = shop.getChestLocation().distance(player.getLocation());
				if (distance < nearestDistance) {
					nearestDistance = distance;
					nearestShop = shop;
				}
			}

			if (nearestShop == null) {
				continue;
			}

			Vector direction = nearestShop
					.getChestLocation()
					.add(0.5, -1, 0.5) // center and look at the chest
					.toVector()
					.subtract(player.getLocation().toVector()) // end - start ==> calculate vector pointing at it
					.normalize();

			Location teleportTo = player.getLocation().setDirection(direction);

			player.teleport(teleportTo);

			player.sendMessage(tr("command_find_found_shop", nearestShop.getOwner(), nearestDistance,
					nearestShop.getItemName(), nearestShop.getItemAmount(), nearestShop.getPrice()));

			return CommandResultType.SUCCESSFUL;
		}

		player.sendMessage(tr("command_find_no_shop_found", args[0]));

		return CommandResultType.SUCCESSFUL;
	}

	private List<String> getMaterialNames() {
		// can't cache as it involves translation and I am to lazy to invalidate it.
		List<String> list = new ArrayList<>();
		for (Material mat : Material.values()) {
			list.add(clean(trItem(mat)));
		}
		return list;
	}

	/**
	 * Horrible inefficient. I should probably die from shame, but it works
	 *
	 * @param translated The Translated name
	 *
	 * @return The Corresponding Material
	 */
	private Material getFromTranslation(String translated) {
		for (Material material : Material.values()) {
			String trans = trItem(material);
			if (clean(trans).equals(clean(translated))) {
				return material;
			}
		}
		return Material.AIR;
	}

	/**
	 * Rudimentary cleans the String so that a search will work
	 *
	 * @param input The String to clean
	 *
	 * @return The cleaned String
	 */
	private String clean(String input) {
		return input.toLowerCase().replace(" ", "_").trim();
	}
}
