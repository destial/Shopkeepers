package com.nisovin.shopkeepers.spigot.text;

import com.nisovin.shopkeepers.util.Validate;

public class ClickEvent {

	public enum Action {
		/**
		 * Prompts the player to open the specified url in his web browser.
		 */
		OPEN_URL,
		/**
		 * Prompts the player to open the file at the specified path.
		 */
		OPEN_FILE,
		/**
		 * Causes the player to enter and send the specified text in chat. This can be used to execute commands
		 * (starting with {@link /}). The player requires the corresponding command permission.
		 */
		RUN_COMMAND,
		/**
		 * Inserts the given string (not required to be a command) into the player's text box.
		 * <p>
		 * Does not work for signs or books.
		 * <p>
		 * Unlike insertion, this replaces the existing contents of the chat input.
		 */
		SUGGEST_COMMAND,
		/**
		 * Interprets the text as page number and switches to the corresponding book page.
		 * <p>
		 * Only works inside books.
		 */
		CHANGE_PAGE
	}

	private final Action action; // not null
	private final String value; // not null, can be empty

	public ClickEvent(Action action, String value) {
		Validate.notNull(action, "Action is null!");
		Validate.notNull(value, "Value is null!");
		this.action = action;
		this.value = value;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return the value (hover text)
	 */
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClickEvent [action=");
		builder.append(action);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
