package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.conversations.ConversationManager.ConversationType;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.signs.BuySign;

/**
 * Allows the user to sell a region they have
 */
public class CommandSellRegion extends CommandPreset {

	/**
	 * A new instance of the SellRegion command.
	 */
	public CommandSellRegion() {
		super("", true);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		List<String> toReturn = new ArrayList<>();
		
		if(position == 0) {
			Bukkit.getWorlds().stream().map(world -> world.getName()).forEach(toReturn::add);
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if(args.length < 2) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return true;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
		
		World world = Bukkit.getWorld(args[0]);

		if(world == null) {
			sender.sendMessage(tr("world not valid", args[0]));
			return true;
		}
		
		String region = args[1];
		
		if(!InventoryProfiles.hasRegionHook()) {
			sender.sendMessage(tr("no region hook found"));
			return true;
		}
		
		if(!InventoryProfiles.getRegionHook().hasRegion(region, world)) {
			sender.sendMessage(tr("region not valid", region));
			return true;
		}
		
		Optional<RegionObject> regionObjOpt = profile.getRegionObjects().stream().filter(regionObject -> {
			return regionObject.getWorld().getUID().equals(world.getUID())
					&& regionObject.getRegionID().equalsIgnoreCase(region);
		}).findFirst();
		
		if(!regionObjOpt.isPresent() || regionObjOpt.get().getRole() != RegionRole.OWNER) {
			sender.sendMessage(tr("not your region"));
			return true;
		}
		
		
		RegionObject regionObject = regionObjOpt.get();
		double refundMoney = regionObject.getPrice()
				* InventoryProfiles.getInstance().getConfig().getDouble("region refund percentage");

		InventoryProfiles.getConversationManager().startConversation((Player) sender, ConversationType.CONFIRMATION,
				event -> {
					if (!event.gracefulExit()) {
						return;
					}
					if (!(boolean) event.getContext().getSessionData("result")) {
						return;
					}
					profile.removeRegionObject(regionObject);

					profile.setMoney(profile.getMoney(true) + refundMoney);

					sender.sendMessage(tr("sold region", regionObject.getRegionID(), refundMoney));

					if (!regionObject.getSignLocation().isPresent()) {
						sender.sendMessage(tr("could not create buy sign"));
					}

					BuySign sign = new BuySign(regionObject.getSignLocation().get(), regionObject.getRegionID(),
							regionObject.getPrice(), regionObject.isWallSign(), regionObject.getSignFacingDirection().orElse(BlockFace.SOUTH));

					InventoryProfiles.getSignManager().addSign(sign);
				}, tr("sell region confirmation prompt text", regionObject.getRegionID(), refundMoney));

		return true;
	}

}
