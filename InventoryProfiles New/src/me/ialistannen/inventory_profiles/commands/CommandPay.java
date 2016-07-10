package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * The Pay command. Pays another use some of your money
 */
public class CommandPay extends CommandPreset {

	/**
	 * Three thousand years we slumbered, now we RIIISE
	 */
	public CommandPay() {
		super("", true);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		List<String> toReturn = new ArrayList<>();
		
		if(position == 0) {
			toReturn.addAll(getAllProfileNames());
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
		
		if(!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			sender.sendMessage(tr("username unknown", args[0]));
			return true;
		}
		
		Optional<Double> money = Util.getDouble(args[1]);
		
		if(!money.isPresent()) {
			sender.sendMessage(tr("not a double", args[1]));
			return true;
		}
		
		Profile otherPlayerProfile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();
		
		double playerBalance = profile.getMoney(true);
		
		if(playerBalance < money.get()) {
			sender.sendMessage(tr("not enough money", money.get() - playerBalance));
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
