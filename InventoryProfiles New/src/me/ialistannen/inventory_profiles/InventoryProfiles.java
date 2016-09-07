package me.ialistannen.inventory_profiles;

import me.ialistannen.bukkitutil.commandsystem.base.CommandTree;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultCommandExecutor;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultHelpCommand;
import me.ialistannen.bukkitutil.commandsystem.implementation.DefaultTabCompleter;
import me.ialistannen.inventory_profiles.commands.CommandInventoryProfiles;
import me.ialistannen.inventory_profiles.conversations.ConversationManager;
import me.ialistannen.inventory_profiles.hooks.HookManager;
import me.ialistannen.inventory_profiles.hooks.MoneyHook;
import me.ialistannen.inventory_profiles.hooks.RegionHook;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.listener.BuySignListener;
import me.ialistannen.inventory_profiles.listener.PlayerListener;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.players.ProfileManager;
import me.ialistannen.inventory_profiles.runnables.AutoSaver;
import me.ialistannen.inventory_profiles.runnables.PlaytimeChecker;
import me.ialistannen.inventory_profiles.signs.BuySign;
import me.ialistannen.inventory_profiles.signs.SignManager;
import me.ialistannen.inventory_profiles.util.DurationParser;
import me.ialistannen.inventory_profiles.util.LocationSerializable;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.languageSystem.I18N;
import me.ialistannen.languageSystem.MessageProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The main class for InventoryProfiles
 */
public class InventoryProfiles extends JavaPlugin {

	private static InventoryProfiles instance;
	private ProfileManager profileManager;
	private ConversationManager conversationManager;
	private HookManager hookManager;
	private SignManager signManager;

	private MessageProvider language;

	@Override
	public void onEnable() {
		instance = this;
		registerConfigurationSerializableClasses();

		saveDefaultConfig();

		{
			Path languageFolderPath = getDataFolder().toPath().resolve("translations");
			try {
				Files.createDirectories(languageFolderPath);
			} catch (IOException e) {
				e.printStackTrace();
			}

			I18N.copyDefaultFiles("translations", languageFolderPath, false, this.getClass());

			language = new I18N("translations", languageFolderPath,
					Locale.forLanguageTag(getConfig().getString("language")), getLogger(), getClassLoader(),
					"Messages");
		}

		hookManager = new HookManager();

		profileManager = new ProfileManager(getDataFolder().toPath().resolve("profileSave.yml"));
		signManager = new SignManager(getDataFolder().toPath().resolve("signSave.yml"));
		conversationManager = new ConversationManager(this);

		reloadCommands();

		new PlaytimeChecker().runTaskTimer(this, 0, 20);

		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new BuySignListener(), this);

		if (getConfig().getBoolean("auto saver activated")) {
			new AutoSaver().runTaskTimer(this, 0,
					DurationParser.parseDurationToTicks(getConfig().getString("backup period")));
		}
	}

	private void registerCommands() {
		CommandTree treeManager = new CommandTree(getLanguage());
		CommandExecutor commandExecutor = new DefaultCommandExecutor(treeManager, getLanguage());
		TabCompleter tabCompleter = new DefaultTabCompleter(treeManager);

		CommandInventoryProfiles commandInventoryProfiles = new CommandInventoryProfiles();

		// add help command
		treeManager.addChild(commandInventoryProfiles,
				new DefaultHelpCommand(getLanguage(), treeManager, "command_help"));

		// add Ip command
		treeManager.addChild(commandInventoryProfiles);

		getCommand("inventoryProfiles").setExecutor(commandExecutor);
		getCommand("inventoryProfiles").setTabCompleter(tabCompleter);
	}

	@Override
	public void onDisable() {
		Bukkit.getOnlinePlayers()
				.forEach(Util::updateSavedPlayerData);

		getConversationManager().cancelAllConversations();
		getProfileManager().save();
		getSignManager().save();
		instance = null;
	}

	private void registerConfigurationSerializableClasses() {
		ConfigurationSerialization.registerClass(Profile.class);
		ConfigurationSerialization.registerClass(LocationSerializable.class);
		ConfigurationSerialization.registerClass(RegionObject.class);
		ConfigurationSerialization.registerClass(BuySign.class);
	}

	/**
	 * Reloads all the commands by removing and then re-registering them.
	 * <p>
	 * <br>If this is async, you may experience <b><i>some</i></b> issues.
	 */
	public void reloadCommands() {
		registerCommands();
	}

	/**
	 * @return The language currently used by this plugin
	 */
	public Locale getCurrentLocale() {
		return getLanguage().getLanguage();
	}

	/**
	 * @return The {@link MessageProvider}
	 */
	public MessageProvider getLanguage() {
		return language;
	}


	/**
	 * @return The instance of this class.
	 */
	public static InventoryProfiles getInstance() {
		return instance;
	}

	/**
	 * @return The profile manager
	 */
	public static ProfileManager getProfileManager() {
		return getInstance().profileManager;
	}

	/**
	 * @return The {@link ConversationManager}
	 */
	public static ConversationManager getConversationManager() {
		return getInstance().conversationManager;
	}

	/**
	 * Returns the HookManager. Other plugins may use it to register their Language change hooks.
	 *
	 * @return The {@link HookManager}
	 */
	public HookManager getHookManager() {
		return hookManager;
	}

	/**
	 * @return If this has a money hook
	 */
	public static boolean hasMoneyHook() {
		return getInstance().getHookManager().getMoneyHook().isWorking();
	}

	/**
	 * @return The money hook
	 */
	public static MoneyHook getMoneyHook() {
		return getInstance().getHookManager().getMoneyHook();
	}

	/**
	 * @return If this has a region hook
	 */
	public static boolean hasRegionHook() {
		return getInstance().getHookManager().getRegionHook().isWorking();
	}

	/**
	 * @return The region hook
	 */
	public static RegionHook getRegionHook() {
		return getInstance().getHookManager().getRegionHook();
	}

	/**
	 * @return The {@link SignManager}
	 */
	public static SignManager getSignManager() {
		return getInstance().signManager;
	}
}
