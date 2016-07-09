package me.ialistannen.inventory_profiles.util;

import org.bukkit.entity.Player;

/**
 * Some function for Experience things
 */
public class ExpUtil {

	/**
	 * @param level The level
	 * @return The total exp needed to get to the level
	 */
	public static int getXpForLevel(int level) {
		int totalXp = 0;
		for(int i = 1; i <= level; i++) {	
			totalXp += getXpIncreaseAtLevel(i);
		}
		
		return totalXp;
	}
	
	/**
	 * @param level The Level
	 * @return The xp increase at the given level
	 */
	public static int getXpIncreaseAtLevel(int level) {
		int xpPerLevel = 0;
		for(int i = 1; i <= level; i++) {
			if(i == 1) {
				xpPerLevel += 7;
			}
			else if(i <= 16) {
				xpPerLevel += 2;
			}
			else if(i <= 31) {
				xpPerLevel += 5;
			}
			else {
				xpPerLevel += 9;
			}
		}
		
		return xpPerLevel;		
	}
	
	/**
	 * @param level The level the user is at
	 * @param progress The Progress he has made
	 * @return The Extra xp that is
	 */
	public static int getExtraXp(int level, float progress) {
		return Math.round(getXpIncreaseAtLevel(level) * progress);
	}
	
	/**
	 * @param level The Level of the player
	 * @param progress The extra progress
	 * @return The total Experience for the player
	 */
	public static int getTotalXp(int level, float progress) {
		return getXpForLevel(level) + getExtraXp(level, progress);
	}
	
	/**
	 * @param xp The Experience of the player
	 * @return The level the player has.
	 */
	public static int getLevel(int xp) {
		int level = 1;
		for(; xp >= getXpIncreaseAtLevel(level) ; level++) {
			xp -= getXpIncreaseAtLevel(level);
		}
		return level - 1;
	}
	
	/**
	 * @param totalXp The Total xp (level + extra)
	 * @param level The level the player is at
	 * @return The Extra percentage the player is at
	 */
	public static float getExtraXpForLevel(int totalXp, int level) {
		totalXp -= getXpForLevel(level);
		return (float) totalXp / getXpIncreaseAtLevel(level);
	}
	
	/**
	 * Sets the Player's experience to the specified amount
	 * 
	 * @param player The player to set the xp for
	 * @param xp The xp to set it to
	 */
	public static void setXp(Player player, int xp) {
		player.setLevel(getLevel(xp));
		player.setExp(getExtraXpForLevel(xp, getLevel(xp)));
	}
}
