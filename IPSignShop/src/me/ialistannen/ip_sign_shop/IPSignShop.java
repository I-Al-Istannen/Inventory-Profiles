package me.ialistannen.ip_sign_shop;

import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommandExecutor;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultHelpCommand;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultTabCompleter;
import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.LanguageEventReceiverHook;
import me.ialistannen.inventory_profiles.util.DurationParser;
import me.ialistannen.ip_sign_shop.commands.CommandIpSignShop;
import me.ialistannen.ip_sign_shop.conversations.ConversationCreator;
import me.ialistannen.ip_sign_shop.datastorage.AutoSaver;
import me.ialistannen.ip_sign_shop.datastorage.Shop;
import me.ialistannen.ip_sign_shop.datastorage.ShopManager;
import me.ialistannen.ip_sign_shop.listener.CreationListener;
import me.ialistannen.ip_sign_shop.listener.DestroyListener;
import me.ialistannen.ip_sign_shop.listener.InteractWithShopListener;
import me.ialistannen.ip_sign_shop.listener.ProtectShopContentsListener;
import me.ialistannen.languageSystem.I18N;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * The main class for the Quick Chest shop
 */
public class IPSignShop extends JavaPlugin {

	private static IPSignShop instance;

	private ShopManager shopManager = new ShopManager();
	private ConversationCreator conversationCreator;

	private I18N language;

	@Override
	public void onEnable() {
		instance = this;
		registerConfigurationSerializationClasses();
		{
			Path langFolder = getDataFolder().toPath().resolve("translations");

			if (!Files.exists(langFolder)) {
				try {
					Files.createDirectories(langFolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			I18N.copyDefaultFiles("translations", langFolder, false, getClass());
			language = new I18N("translations", langFolder, InventoryProfiles.getInstance().getCurrentLocale(),
					getLogger(), getClassLoader(), "Messages", "Items");
		}

		conversationCreator = new ConversationCreator(this);

		try {
			shopManager = ShopManager.fromFile(getDataFolder().toPath().resolve("saves.yml"));
		} catch (FileNotFoundException e) {
			// recoverable. Just create the file later on
			shopManager = new ShopManager();
		} catch (Exception e) {
			// well, problems here. Unforeseen or an IllegalArgumentException. Either way, shut yourself down and
			// don't modify the saves!
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

		if (!InventoryProfiles.hasMoneyHook()) {
			getLogger().log(Level.SEVERE,
					"No economy plugin found! This plugin won't work correctly!"
							+ " I am a generous god, so you will just receive the warning but the plugin will still " +
							"load. Or is it a curse?");
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				getShopManager().updateAllShops();
			}
		}.runTaskTimer(this, 0, getConfig().getInt("delay between shop updates"));

		if (getConfig().getBoolean("auto saver activated")) {
			new AutoSaver().runTaskTimer(this, 0, DurationParser
					.parseDurationToTicks(getConfig().getString("backup period"))
			);
		}

		// the language settings
		InventoryProfiles.getInstance().getHookManager().registerLanguageReceiverHook((changeType, newLanguage) -> {
			if (changeType == LanguageEventReceiverHook.ChangeType.RELOADED) {
				getLanguage().reload();
			}
			else if (changeType == LanguageEventReceiverHook.ChangeType.LOCALE_SET) {
				getLanguage().setLanguage(newLanguage);
			}
			else {
				return;
			}
			reloadCommands();
		});
	}

	private void registerConfigurationSerializationClasses() {
		ConfigurationSerialization.registerClass(Shop.class);
	}

	/**
	 * Reloads all the commands by removing and then re-registering them.
	 * <p>
	 * <br>If this is async, you may experience <b><i>some</i></b> issues.
	 */
	private void reloadCommands() {
		CommandTree treeManager = new CommandTree(getLanguage());

		CommandIpSignShop commandIpSignShop = new CommandIpSignShop();

		treeManager.addChild(commandIpSignShop, new DefaultHelpCommand(getLanguage(), treeManager, "command_help"));

		treeManager.addChild(commandIpSignShop);

		getCommand("ipSignShop").setExecutor(new DefaultCommandExecutor(treeManager, language));
		getCommand("ipSignShop").setTabCompleter(new DefaultTabCompleter(treeManager));
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
