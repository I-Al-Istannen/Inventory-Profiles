package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.base.CommandResultType;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommand;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.ialistannen.inventory_profiles.util.Util.getAllProfileNames;
import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * The Pay command. Pays another use some of your money
 */
class CommandPay extends DefaultCommand {

	/**
	 * New instance
	 */
	CommandPay() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_pay",
				Util.tr("command_pay_permission"), sender -> sender instanceof Player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, List<String> wholeUserChat,
	                                int indexRelativeToYou) {

		if (indexRelativeToYou == 0) {
			return getAllProfileNames();
		}
		return Collections.emptyList();
	}


	@Override
	public CommandResultType execute(CommandSender sender, String... args) {
		Player player = (Player) sender;

		if (args.length < 2) {
			return CommandResultType.SEND_USAGE;
		}

		if (!InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName())) {
			player.sendMessage(tr("not logged in"));
			return CommandResultType.SUCCESSFUL;
		}

		@SuppressWarnings("OptionalGetWithoutIsPresent") // see hasProfile call before
				Profile profile = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get();

		if (!InventoryProfiles.getProfileManager().hasProfile(args[0])) {
			player.sendMessage(tr("username unknown", args[0]));
			return CommandResultType.SUCCESSFUL;
		}

		Optional<Double> money = Util.getDouble(args[1]);

		if (!money.isPresent()) {
			player.sendMessage(tr("not a double", args[1]));
			return CommandResultType.SUCCESSFUL;
		}

		@SuppressWarnings("OptionalGetWithoutIsPresent") // see hasProfile call before
				Profile otherPlayerProfile = InventoryProfiles.getProfileManager().getProfile(args[0]).get();

		double playerBalance = profile.getMoney(true);

		if (playerBalance < money.get()) {
			player.sendMessage(tr("not enough money", money.get() - playerBalance));
			return CommandResultType.SUCCESSFUL;
		}

		profile.setMoney(profile.getMoney(true) - money.get());
		otherPlayerProfile.setMoney(otherPlayerProfile.getMoney(true) + money.get());

		player.sendMessage(tr("paid money", otherPlayerProfile.getName(), money.get()));
		otherPlayerProfile.getPlayer().ifPresent(otherPlayer ->
				otherPlayer.sendMessage(tr("received money", profile.getName(), money.get()))
		);

		return CommandResultType.SUCCESSFUL;
	}

}
