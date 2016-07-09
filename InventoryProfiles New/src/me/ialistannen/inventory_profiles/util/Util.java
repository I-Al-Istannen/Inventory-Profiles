package me.ialistannen.inventory_profiles.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.language.IPLanguage;
import me.ialistannen.inventory_profiles.players.Profile;

/**
 * A class with Util methods
 */
public class Util {

	private static final Pattern DAYS = Pattern.compile("((-|\\+)?\\d+d)"), HOURS = Pattern.compile("((-|\\+)?\\d+h)"), MINUTES = Pattern.compile("((-|\\+)?\\d+m)")
			, SECONDS = Pattern.compile("((-|\\+)?\\d+s)"), MILLIS = Pattern.compile("((-|\\+)?\\d+ms)");
	
	/**
	 * The prefix for all permissions. Doesn't end with ".".
	 */
	public static final String PERMISSION_PREFIX = "inventoryProfiles";
	
	/**
	 * Color char is '&'. Uses {@link ChatColor#translateAlternateColorCodes(char, String)}.
	 * 
	 * @param input The Input to color
	 * @return The colored input
	 */
	public static String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	
	/**
	 * Updates the player data
	 * @param player The Player to update it for
	 * @return True if the data was saved successfully
	 */
	public static boolean updateSavedPlayerData(Player player) {
		Optional<Profile> profileOpt = InventoryProfiles.getProfileManager().getProfile(player.getDisplayName());
		
		if(!profileOpt.isPresent()) {
			return false;
		}
		
		Profile profile = profileOpt.get();
		List<ItemStack> playerItems = new ArrayList<>();
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			playerItems.add(i, player.getInventory().getItem(i));
		}
		
		playerItems.add(player.getInventory().getBoots());
		playerItems.add(player.getInventory().getLeggings());
		playerItems.add(player.getInventory().getChestplate());
		playerItems.add(player.getInventory().getHelmet());
		
		profile.setItems(playerItems);
		profile.setLastLogoutLocation(player.getLocation());
		profile.setXp(ExpUtil.getTotalXp(player.getLevel(), player.getExp()));
		profile.setGameMode(player.getGameMode());
		
		if(InventoryProfiles.hasMoneyHook()) {
			profile.setMoney(InventoryProfiles.getMoneyHook().getBalance(player));
		}
				
		return true;
	}
	
	/**
	 * The Pattern:
	 * <br>dd => Days
	 * <br>hh => Hours
	 * <br>mm => Minutes
	 * <br>ss => Seconds
	 * <br>SS => Miliseconds
	 * 
	 * @param duration The Duration
	 * @param pattern The Pattern
	 * @return The formatted Duration
	 */
	public static String formatDuration(Duration duration, String pattern) {
		boolean negative = duration.isNegative();
		long days = duration.toDays();
		duration = duration.minusDays(days);
		long hours = duration.toHours();
		duration = duration.minusHours(hours);
		long minutes = duration.toMinutes();
		duration = duration.minusMinutes(minutes);
		long seconds = duration.getSeconds();
		duration = duration.minusSeconds(seconds);
		long milis = duration.toMillis();
				
		return (negative ? "-" : "") + pattern.
				replace("dd", Math.abs(days) + "")
				.replace("hh", Math.abs(hours) + "")
				.replace("mm", Math.abs(minutes) + "")
				.replace("ss", Math.abs(seconds) + "")
				.replace("SS", Math.abs(milis) + "");
	}
	
	/**
	 * Uses the format specified in the config.yml, key = duration pattern
	 * 
	 * @param duration The Duration to format
	 * @return The formatted String.
	 */
	public static String formatDuration(Duration duration) {
		return formatDuration(duration, InventoryProfiles.getInstance().getConfig().getString("duration pattern"));
	}
	
	/**
	 * @param string The String to trim
	 * @param size The Size to trim to
	 * @return The trimmed String or the origian lone if it was inside the range
	 */
	public static String trimToSize(String string, int size) {
		return string.length() <= size ? string : string.substring(0, size);
	}
	
	/**
	 * @param input The input to parse
	 * @return A boolean or an empty optional if it didn't equal "true" or "false", ignoring case
	 */
	public static Optional<Boolean> getBoolean(String input) {
		if(input.trim().equalsIgnoreCase("true")) {
			return Optional.of(true);
		}
		else if(input.trim().equalsIgnoreCase("false")) {
			return Optional.of(false);
		}
		
		return Optional.empty();
	}
	
	/**
	 * @param input The Input to parse
	 * @return The integer or an emtpy optional if it is an incorrect format
	 */
	public static Optional<Integer> getInteger(String input) {
		try {
			return Optional.of(Integer.parseInt(input));
		} catch(NumberFormatException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * @param input The Input to parse
	 * @return The Double or an emtpy optional if it is an incorrect format
	 */
	public static Optional<Double> getDouble(String input) {
		try {
			return Optional.of(NumberFormat.getNumberInstance(IPLanguage.getLocale()).parse(input).doubleValue());
		} catch (ParseException e) {
			return Optional.empty();
		}
	}
	
	
	/**
	 * Format is xx<b>d</b> xx<b>h</b> xx<b>m</b> xx<b>s</b> xx<b>ms</b>
	 * <br>Also accepts any group negated with a '-' sign.
	 * 
	 * @param input The input.
	 * @return The parsed Duration or an empty optional if it wasn't a valid String
	 */
	public static Optional<Duration> parseDurationString(String input) {
		Optional<Integer> days = getIntResultOfFirstGroup(DAYS, input);
		Optional<Integer> hours = getIntResultOfFirstGroup(HOURS, input);
		Optional<Integer> minutes = getIntResultOfFirstGroup(MINUTES, input);
		Optional<Integer> seconds = getIntResultOfFirstGroup(SECONDS, input);
		Optional<Integer> millis = getIntResultOfFirstGroup(MILLIS, input);
		
		if(!days.isPresent() && !hours.isPresent() && !minutes.isPresent() && !seconds.isPresent() && !millis.isPresent()) {
			return Optional.empty();
		}

		return Optional.of(Duration.parse("P" + days.orElse(0) + "DT" + hours.orElse(0) + "H" + minutes.orElse(0)
				+ "M" + seconds.orElse(0) + "." + millis.orElse(0) + "S"));
	}
	
	/**
	 * @param pattern The Pattern to use
	 * @param input The String to match
	 * @return The Integer that the first group of the pattern represents or an empty optional if not found or in invalid format
	 */
	private static Optional<Integer> getIntResultOfFirstGroup(Pattern pattern, String input) {
		Matcher matcher = pattern.matcher(input);
		if(matcher.find()) {
			String integer = matcher.group(1).replaceAll("[^\\d-+.+]", "");
			return getInteger(integer);
		}
		return Optional.empty();
	}
	
	/**
	 * @return The delay until the playtime rests again
	 */
	public static Optional<Duration> getPlaytimeResetDelay() {
		return Util.parseDurationString(InventoryProfiles.getInstance().getConfig().getString("playtime reset delay"));
	}
	
	/**
	 * Formats enum constants according to the german language
	 * 
	 * @param input The Input String
	 * @return The name in lowercase, but with every word starting with an uppercase char
	 */
	public static String getNiceNameForConstant(String input) {
		StringBuilder builder = new StringBuilder();
		
		boolean upperCase = true;
		for (char c : input.toCharArray()) {
			if(c == '_') {
				upperCase = true;
				builder.append(" ");
				continue;
			}
			
			if(upperCase) {
				builder.append(Character.toUpperCase(c));
				upperCase = false;
			}
			else {
				builder.append(Character.toLowerCase(c));
			}
		}
		
		return builder.toString();
	}
}
