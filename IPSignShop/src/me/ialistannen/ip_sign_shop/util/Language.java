package me.ialistannen.ip_sign_shop.util;

import static me.ialistannen.ip_sign_shop.util.IPSignShopUtil.color;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Material;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.ip_sign_shop.IPSignShop;

/**
 * The language system
 */
public class Language {
	
	private static Language instance;
	private static final String LANGUAGE_LOG_PREFIX = "[Language] ";
	/**
	 * Matches two double quotes. To be used to remove them from the translated strings
	 */
	private static final Pattern REMOVE_DOUBLE_QUOTES = Pattern.compile("''");
	
	/**
	 * The name of the folder the translations are stored on on disk
	 */
	public static final String FOLDER_NAME = "translations";
	/**
	 * If you want an item translation
	 */
	public static final String ITEMS = "Items";
	/**
	 * If you want to translate a message
	 */
	public static final String MESSAGES = "Messages";
	
	private Map<String, ResourceBundle> defaultBundles = new HashMap<>();
	private Map<String, ResourceBundle> localeBundles = new HashMap<>(), fileBundles = new HashMap<>();
	private Locale locale;
	private Map<String, MessageFormat> messageFormatCache = new HashMap<>();
	
	
	/**
	 * Constructs an instance
	 */
	public Language() {
		instance = this;
		fillDefault();
		updateLocale(InventoryProfiles.getInstance().getCurrentLocale());
	}
	
	private void fillDefault() {
		defaultBundles.put(MESSAGES, ResourceBundle.getBundle("translations." + MESSAGES, Locale.ENGLISH));
		defaultBundles.put(ITEMS, ResourceBundle.getBundle("translations." + ITEMS, Locale.ENGLISH));
	}
	
	/**
	 * @param key The key to get
	 * @param formatObjects The Arguments to translate
	 * @return The translated and colored String
	 */
	public static String tr(String key, Object... formatObjects) {
		if(getInstance() == null) {
			return "";
		}

		if(formatObjects.length == 0) {
			return REMOVE_DOUBLE_QUOTES.matcher(color(getInstance().translate(key, MESSAGES))).replaceAll("'");
		}
		
		return REMOVE_DOUBLE_QUOTES.matcher(color(getInstance().format(key, MESSAGES, formatObjects))).replaceAll("'");
	}
	
	/**
	 * @param item The Material to translate
	 * @return The translated Material
	 */
	public static String translateItemName(Material item) {
		if(getInstance() == null) {
			return "";
		}
		
		return REMOVE_DOUBLE_QUOTES.matcher(color(getInstance().translate(item.name(), ITEMS))).replaceAll("'");
	}
	
	/**
	 * @param key The Key to get the String for
	 * @param fileBundle The Filebundle to read from. Use {@link #MESSAGES} or {@link #ITEMS}
	 * @return The translated String. Got from the fileBundle, then the LocaleBundle and then the defaultBundle
	 */
	public String translate(String key, String fileBundle) {
		try {
			try {
				return fileBundles.get(fileBundle).getString(key);
			} catch (MissingResourceException e) {}
			
			return localeBundles.get(fileBundle).getString(key);
			
		} catch (Exception e) {
			IPSignShop.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + String.format("No translation found for key %s.", key), e); 
			return defaultBundles.get(fileBundle).getString(key);
		}
	}
	
	/**
	 * @param key The key to the message
	 * @param fileBundle The Filebundle to read from. Use {@link #MESSAGES} or {@link #ITEMS}
	 * @param objects The Parameters to use for formatting
	 * @return The formatted String
	 */
	public String format(String key, String fileBundle, Object... objects) {
		String format = translate(key, fileBundle);
		MessageFormat messageFormat = messageFormatCache.get(format);
		if(messageFormat == null) {
			try {
				messageFormat = new MessageFormat(format);
			} catch(IllegalArgumentException e) {
				IPSignShop.getInstance().getLogger().log(Level.SEVERE, LANGUAGE_LOG_PREFIX + "Invalid translation key for '" + key + "'", e);
				format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
				messageFormat = new MessageFormat(format);
			}
			messageFormatCache.put(format, messageFormat);
		}
		
		return messageFormat.format(objects);
	}
	
	/**
	 * @param locale The new locale
	 */
	public void updateLocale(Locale locale) {
		ResourceBundle.clearCache();
		messageFormatCache.clear();
		
		Locale newLocale = this.locale;
		
		localeBundles.clear();
		fileBundles.clear();
		
		try {
			localeBundles.put(MESSAGES, ResourceBundle.getBundle("translations/" + MESSAGES, locale));
			localeBundles.put(ITEMS, ResourceBundle.getBundle("translations/" + ITEMS, locale));			
			newLocale = localeBundles.get(MESSAGES).getLocale();
		} catch(MissingResourceException e) {
			
		}
		
		try {
			fileBundles.put(MESSAGES, ResourceBundle.getBundle(MESSAGES, locale, new FileClassLoader(IPSignShop.getInstance().getDataFolder().toPath().resolve(FOLDER_NAME))));
			fileBundles.put(ITEMS, ResourceBundle.getBundle(ITEMS, locale, new FileClassLoader(IPSignShop.getInstance().getDataFolder().toPath().resolve(FOLDER_NAME))));
			newLocale = fileBundles.get(MESSAGES).getLocale();
		} catch(MissingResourceException e) {
			
		}
		
		IPSignShop.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "Using language " + newLocale.getDisplayName());
		this.locale = locale;
		IPSignShop.getShopManager().updateAllShops();
	}
	
	/**
	 * @return The current Locale
	 */
	public static Locale getLocale() {
		return getInstance().locale;
	}
	
	/**
	 * @param locale The new Locale
	 */
	public static void setLocale(Locale locale) {
		getInstance().updateLocale(locale);
	}
	
	/**
	 * @return The Instance
	 */
	private static Language getInstance() {
		if(instance == null) {
			instance = new Language();
		}
		return instance;
	}
	
	/**
	 * @param targetDir The Directory to copy to
	 * @param overwrite If true it will overwrite existing ones
	 */
	public static void copyMessageProperties(Path targetDir, boolean overwrite) {
		try(JarFile jar = new JarFile(new File(IPSignShop.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
			
			Enumeration<JarEntry> entries = jar.entries();
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if(!entry.getName().startsWith("translations/")) {
					continue;
				}
				
				Path path = targetDir.resolve(entry.getName().replaceAll(".+\\/", ""));
				
				if(!Files.exists(path)) {
					Files.createDirectories(targetDir);
					Files.createFile(path);
					Files.copy(jar.getInputStream(entry), path, StandardCopyOption.REPLACE_EXISTING);
				}
				else if(overwrite) {
					Files.delete(path);
					Files.copy(jar.getInputStream(entry), path, StandardCopyOption.REPLACE_EXISTING);
				}
				else {
					continue;
				}
			}
		} catch (IOException e) {
			IPSignShop.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "Couldn't copy the Message/Item.properties files", e);
		} catch (URISyntaxException uriSyntax) {
			IPSignShop.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "Couldn't copy the Message/Item.properties files", uriSyntax);
		}
	}
	
	/**
	 * A Classloader to load from file
	 */
	private static class FileClassLoader extends ClassLoader {
		Path basePath;
		
		/**
		 * @param path The Path to the folder
		 */
		public FileClassLoader(Path path) {
			this.basePath = path;
		}
		
		@Override
		public URL getResource(String name) {
			try {
				Path resolved = basePath.resolve(name);
				if(Files.exists(resolved)) {
					return resolved.toUri().toURL();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		public InputStream getResourceAsStream(String name) {
			if(name == null) {
				throw new NullPointerException("Name is null");
			}
			
			try {
				URL path = getResource(name);
				if(path == null) {
					return null;
				}
				
				Path pathObj = Paths.get(path.toURI());
				return Files.newInputStream(pathObj);
				
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
