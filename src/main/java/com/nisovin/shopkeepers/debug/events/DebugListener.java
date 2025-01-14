package com.nisovin.shopkeepers.debug.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.util.Log;
import com.nisovin.shopkeepers.util.Utils;

/**
 * Registers event handlers for all currently known (i.e. loaded) types of events and then prints debug information
 * whenever these events are triggered.
 */
public class DebugListener implements Listener {

	public static DebugListener register(boolean logAllEvents, boolean printListeners) {
		// TODO Might only log events whose classes got loaded yet (eg. with registered listeners).
		Log.info("Registering DebugListener.");
		DebugListener debugListener = new DebugListener(logAllEvents, printListeners);
		List<HandlerList> allHandlerLists = HandlerList.getHandlerLists();
		for (HandlerList handlerList : allHandlerLists) {
			handlerList.register(new RegisteredListener(debugListener, new EventExecutor() {
				@Override
				public void execute(Listener listener, Event event) throws EventException {
					debugListener.handleEvent(event);
				}
			}, EventPriority.LOWEST, SKShopkeepersPlugin.getInstance(), false));
		}
		return debugListener;
	}

	private static class EventData {
		boolean printedListeners = false;
	}

	private final Map<String, EventData> eventData = new HashMap<>();
	private String lastLoggedEvent = null;
	private int lastLoggedEventCounter = 0;

	private final boolean logAllEvents;
	private final boolean printListeners;

	private DebugListener(boolean logAllEvents, boolean printListeners) {
		this.logAllEvents = logAllEvents;
		this.printListeners = printListeners;
	}

	public void unregister() {
		HandlerList.unregisterAll(this);
	}

	private void handleEvent(Event event) {
		String eventName = event.getEventName();
		EventData data = eventData.computeIfAbsent(eventName, key -> new EventData());

		// Event logging:
		if (logAllEvents) {
			// Combine subsequent calls of the same event into single output that gets printed with the next event:
			if (eventName.equals(lastLoggedEvent)) {
				lastLoggedEventCounter++;
			} else {
				if (lastLoggedEventCounter > 0) {
					assert lastLoggedEvent != null;
					Log.info("[DebugListener] Event: " + lastLoggedEvent + " (" + lastLoggedEventCounter + "x" + ")");
				}

				lastLoggedEvent = eventName;
				lastLoggedEventCounter = 1;
			}
		}

		// Print listeners, once:
		if (printListeners && !data.printedListeners) {
			data.printedListeners = true;
			Utils.printRegisteredListeners(event);
		}
	}
}
