package me.ialistannen.ip_sign_shop;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.ip_sign_shop.commands.CommandClean;
import me.ialistannen.ip_sign_shop.commands.CommandFind;
import me.ialistannen.ip_sign_shop.commands.CommandHelp;
import me.ialistannen.ip_sign_shop.commands.CommandRemove;
import me.ialistannen.ip_sign_shop.commands.CommandSetMode;
import me.ialistannen.ip_sign_shop.commands.CommandSetOwner;
import me.ialistannen.ip_sign_shop.commands.CommandSetPrice;
import me.ialistannen.ip_sign_shop.conversations.ConversationCreator;
import me.ialistannen.ip_sign_shop.datastorage.AutoSaver;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopManager;
import me.ialistannen.ip_sign_shop.listener.CreationListener;
import me.ialistannen.ip_sign_shop.listener.DestroyListener;
import me.ialistannen.ip_sign_shop.listener.InteractWithShopListener;
import me.ialistannen.ip_sign_shop.listener.ProtectShopContentsListener;
import me.ialistannen.ip_sign_shop.util.DurationParser;
import me.ialistannen.languageSystem.I18N;
import me.ialistannen.tree_command_system.CommandTreeCommandListener;
import me.ialistannen.tree_command_system.CommandTreeManager;
import me.ialistannen.tree_command_system.CommandTreeTabCompleteListener;

/**
 * The main class for the Quick Chest shop
 */
public class IPSignShop extends JavaPlugin {

	private static IPSignShop instance;

	private ShopManager shopManager = new ShopManager();
	private ConversationCreator conversationCreator;
	
	private I18N language;
	private CommandTreeManager treeManager;
	
	@Override
	public void onEnable() {
		instance = this;
		registerConfigurationSerializationClasses();
		{
			Path langFolder = getDataFolder().toPath().resolve("translations");
			I18N.copyDefaultFiles("translations", langFolder, false, getClass());
			language = new I18N("translations", langFolder, InventoryProfiles.getInstance().getCurrentLocale(), getLogger(), getClassLoader(), "Messages", "Items");
		}
		
		conversationCreator = new ConversationCreator(this);
		
		try {
			shopManager = ShopManager.fromFile(getDataFolder().toPath().resolve("saves.yml"));
		} catch (FileNotFoundException e) {
			// recoverable. Just create the file later on
			shopManager = new ShopManager();
		} catch(Exception e) {
			// well, problems here. Unforeseen or an IllegalArgumentException. Either way, shut yourself down and don't modify the saves!
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		saveDefaultConfig();
		reloadCommands();
		
		
		Bukkit.getPluginManager().registerEvents(new CreationListener(), this);
		Bukkit.getPluginManager().registerEvents(new DestroyListener(), this);
		Bukkit.getPluginManager().registerEvents(new InteractWithShopListener(), this);
		Bukkit.getPluginManager().registerEvents(new ProtectShopContentsListener(), this);
				
		if(!InventoryProfiles.hasMoneyHook()) {
			getLogger().log(Level.SEVERE,
					"No economy plugin found! This plugin won't work correctly!"
					+ " I am a generous god, so you will just receive the warning but the plugin will still load ;)");
		}
		
		new BukkitRunnable() {

			@Override
			public void run() {
				getShopManager().updateAllShops();
			}
		}.runTaskTimer(this, 0, getConfig().getInt("delay between shop updates"));
		
		if(getConfig().getBoolean("auto saver activated")) {
			new AutoSaver().runTaskTimer(this, 0, DurationParser.parseDurationToTicks(getConfig().getString("backup period")));
		}
	}

	private void registerConfigurationSerializationClasses() {
		ConfigurationSerialization.registerClass(Shop.class);
	}
	
	/**
	 * Reloads all the commands by removing and then re-registering them.
	 * 
	 * <br>If this is async, you may experience <b><i>some</i></b> issues.
	 */
	public void reloadCommands() {
		// nulling not needed.
		getCommand("ipsignshop").setExecutor(null);
		getCommand("ipsignshop").setTabCompleter(null);
		treeManager = new CommandTreeManager();
		
		treeManager.registerChild(treeManager.getRoot(), new CommandHelp());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetOwner());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetMode());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetPrice());
		treeManager.registerChild(treeManager.getRoot(), new CommandClean());
		treeManager.registerChild(treeManager.getRoot(), new CommandRemove());
		treeManager.registerChild(treeManager.getRoot(), new CommandFind());
		
		getCommand("ipsignshop").setExecutor(new CommandTreeCommandListener(treeManager, language));
		getCommand("ipsignshop").setTabCompleter(new CommandTreeTabCompleteListener(treeManager, true));
	}

	@Override
	public void onDisable() {
		try {
			getShopManager().toFile(getSaveFile(), StandardCopyOption.REPLACE_EXISTING);
		} catch (FileAlreadyExistsException e) {
			e.printStackTrace();
		}
		
		getShopManager().despawnAllItems();
	}

	/**
	 * @return The {@link CommandTreeManager}
	 */
	public CommandTreeManager getTreeManager() {
		return treeManager;
	}
	
	/**
	 * @return The language
	 */
	public I18N getLanguage() {
		return language;
	}
	
	/**
	 * @return The save file for the Shop Manager
	 */
	public Path getSaveFile() {
		return getDataFolder().toPath().resolve("saves.yml");
	}
	
	/**
	 * @return The Shop manager
	 */
	public static ShopManager getShopManager() {
		return getInstance().shopManager;
	}

	/**
	 * @return The {@link ConversationCreator}
	 */
	public static ConversationCreator getConversationCreator() {
		return getInstance().conversationCreator;
	}

	/**
	 * @return The instance of this class.
	 */
	public static IPSignShop getInstance() {
		return instance;
	}
}
