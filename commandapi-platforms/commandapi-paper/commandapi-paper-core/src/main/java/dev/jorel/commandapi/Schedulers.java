package dev.jorel.commandapi;

import org.bukkit.plugin.Plugin;

public class Schedulers {

	private final boolean isFoliaPresent;

	public Schedulers(boolean isFoliaPresent) {
		this.isFoliaPresent = isFoliaPresent;
	}

	public int scheduleSyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
		if (isFoliaPresent) {
			plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay <= 0 ? 1 : delay, period);
			return 1;
		} else {
			return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period).getTaskId();
		}
	}

	public void cancelTask(Plugin plugin, int id) {
		if (isFoliaPresent) {
			plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
		} else {
			plugin.getServer().getScheduler().cancelTask(id);
		}
	}

	public void scheduleSync(Plugin plugin, Runnable runnable) {
		if (isFoliaPresent) {
			plugin.getServer().getGlobalRegionScheduler().execute(plugin, runnable);
		} else {
			plugin.getServer().getScheduler().runTask(plugin, runnable);
		}
	}

	public void scheduleSyncDelayed(Plugin plugin, Runnable runnable, long delay) {
		if (isFoliaPresent) {
			plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delay <= 0 ? 1 : delay);
		} else {
			plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
		}
	}

	public void scheduleAsync(Plugin plugin, Runnable runnable) {
		if (isFoliaPresent) {
			plugin.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run());
		} else {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
		}
	}

}
