package me.ialistannen.inventory_profiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.ialistannen.inventory_profiles.commands.CommandAddRegion;
import me.ialistannen.inventory_profiles.commands.CommandBan;
import me.ialistannen.inventory_profiles.commands.CommandCreate;
import me.ialistannen.inventory_profiles.commands.CommandDelete;
import me.ialistannen.inventory_profiles.commands.CommandHelp;
import me.ialistannen.inventory_profiles.commands.CommandHome;
import me.ialistannen.inventory_profiles.commands.CommandLanguageReload;
import me.ialistannen.inventory_profiles.commands.CommandLanguageSet;
import me.ialistannen.inventory_profiles.commands.CommandListRegions;
import me.ialistannen.inventory_profiles.commands.CommandLogout;
import me.ialistannen.inventory_profiles.commands.CommandLookupPassword;
import me.ialistannen.inventory_profiles.commands.CommandMember;
import me.ialistannen.inventory_profiles.commands.CommandMoney;
import me.ialistannen.inventory_profiles.commands.CommandPay;
import me.ialistannen.inventory_profiles.commands.CommandRemoveRegion;
import me.ialistannen.inventory_profiles.commands.CommandSellRegion;
import me.ialistannen.inventory_profiles.commands.CommandSetHome;
import me.ialistannen.inventory_profiles.commands.CommandSetPassword;
import me.ialistannen.inventory_profiles.commands.CommandSetPlaytime;
import me.ialistannen.inventory_profiles.commands.CommandSetPlaytimeModifier;
import me.ialistannen.inventory_profiles.commands.CommandShowPlaytime;
import me.ialistannen.inventory_profiles.commands.CommandUnban;
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
import me.ialistannen.tree_command_system.CommandTreeCommandListener;
import me.ialistannen.tree_command_system.CommandTreeManager;
import me.ialistannen.tree_command_system.CommandTreeTabCompleteListener;

/**
 * The main class for InventoryProfiles
 */
public class InventoryProfiles extends JavaPlugin {

	private static InventoryProfiles instance;
	private ProfileManager profileManager;
	private ConversationManager conversationManager;
	private MoneyHook moneyHook;
	private RegionHook regionHook;
	private SignManager signManager;
	
	private CommandTreeManager treeManager;
	
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
					Locale.forLanguageTag(getConfig().getString("language")), getLogger(), getClassLoader(), "Messages");
		}
		
		moneyHook = HookManager.getMoneyHook();
		regionHook = HookManager.getRegionHook();
		
		profileManager = new ProfileManager(getDataFolder().toPath().resolve("profileSave.yml"));
		signManager = new SignManager(getDataFolder().toPath().resolve("signSave.yml"));
		conversationManager = new ConversationManager(this);
		
		reloadCommands();
				
		new PlaytimeChecker().runTaskTimer(this, 0, 20);
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new BuySignListener(), this);
		
		if(getConfig().getBoolean("auto saver activated")) {
			new AutoSaver().runTaskTimer(this, 0, DurationParser.parseDurationToTicks(getConfig().getString("backup period")));
		}
	}
	
	private void registerCommands() {
		treeManager.registerChild(treeManager.getRoot(), new CommandHelp());
		treeManager.registerChild(treeManager.getRoot(), new CommandLogout());
		treeManager.registerChild(treeManager.getRoot(), new CommandCreate());
		treeManager.registerChild(treeManager.getRoot(), new CommandDelete());
		treeManager.registerChild(treeManager.getRoot(), new CommandMoney());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetPlaytime());
		treeManager.registerChild(treeManager.getRoot(), new CommandShowPlaytime());
		treeManager.registerChild(treeManager.getRoot(), new CommandLookupPassword());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetPassword());
		treeManager.registerChild(treeManager.getRoot(), new CommandBan());
		treeManager.registerChild(treeManager.getRoot(), new CommandUnban());
		treeManager.registerChild(treeManager.getRoot(), new CommandPay());
		treeManager.registerChild(treeManager.getRoot(), new CommandLanguageReload());
		treeManager.registerChild(treeManager.getRoot(), new CommandLanguageSet());
		treeManager.registerChild(treeManager.getRoot(), new CommandAddRegion());
		treeManager.registerChild(treeManager.getRoot(), new CommandRemoveRegion());
		treeManager.registerChild(treeManager.getRoot(), new CommandSellRegion());
		treeManager.registerChild(treeManager.getRoot(), new CommandListRegions());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetPlaytimeModifier());
		treeManager.registerChild(treeManager.getRoot(), new CommandMember());
		treeManager.registerChild(treeManager.getRoot(), new CommandSetHome());
		treeManager.registerChild(treeManager.getRoot(), new CommandHome());
	}
	
	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Util.updateSavedPlayerData(player);
		}
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
	 * 
	 * <br>If this is async, you may experience <b><i>some</i></b> issues.
	 */
	public void reloadCommands() {
		getCommand("inventoryProfiles").setTabCompleter(null);
		getCommand("inventoryProfiles").setExecutor(null);
		treeManager = new CommandTreeManager();
		registerCommands();
		getCommand("inventoryProfiles").setExecutor(new CommandTreeCommandListener(treeManager, language));
		getCommand("inventoryProfiles").setTabCompleter(new CommandTreeTabCompleteListener(treeManager, true));
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
	 * @return If this has a money hook
	 */
	public static boolean hasMoneyHook() {
		return getInstance().moneyHook.isWorking();
	}
	
	/**
	 * @return The money hook
	 */
	public static MoneyHook getMoneyHook() {
		return getInstance().moneyHook;
	}
	
	/**
	 * @return If this has a rgion hook
	 */
	public static boolean hasRegionHook() {
		return getInstance().regionHook.isWorking();
	}
	
	/**
	 * @return The region hook
	 */
	public static RegionHook getRegionHook() {
		return getInstance().regionHook;
	}
	
	/**
	 * @return The tree manager
	 */
	public CommandTreeManager getTreeManager() {
		return getInstance().treeManager;
	}
	
	/**
	 * @return The {@link SignManager}
	 */
	public static SignManager getSignManager() {
		return getInstance().signManager;
	}
}
