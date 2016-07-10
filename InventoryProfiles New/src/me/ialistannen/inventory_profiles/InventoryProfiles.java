package me.ialistannen.inventory_profiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.ialistannen.inventory_profiles.commands.CommandManager;
import me.ialistannen.inventory_profiles.commands.IPTabCompleter;
import me.ialistannen.inventory_profiles.conversations.ConversationManager;
import me.ialistannen.inventory_profiles.hooks.HookManager;
import me.ialistannen.inventory_profiles.hooks.MoneyHook;
import me.ialistannen.inventory_profiles.hooks.RegionHook;
import me.ialistannen.inventory_profiles.hooks.RegionHook.RegionObject;
import me.ialistannen.inventory_profiles.listener.BuySignListener;
import me.ialistannen.inventory_profiles.listener.PlayerListener;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.players.ProfileManager;
import me.ialistannen.inventory_profiles.runnables.PlaytimeChecker;
import me.ialistannen.inventory_profiles.signs.BuySign;
import me.ialistannen.inventory_profiles.signs.SignManager;
import me.ialistannen.inventory_profiles.util.LocationSerializable;
import me.ialistannen.inventory_profiles.util.Util;
import me.ialistannen.languageSystem.I18N;
import me.ialistannen.languageSystem.MessageProvider;

/**
 * The main class for InventoryProfiles
 */
public class InventoryProfiles extends JavaPlugin {

	private static InventoryProfiles instance;
	private ProfileManager profileManager;
	private ConversationManager conversationManager;
	private MoneyHook moneyHook;
	private RegionHook regionHook;
	private CommandManager commandManager;
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
					Locale.forLanguageTag(getConfig().getString("language")), getLogger(), getClassLoader(), "Messages");
		}
		
		moneyHook = HookManager.getMoneyHook();
		regionHook = HookManager.getRegionHook();
		
		profileManager = new ProfileManager(getDataFolder().toPath().resolve("profileSave.yml"));
		signManager = new SignManager(getDataFolder().toPath().resolve("signSave.yml"));
		conversationManager = new ConversationManager(this);
		commandManager = new CommandManager();
		
		getCommand("inventoryProfiles").setExecutor(getCommandManager());
		getCommand("inventoryProfiles").setTabCompleter(new IPTabCompleter());
				
		new PlaytimeChecker().runTaskTimer(this, 0, 20);
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new BuySignListener(), this);
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
	 * @return The {@link CommandManager}
	 */
	public static CommandManager getCommandManager() {
		return getInstance().commandManager;
	}
	
	/**
	 * @return The {@link SignManager}
	 */
	public static SignManager getSignManager() {
		return getInstance().signManager;
	}
}
