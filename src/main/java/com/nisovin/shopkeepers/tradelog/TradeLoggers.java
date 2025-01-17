package com.nisovin.shopkeepers.tradelog;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.config.Settings;
import com.nisovin.shopkeepers.tradelog.csv.CsvTradeLogger;
import com.nisovin.shopkeepers.tradelog.data.TradeRecord;
import com.nisovin.shopkeepers.util.Validate;
import com.nisovin.shopkeepers.util.trading.MergedTrades;
import com.nisovin.shopkeepers.util.trading.TradeMerger;
import com.nisovin.shopkeepers.util.trading.TradeMerger.MergeMode;

public class TradeLoggers implements Listener {

	private final Plugin plugin;
	private final List<TradeLogger> loggers = new ArrayList<>();
	// In order to represent the logged trades more compactly, we merge equivalent trades that are triggered in quick
	// succession over a certain period of time. The maximum merge duration is configurable, and the trade merging can
	// also be disabled.
	// The processing of these merged trades may happen accordingly deferred: The logged timestamps and shopkeeper
	// states may therefore slightly differ to what they were when the trades took actually place. However, we consider
	// the typically chosen merge durations to be small enough for this to not be an issue. Also, the order in which the
	// trades took place is still preserved.
	private TradeMerger tradeMerger;

	public TradeLoggers(Plugin plugin) {
		Validate.notNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	public void onEnable() {
		int mergeDuration = Settings.tradeLogMergeDurationTicks;
		if (mergeDuration == 1) {
			// Only merge trades that are triggered by the same click event:
			tradeMerger = new TradeMerger(plugin, MergeMode.SAME_CLICK_EVENT, this::processTrades);
		} else {
			// Note: A merge duration of 0 disables the trade merging.
			tradeMerger = new TradeMerger(plugin, MergeMode.DURATION, this::processTrades)
					.withMergeDurations(mergeDuration, Settings.tradeLogNextMergeTimeoutTicks);
		}
		tradeMerger.onEnable();

		if (Settings.logTradesToCsv) {
			loggers.add(new CsvTradeLogger(plugin));
		}

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public void onDisable() {
		// Stop reacting to new trades:
		HandlerList.unregisterAll(this);

		// Process any pending previous trades:
		tradeMerger.onDisable();

		// Wait for any pending writes to complete:
		loggers.forEach(TradeLogger::flush);
		loggers.clear();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onTradeCompleted(ShopkeeperTradeEvent event) {
		if (loggers.isEmpty()) return; // Nothing to log

		tradeMerger.mergeTrade(event);
	}

	private void processTrades(MergedTrades trades) {
		TradeRecord trade = TradeRecord.create(trades);
		loggers.forEach(logger -> logger.logTrade(trade));
	}
}
