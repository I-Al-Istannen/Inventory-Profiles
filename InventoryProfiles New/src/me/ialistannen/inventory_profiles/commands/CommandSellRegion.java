package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.conversations.ConversationManager.ConversationType;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionRole;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.signs.BuySign;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * Allows the user to sell a region they have
 */
public class CommandSellRegion extends PlayerCommandNode {

	/**
	 * New instance
	 */
	public CommandSellRegion() {
		super(tr("subCommandSellRegion name"), tr("subCommandSellRegion keyword"),
				Pattern.compile(tr("subCommandSellRegion pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandSellRegion usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandSellRegion description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		List<String> toReturn = new ArrayList<>();
		
		if(wholeUserChat.size() == 2) {
			Bukkit.getWorlds().stream().map(world -> world.getName()).forEach(toReturn::add);
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(Player player, String... args) {
		if(args.length < 2) {
			return false;
		}
				
		if(!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return true;
		}
		
		Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();
		
		World world = Bukkit.getWorld(args[0]);

		if(world == null) {
			player.sendMessage(tr("world not valid", args[0]));
			return true;
		}
		
		String region = args[1];
		
		if(!InventoryProfiles.hasRegionHook()) {
			player.sendMessage(tr("no region hook found"));
			return true;
		}
		
		if(!InventoryProfiles.getRegionHook().hasRegion(region, world)) {
			player.sendMessage(tr("region not valid", region));
			return true;
		}
		
		Optional<RegionObject> regionObjOpt = profile.getRegionObjects().stream().filter(regionObject -> {
			return regionObject.getWorld().getUID().equals(world.getUID())
					&& regionObject.getRegionID().equalsIgnoreCase(region);
		}).findFirst();
		
		if(!regionObjOpt.isPresent() || regionObjOpt.get().getRole() != RegionRole.OWNER) {
			player.sendMessage(tr("not your region"));
			return true;
		}
		
		
		RegionObject regionObject = regionObjOpt.get();
		double refundMoney = regionObject.getPrice()
				* InventoryProfiles.getInstance().getConfig().getDouble("region refund percentage");

		InventoryProfiles.getConversationManager().startConversation(player, ConversationType.CONFIRMATION,
				event -> {
					if (!event.gracefulExit()) {
						return;
					}
					if (!(boolean) event.getContext().getSessionData("result")) {
						return;
					}
					profile.removeRegionObject(regionObject);

					profile.setMoney(profile.getMoney(true) + refundMoney);

					player.sendMessage(tr("sold region", regionObject.getRegionID(), refundMoney));

					if (!regionObject.getSignLocation().isPresent()) {
						player.sendMessage(tr("could not create buy sign"));
					}

					BuySign sign = new BuySign(regionObject.getSignLocation().get(), regionObject.getRegionID(),
							regionObject.getPrice(), regionObject.isWallSign(), regionObject.getSignFacingDirection().orElse(BlockFace.SOUTH));

					InventoryProfiles.getSignManager().addSign(sign);
				}, tr("sell region confirmation prompt text", regionObject.getRegionID(), refundMoney));

		return true;
	}

}
