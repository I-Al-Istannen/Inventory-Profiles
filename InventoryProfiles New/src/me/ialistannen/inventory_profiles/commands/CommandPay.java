package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.tree_command_system.PlayerCommandNode;

/**
 * The Pay command. Pays another use some of your money
 */
public class CommandPay extends PlayerCommandNode {

	/**
	 * New instance
	 */
	public CommandPay() {
		super(tr("subCommandPay name"), tr("subCommandPay keyword"),
				Pattern.compile(tr("subCommandPay pattern"), Pattern.CASE_INSENSITIVE), "");
	}
	
	
	@Override
	public String getUsage() {
		return tr("subCommandPay usage", getName());
	}

	@Override
	public String getDescription() {
		return tr("subCommandPay description", getName());
	}
	
	@Override
	protected List<String> getTabCompletions(String input, List<String> wholeUserChat, Player player) {
		List<String> toReturn = new ArrayList<>();
		
		if(wholeUserChat.size() == 2) {
			toReturn.addAll(Util.getAllProfileNames());
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
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			player.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Optional<Double> money = Util.getDouble(args[1]);
		
		if(!money.isPresent()) {
			player.sendMessage(tr("not a double", args[1]));
			return true;
		}
		
		Profile otherPlayerProfile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		double playerBalance = profile.getMoney(true);
		
		if(playerBalance < money.get()) {
			player.sendMessage(tr("not enough money", money.get() - playerBalance));
			return true;
		}
		
		profile.setMoney(profile.getMoney(true) - money.get());
		otherPlayerProfile.setMoney(otherPlayerProfile.getMoney(true) + money.get());
		
		player.sendMessage(tr("paid money", otherPlayerProfile.getName(), money.get()));
		otherPlayerProfile.getPlayer().ifPresent(otherPlayer -> {
			otherPlayer.sendMessage(tr("received money", profile.getName(), money.get()));
		});
		
		return true;
	}

}
