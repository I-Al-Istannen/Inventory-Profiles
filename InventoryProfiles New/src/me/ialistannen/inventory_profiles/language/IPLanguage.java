package me.ialistannen.inventory_profiles.language;

import static me.ialistannen.inventory_profiles.util.Util.color;

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

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.hooks.IPSignShopHook;

/**
 * The Language class for IP
 */
public class IPLanguage {

	private static IPLanguage instance;
	private static final String LANGUAGE_LOG_PREFIX = "[Language] ";
	/**
	 * Matches two double quotes. To be used to remove them from the translated strings, if they should occure.
	 */
	private static final Pattern REMOVE_DOUBLE_QUOTES = Pattern.compile("''");
	
	/**
	 * The name of the folder the translations are stored on on disk
	 */
	public static final String FOLDER_NAME = "translations";
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
	public IPLanguage() {
		instance = this;
		fillDefault();
		
		String defaultLocaleName = InventoryProfiles.getInstance().getConfig().getString("language");
		if(defaultLocaleName == null) {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "No language specified in config. Using default 'en'");
			defaultLocaleName = "en";
		}
		else {
			InventoryProfiles.getInstance().getLogger().info(LANGUAGE_LOG_PREFIX + "Language specified in config: '" + defaultLocaleName + "'.");
		}
		
		locale = Locale.forLanguageTag(defaultLocaleName);
		
		updateLocale(locale);
	}
	
	/**
	 * Nullifies the instance to prevent memory leaks after reload. Is that even a thing?
	 */
	public static void nullify() {
		ResourceBundle.clearCache();
		instance = null;
	}
	
	private void fillDefault() {
		defaultBundles.put(MESSAGES, ResourceBundle.getBundle("translations." + MESSAGES, Locale.ENGLISH));
	}
	
	/**
	 * Translates and formats the value for a key. An errir is thrown if the key is missing from every file
	 * 
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
	 * @param key The Key to get the String for
	 * @param fileBundle The Filebundle to read from. Currently only {@link #MESSAGES}
	 * @return The translated String. Got from the fileBundle, then the LocaleBundle and then the defaultBundle
	 */
	public String translate(String key, String fileBundle) {
		try {
			try {
				if(fileBundles.containsKey(MESSAGES)) {
					return fileBundles.get(fileBundle).getString(key);
				}
			} catch (MissingResourceException e) {}
			
			return localeBundles.get(fileBundle).getString(key);
			
		} catch (Exception e) {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + String.format("No translation found for key %s.", key), e); 
			return defaultBundles.get(fileBundle).getString(key);
		}
	}
	
	/**
	 * @param key The key to the message
	 * @param fileBundle The Filebundle to read from. Currently only {@link #MESSAGES}
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
				InventoryProfiles.getInstance().getLogger().log(Level.SEVERE, LANGUAGE_LOG_PREFIX + "Invalid translation key for '" + key + "'", e);
				format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");	// replace all Placeholders with []
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
		ResourceBundle.clearCache();	// interesting how that interferes with IPSS Language. TODO: Check it -> Seems to work
		messageFormatCache.clear();
				
		localeBundles.clear();
		fileBundles.clear();
		
		try {
			localeBundles.put(MESSAGES, ResourceBundle.getBundle("translations/" + MESSAGES, locale));
		} catch(MissingResourceException e) {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "No language file for "
					+ locale.getDisplayName() + " was found intern (in the Jar file).");			
		}
		
		try {
			fileBundles.put(MESSAGES, ResourceBundle.getBundle(MESSAGES, locale, new FileClassLoader(
					InventoryProfiles.getInstance().getDataFolder().toPath().resolve(FOLDER_NAME))));		
		} catch(MissingResourceException e) {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "No language file for "
					+ locale.getDisplayName() + " was found in the " + FOLDER_NAME + " folder.");
		}
		
		
		if(fileBundles.containsKey(MESSAGES)) {
			this.locale = fileBundles.get(MESSAGES).getLocale();
			InventoryProfiles.getInstance().getLogger().log(Level.INFO, LANGUAGE_LOG_PREFIX + "Using language " + this.locale.getDisplayName() + " (Folder)");
		}
		else if(localeBundles.containsKey(MESSAGES)) {
			this.locale = localeBundles.get(MESSAGES).getLocale();
			InventoryProfiles.getInstance().getLogger().log(Level.INFO, LANGUAGE_LOG_PREFIX + "Using language " + this.locale.getDisplayName() + " (Jar-File)");			
		}
		else {
			this.locale = defaultBundles.get(MESSAGES).getLocale();
			InventoryProfiles.getInstance().getLogger().log(Level.INFO, LANGUAGE_LOG_PREFIX + "Using language " + this.locale.getDisplayName() + " (Default)");
		}
		
		if(InventoryProfiles.getSignManager() != null) {
			InventoryProfiles.getSignManager().updateAllSigns();
		}
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
		IPSignShopHook.updateLanguage(getLocale());
	}
	
	/**
	 * @return The Instance
	 */
	private static IPLanguage getInstance() {
		if(instance == null) {
			instance = new IPLanguage();
		}
		return instance;
	}
	
	/**
	 * @param targetDir The Directory to copy to
	 * @param overwrite If true it will overwrite existing ones
	 */
	public static void copyMessageProperties(Path targetDir, boolean overwrite) {
		try(JarFile jar = new JarFile(new File(InventoryProfiles.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
			
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
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "Couldn't copy the Message.properties files", e);
		} catch (URISyntaxException uriSyntax) {
			InventoryProfiles.getInstance().getLogger().log(Level.WARNING, LANGUAGE_LOG_PREFIX + "Couldn't copy the Message.properties files", uriSyntax);
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
