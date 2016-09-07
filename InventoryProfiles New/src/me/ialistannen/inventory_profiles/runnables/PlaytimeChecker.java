package me.ialistannen.inventory_profiles.runnables;

import me.ialistannen.inventory_profiles.InventoryProfiles;
import me.ialistannen.inventory_profiles.players.Profile;
import me.ialistannen.inventory_profiles.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.ialistannen.inventory_profiles.util.Util.tr;

/**
 * Checks the playtime
 */
public class PlaytimeChecker extends BukkitRunnable {

	private final Map<String, LocalTime> lastCheckedMap = new HashMap<>();
	
	@Override
	public void run() {
		List<Long> warnTimes = InventoryProfiles.getInstance().getConfig().getStringList("playtime warn at times in minutes").stream()
				.map(Util::parseDurationString)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(Duration::getSeconds)
				.collect(Collectors.toList());
		
		List<Profile> players = Bukkit.getOnlinePlayers().stream()
				.filter(player -> InventoryProfiles.getProfileManager().hasProfile(player.getDisplayName()))
				.map(player -> InventoryProfiles.getProfileManager().getProfile(player.getDisplayName()).get())
				.collect(Collectors.toList());
		
		players.forEach(profile -> {
			if(lastCheckedMap.containsKey(profile.getName())) {
				profile.setUsedPlaytime(profile.getUsedPlaytime() + ChronoUnit.MILLIS.between(lastCheckedMap.get(profile.getName()), LocalTime.now()));
			}
			lastCheckedMap.put(profile.getName(), LocalTime.now());
			
			Duration playtimeLeft = profile.getPlaytimeLeft();
			
			// Sends a warn message to the user
			if(warnTimes.contains(playtimeLeft.getSeconds())) {
				sendWarnMessage(profile);
			}
						
			if(profile.hasNoPlaytimeLeft()) {
				profile.setPlaytimeWentOut(LocalDateTime.now());
			}
			InventoryProfiles.getProfileManager().checkAndKickPlayerPlaytime(profile);
		});

		lastCheckedMap.keySet().retainAll(players.stream().map(Profile::getName).collect(Collectors.toSet()));
	}

	private void sendWarnMessage(Profile profile) {
		profile.getPlayer().ifPresent(player -> player.sendMessage(tr("playtime amount warn message", Util.formatDuration(profile.getPlaytimeLeft()))));
	}
}
