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
 * Sets/takes/gives/ shows money
 */
public class CommandMoney extends CommandPreset {

	/**
	 * A new instance. Constructor, you know.
	 */
	public CommandMoney() {
		super("", false);
	}
	
	@Override
	public List<String> onTabComplete(int position, List<String> messages) {
		List<String> toReturn = new ArrayList<>();
		
		// highly unstable, Maybe better without?
		if(position == 0) {
			toReturn.add(tr("subCommandMoneyBalanceKeyword").contains("|") ? tr("subCommandMoneyBalanceKeyword").split("\\|")[0] : tr("subCommandMoneyBalanceKeyword"));
			toReturn.add(tr("subCommandMoneySetKeyword").contains("|") ? tr("subCommandMoneySetKeyword").split("\\|")[0] : tr("subCommandMoneySetKeyword"));
			toReturn.add(tr("subCommandMoneyTakeKeyword").contains("|") ? tr("subCommandMoneyTakeKeyword").split("\\|")[0] : tr("subCommandMoneyTakeKeyword"));
			toReturn.add(tr("subCommandMoneyGiveKeyword").contains("|") ? tr("subCommandMoneyGiveKeyword").split("\\|")[0] : tr("subCommandMoneyGiveKeyword"));
		}
		if(position == 1) {
			toReturn.addAll(getAllProfileNames());
		}
		return toReturn;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		
		if(sender instanceof Player) {
			if(args.length == 0 || !sender.hasPermission(Util.PERMISSION_PREFIX + ".money")) {
				Player player = (Player) sender;
				Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());
				
				if(!profile.isPresent()) {
					sender.sendMessage(tr("not logged in"));
					return true;
				}
				
				sender.sendMessage(tr("balance inform message", profile.get().getName(), profile.get().getMoney(true)));
				return true;
			}
		}
		
		if(args.length < 2) {
			return false;
		}
		
		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[1]);
		if(!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[1]));
			return true;
		}
		
		Profile profile = optProf.get();
		
		// show the balance
		if(args[0].matches(tr("subCommandMoneyBalanceKeyword"))) {
			sender.sendMessage(tr("balance inform message", profile.getName(), profile.getMoney(true)));
			return true;
		}
		
		// now set/take/give <amount> and first for profile
		if(args.length < 3) {
			return false;
		}

		
		Optional<Double> moneyOpt = Util.getDouble(args[2]);
		
		if(!moneyOpt.isPresent()) {
			sender.sendMessage(tr("not a double", args[2]));
			return true;
		}
		
		// set the money
		if(args[0].matches(tr("subCommandMoneySetKeyword"))) {
			profile.setMoney(moneyOpt.get());
			sender.sendMessage(tr("money set message", profile.getName(), profile.getMoney(true)));
			return true;
		}
		// take money
		else if(args[0].matches(tr("subCommandMoneyTakeKeyword"))) {
			profile.setMoney(profile.getMoney(true) - moneyOpt.get());
			sender.sendMessage(tr("money take message", profile.getName(), moneyOpt.get()));			
			return true;
		}
		// give money
		else if(args[0].matches(tr("subCommandMoneyGiveKeyword"))) {
			profile.setMoney(profile.getMoney(true) + moneyOpt.get());
			sender.sendMessage(tr("money give message", profile.getName(), moneyOpt.get()));			
			return true;			
		}
		else {
			// not a valid keyword
			return false;
		}
	}

	
}
