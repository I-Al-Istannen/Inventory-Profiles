package me.ialistannen.inventory_profiles.commands;

import me.ialistannen.bukkitutil.commandsystem.implementation.RelayCommandNode;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.util.Util;

/**
 * The main command for this plugin
 */
public class CommandInventoryProfiles extends RelayCommandNode {

	public CommandInventoryProfiles() {
		super(InventoryProfiles.getInstance().getLanguage(), "command_inventory_profiles",
				Util.tr("command_inventory_profiles_permission"), sender -> true);

		addChild(new CommandLogout());
		addChild(new CommandCreate());
		addChild(new CommandDelete());
		addChild(new CommandMoney());
		addChild(new CommandSetPlaytime());
		addChild(new CommandShowPlaytime());
		addChild(new CommandLookupPassword());
		addChild(new CommandSetPassword());
		addChild(new CommandBan());
		addChild(new CommandUnban());
		addChild(new CommandPay());
		addChild(new CommandLanguageReload());
		addChild(new CommandLanguageSet());
		addChild(new CommandAddRegion());
		addChild(new CommandRemoveRegion());
		addChild(new CommandSellRegion());
		addChild(new CommandListRegions());
		addChild(new CommandSetPlaytimeModifier());
		addChild(new CommandMember());
		addChild(new CommandSetHome());
		addChild(new CommandHome());
	}
}
