package me.ialistannen.inventory_profiles.commands;

import static me.ialistannen.inventory_profiles.util.Util.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the commands
 */
public class CommandManager implements CommandExecutor {

	private List<CommandPreset> commands = new ArrayList<>();
	
	{
		addCommand(new CommandHelp());
		addCommand(new CommandLogout());
		addCommand(new CommandCreate());
		addCommand(new CommandDelete());
		addCommand(new CommandMoney());
		addCommand(new CommandSetPlaytime());
		addCommand(new CommandShowPlaytime());
		addCommand(new CommandLookupPassword());
		addCommand(new CommandSetPassword());
		addCommand(new CommandBan());
		addCommand(new CommandUnban());
		addCommand(new CommandPay());
		addCommand(new CommandLanguageReload());
		addCommand(new CommandLanguageSet());
		addCommand(new CommandAddRegion());
		addCommand(new CommandRemoveRegion());
		addCommand(new CommandSellRegion());
		addCommand(new CommandListRegions());
		addCommand(new CommandSetPlaytimeModifier());
		addCommand(new CommandMember());
		addCommand(new CommandSetHome());
		addCommand(new CommandHome());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String commandName = args.length > 0 ? args[0] : "help";	// maybe finds the help, maybe not. Doesn't matter
		CommandPreset commandPreset = getCommand(commandName).orElse(getHelp());
		
		if(!commandPreset.canUse(sender)) {
			sender.sendMessage(tr("no permission"));
			return true;
		}
		if(commandPreset.isMustBePlayer() && !(sender instanceof Player)) {
			sender.sendMessage(tr("must be player"));
			return true;
		}
		
		String[] argsToPass = new String[Math.max(args.length - 1, 0)];
		
		if(args.length >= 1) {
			System.arraycopy(args, 1, argsToPass, 0, argsToPass.length);
		}
		
		if(!commandPreset.execute(sender, argsToPass)) {
			sender.sendMessage(commandPreset.getUsage());
		}
		
		return true;
	}
	
	/**
	 * @return The Help command, no matter the name and keyword
	 */
	private CommandPreset getHelp() {
		return getCommands().stream().filter(command -> command.getClass() == CommandHelp.class).findAny().get();
	}
	
	/**
	 * @param thingToMatchPattern The input. The thing the user entered. It will be matched against {@link CommandPreset#getPattern()}
	 * @return A {@link CommandPreset} if any matched. Otherwise an empty optional
	 */
	public Optional<CommandPreset> getCommand(String thingToMatchPattern) {
		return getCommands().stream().filter(command -> command.matchesPattern(thingToMatchPattern)).findAny();
	}
	
	/**
	 * @param preset The {@link CommandPreset} to add
	 */
	public void addCommand(CommandPreset preset) {
		commands.add(preset);
	}

	/**
	 * @return An unmodifiable list with all commands
	 */
	public List<CommandPreset> getCommands() {
		return Collections.unmodifiableList(commands);
	}
	
	/** 
	 * @return A list with the keywords of all commands
	 */
	public List<String> getCommandKeywords() {
		return getCommands().stream().map(command -> command.getKeyword()).collect(Collectors.toList());
	}
}
