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
import me.ialistannen.ip_sign_shop.commands.CommandManager;
import me.ialistannen.ip_sign_shop.commands.CommandRemove;
import me.ialistannen.ip_sign_shop.commands.CommandSetMode;
import me.ialistannen.ip_sign_shop.commands.CommandSetOwner;
import me.ialistannen.ip_sign_shop.commands.CommandSetPrice;
import me.ialistannen.ip_sign_shop.commands.MyCommandTabCompleter;
import me.ialistannen.ip_sign_shop.conversations.ConversationCreator;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopManager;
import me.ialistannen.ip_sign_shop.listener.CreationListener;
import me.ialistannen.ip_sign_shop.listener.DestroyListener;
import me.ialistannen.ip_sign_shop.listener.InteractWithShopListener;
import me.ialistannen.ip_sign_shop.listener.ProtectShopContentsListener;
import me.ialistannen.languageSystem.I18N;

/**
 * The main class for the Quick Chest shop
 */
public class IPSignShop extends JavaPlugin {

	private static IPSignShop instance;

	private ShopManager shopManager = new ShopManager();
	private ConversationCreator conversationCreator;
	private CommandManager commandManager;
	
	private I18N language;
	
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
		} catch (FileNotFoundException | IllegalArgumentException e) {
			shopManager = new ShopManager();
		}
		
		commandManager = new CommandManager();

		saveDefaultConfig();
		registerDefaultCommands();
		
		getCommand("ipsignshop").setExecutor(getCommandManager());
		getCommand("ipsignshop").setTabCompleter(new MyCommandTabCompleter());
		
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
	}

	private void registerConfigurationSerializationClasses() {
		ConfigurationSerialization.registerClass(Shop.class);
	}
	
	private void registerDefaultCommands() {
		getCommandManager().addCommand(new CommandHelp());
		getCommandManager().addCommand(new CommandSetOwner());
		getCommandManager().addCommand(new CommandSetMode());
		getCommandManager().addCommand(new CommandSetPrice());
		getCommandManager().addCommand(new CommandClean());
		getCommandManager().addCommand(new CommandRemove());
		getCommandManager().addCommand(new CommandFind());
	}

	@Override
	public void onDisable() {
		try {
			getShopManager().toFile(getDataFolder().toPath().resolve("saves.yml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (FileAlreadyExistsException e) {
			e.printStackTrace();
		}
		
		getShopManager().despawnAllItems();
	}

	/**
	 * @return The language
	 */
	public I18N getLanguage() {
		return language;
	}
	
	/**
	 * @return The Shop manager
	 */
	public static ShopManager getShopManager() {
		return getInstance().shopManager;
	}
	
	/**
	 * @return The Command manager
	 */
	public static CommandManager getCommandManager() {
		return getInstance().commandManager;
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
