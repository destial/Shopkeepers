package com.nisovin.shopkeepers.shopcreation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.TextUtils;

class ShopCreationItemSelectionTask implements Runnable {

	/**
	 * The time in ticks before we send the shop creation item selection message.
	 * <p>
	 * We only send the message if the player is still holding the item after this delay. This avoid message spam
	 * when the player quickly scrolls through the items on the hotbar via the mouse wheel.
	 */
	private static final long DELAY_TICKS = 5L; // 0.25 seconds

	// By player UUID:
	private static final Map<UUID, ShopCreationItemSelectionTask> activeTasks = new HashMap<>();

	/**
	 * Starts this task for the given player.
	 * <p>
	 * Any already active task for the player is cancelled.
	 * 
	 * @param plugin
	 *            the plugin, not <code>null</code>
	 * @param player
	 *            the player, not <code>null</code>
	 */
	static void start(Plugin plugin, Player player) {
		assert plugin != null && player != null;
		// If there is already an active task, we cancel and restart it (i.e. we reuse it):
		ShopCreationItemSelectionTask task = activeTasks.computeIfAbsent(player.getUniqueId(), (uuid) -> {
			return new ShopCreationItemSelectionTask(plugin, player);
		});
		task.start();
	}

	/**
	 * Cleans up and cancels any currently active task for the given player.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 */
	static void cleanupAndCancel(Player player) {
		assert player != null;
		ShopCreationItemSelectionTask task = activeTasks.remove(player.getUniqueId());
		if (task != null) {
			task.cancel();
		}
	}

	/**
	 * This needs to be called on plugin disable.
	 * <p>
	 * This cleans up any currently active tasks.
	 */
	static void onDisable() {
		// Note: It is not required to manually cancel the active tasks on plugin disable. They are cancelled
		// anyways.
		activeTasks.clear();
	}

	private static void cleanup(Player player) {
		activeTasks.remove(player.getUniqueId());
	}

	// -----

	private final Plugin plugin;
	private final Player player;
	private BukkitTask bukkitTask = null;

	// Use the static 'start' factory method.
	private ShopCreationItemSelectionTask(Plugin plugin, Player player) {
		assert plugin != null && player != null;
		this.plugin = plugin;
		this.player = player;
	}

	private void start() {
		// Cancel previous task if already active:
		this.cancel();
		bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, this, DELAY_TICKS);
	}

	// Note: Performs no cleanup.
	private void cancel() {
		if (bukkitTask != null) {
			bukkitTask.cancel();
			bukkitTask = null;
		}
	}

	@Override
	public void run() {
		// Cleanup:
		cleanup(player);

		if (!player.isOnline()) return; // No longer online
		if (!Settings.isShopCreationItem(player.getEquipment().getItemInMainHand())) {
			// No longer holding the shop creation item in hand:
			return;
		}

		// Note: We do not check if the player has the permission to create shops here again. We checked that
		// earlier already, before starting this task. Even if there has been a change to that in the meantime,
		// there is no major harm caused by sending the selection message anyways. The task's delay is short enough that
		// this does not matter.

		// Inform the player about the shop creation item's usage:
		TextUtils.sendMessage(player, Messages.creationItemSelected);
	}
}
