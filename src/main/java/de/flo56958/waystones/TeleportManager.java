package de.flo56958.waystones;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class TeleportManager {
	private static final HashMap<String, Integer> playerCountdown = new HashMap<>();
	private static final HashMap<String, Location> playerLocations = new HashMap<>();

	public static void initTeleportation(Player p, Location to, int time) {
		p.closeInventory();
		if (time == 0) {
			teleport(p, to);
		} else {
			playerLocations.put(p.getUniqueId().toString(), p.getLocation().clone());
			playerCountdown.put(p.getUniqueId().toString(), time);
			Runnable run = new Runnable() {
				@Override
				public void run() {
					if (!p.isOnline()) return;
					int t = playerCountdown.get(p.getUniqueId().toString());
					Location older = playerLocations.get(p.getUniqueId().toString());
					Location newer = p.getLocation();
					if (!(newer.getWorld().getName().equals(older.getWorld().getName()) && newer.getBlockX() == older.getBlockX()
							&& newer.getBlockY() == older.getBlockY() && newer.getBlockZ() == older.getBlockZ())) {
						Main.sendActionBar(p, "Teleport aborted!");
						return;
					}
					if (t == 0) {
						Main.sendActionBar(p, ""); //Empty String to clear Actionbar
						teleport(p, to);
					} else {
						Main.sendActionBar(p, "Teleport in " + t);
						playerCountdown.put(p.getUniqueId().toString(), t - 1);
						if (Main.plugin.getConfig().getBoolean("EnvironmentEffects", true)) {
							p.getLocation().getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), (int) (128 * (time - t / (double) time)));
						}
						Bukkit.getScheduler().runTaskLater(Main.plugin, this, 20);
					}
				}
			};
			run.run();
		}
	}

	private static void teleport(Player p, Location to) {
		if (Main.plugin.getConfig().getBoolean("EnvironmentEffects", true)) {
			p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
			p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
			p.getLocation().getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 128);
		}
		p.teleport(to);
		if (Main.plugin.getConfig().getBoolean("EnvironmentEffects", true)) {
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
				to.getWorld().playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
				to.getWorld().spawnParticle(Particle.PORTAL, to, 128);
			}, 2);
		}
	}
}
