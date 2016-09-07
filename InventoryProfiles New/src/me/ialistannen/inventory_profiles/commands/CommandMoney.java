package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Sets/takes/gives/ shows money
 */
class CommandMoney extends DefaultCommand {

	/**
	 * New instance
	 */
	public CommandMoney() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_money",
				Util.tr("command_money_permission"), sender -> true);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		List<String> toReturn = new ArrayList<>();

		// highly unstable, Maybe better without?
		if (indexRelativeToYou == 0) {
			toReturn.add(tr("command_money_balance_keyword").contains("|")
					? tr("command_money_balance_keyword").split("\\|")[0]
					: tr("command_money_balance_keyword"));
			toReturn.add(tr("command_money_set_keyword").contains("|")
					? tr("command_money_set_keyword").split("\\|")[0]
					: tr("command_money_set_keyword"));
			toReturn.add(tr("command_money_take_keyword").contains("|")
					? tr("command_money_take_keyword").split("\\|")[0]
					: tr("command_money_take_keyword"));
			toReturn.add(tr("command_money_give_keyword").contains("|")
					? tr("command_money_give_keyword").split("\\|")[0]
					: tr("command_money_give_keyword"));
		}
		else if (indexRelativeToYou == 1) {
			toReturn.addAll(Util.getAllProfileNames());
		}
		return toReturn;
	}


	@Override
	public CommandResultType execute(CommandSender sender, String... args) {

		if (sender instanceof Player) {
			if (args.length == 0 || !sender.hasPermission(Util.PERMISSION_PREFIX + ".money")) {
				Player player = (Player) sender;
				Optional<Profile> profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());

				if (!profile.isPresent()) {
					sender.sendMessage(tr("not logged in"));
					return CommandResultType.SUCCESSFUL;
				}

				sender.sendMessage(tr("balance inform message",
						profile.get().getName(), profile.get().getMoney(true)));
				return CommandResultType.SUCCESSFUL;
			}
		}

		if (args.length < 2) {
			return CommandResultType.SEND_USAGE;
		}

		Optional<Profile> optProf = InventoryProfiles.getProfileManager().getProfile(args[1]);
		if (!optProf.isPresent()) {
			sender.sendMessage(tr("username unknown", args[1]));
			return CommandResultType.SUCCESSFUL;
		}

		Profile profile = optProf.get();

		// show the balance
		if (args[0].matches(tr("command_money_balance_keyword"))) {
			sender.sendMessage(tr("balance inform message", profile.getName(), profile.getMoney(true)));
			return CommandResultType.SUCCESSFUL;
		}

		// now set/take/give <amount> and first for profile
		if (args.length < 3) {
			return CommandResultType.SEND_USAGE;
		}


		Optional<Double> moneyOpt = Util.getDouble(args[2]);

		if (!moneyOpt.isPresent()) {
			sender.sendMessage(tr("not a double", args[2]));
			return CommandResultType.SUCCESSFUL;
		}

		// set the money
		if (args[0].matches(tr("command_money_set_keyword"))) {
			profile.setMoney(moneyOpt.get());
			sender.sendMessage(tr("money set message", profile.getName(), profile.getMoney(true)));
			return CommandResultType.SUCCESSFUL;
		}
		// take money
		else if (args[0].matches(tr("command_money_take_keyword"))) {
			profile.setMoney(profile.getMoney(true) - moneyOpt.get());
			sender.sendMessage(tr("money take message", profile.getName(), moneyOpt.get()));
			return CommandResultType.SUCCESSFUL;
		}
		// give money
		else if (args[0].matches(tr("command_money_give_keyword"))) {
			profile.setMoney(profile.getMoney(true) + moneyOpt.get());
			sender.sendMessage(tr("money give message", profile.getName(), moneyOpt.get()));
			return CommandResultType.SUCCESSFUL;
		}
		else {
			// not a valid keyword
			return CommandResultType.SEND_USAGE;
		}
	}


}
