package me.ialistannen.ip_sign_shop.commands;

import static me.ialistannen.ip_sign_shop.util.Language.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The CommandManager
 */
public class CommandManager implements CommandExecutor {

	private List<CommandPreset> commandList = new ArrayList<>();
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String commandName = args.length > 0 ? args[0] : tr("CommandHelp name");
		CommandPreset commandPreset = getCommandFromKeyword(commandName);
		
		if(commandPreset == null) {
			commandPreset = getCommandFromKeyword(tr("CommandHelp keyword"));
		}
		
		if(commandPreset.isMustBePlayer() && !(sender instanceof Player)) {
			sender.sendMessage(tr("must be player", commandPreset.getName()));
			return true;
		}
		
		if(!commandPreset.canUse(sender)) {
			sender.sendMessage(tr("no permission", commandPreset.getName()));
			return true;
		}
		
		String[] argsToPass = new String[Math.max(args.length, 1) - 1];
		if(args.length > 1) {
			System.arraycopy(args, 1, argsToPass, 0, argsToPass.length);
		}
		
		if(!commandPreset.execute(sender, argsToPass)) {
			sender.sendMessage(commandPreset.getUsageMessage());
		}
		
		return true;
	}
	
	/**
	 * @param command The command to add
	 */
	public void addCommand(CommandPreset command) {
		commandList.add(command);
	}

	/**
	 * @param keyWord The keyword of the command
	 * @return The Command or null if not found
	 */
	public CommandPreset getCommandFromKeyword(String keyWord) {
		for (CommandPreset commandPreset : commandList) {
			if(commandPreset.getKeyword().equalsIgnoreCase(keyWord)) {
				return commandPreset;
			}
		}
		return null;
	}

	/**
	 * @return All the Command names
	 */
	public Collection<String> getAllCommandKeywords() {
		List<String> list = new ArrayList<>();
		for (CommandPreset commandPreset : getAll()) {
			list.add(commandPreset.getKeyword());
		}
		return list;
	}
	
	/**
	 * @return All the commands
	 */
	public Collection<CommandPreset> getAll() {
		return new ArrayList<>(commandList);
	}
}
