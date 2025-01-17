package com.nisovin.shopkeepers.text;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import com.nisovin.shopkeepers.util.StringUtils;
import com.nisovin.shopkeepers.util.Validate;
import com.nisovin.shopkeepers.util.text.MessageArguments;

/**
 * Base class for all {@link Text} implementations.
 */
public abstract class AbstractText implements Text {

	// Reused among all Text instances:
	private static final Map<String, Object> TEMP_ARGUMENTS_MAP = new HashMap<>();

	// TODO Remove parent reference?
	// Would allow less mutable state, which simplifies reuse of Text instances.
	private Text parent = null;

	private Text child = null;
	private Text next = null;

	// TODO Cache plain text? Requires childs to inform parents on changes to their translation or placeholder
	// arguments. -> Might not even be worth it in the presence of dynamic arguments.

	protected AbstractText() {
	}

	// PARENT

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Text> T getParent() {
		// Note: Allows the caller to conveniently cast the result to the expected Text type (eg. to TextBuilder in a
		// fluently built Text).
		return (T) parent;
	}

	/**
	 * Sets the parent Text.
	 * <p>
	 * Internal method that is meant to only be used by Text implementations!
	 * 
	 * @param parent
	 *            the parent Text, can be <code>null</code>
	 */
	private void setParent(Text parent) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Text> T getRoot() {
		Text text = this;
		while (text.getParent() != null) {
			text = text.getParent();
		}
		// Note: Allows the caller to conveniently cast the result to the expected Text type (eg. to TextBuilder in a
		// fluently built Text).
		return (T) text;
	}

	// CHILD

	@Override
	public Text getChild() {
		return child;
	}

	/**
	 * Sets the child Text.
	 * <p>
	 * Internal method that is meant to only be used by Text implementations!
	 * 
	 * @param child
	 *            the child Text, or <code>null</code> to unset it
	 */
	protected void setChild(Text child) {
		if (child != null) {
			Validate.isTrue(child != this, "Cannot set self as child!");
			Validate.isTrue(child.getParent() == null, "The given child Text already has a parent!");
			((AbstractText) child).setParent(this);
		}
		if (this.child != null) {
			((AbstractText) this.child).setParent(null);
		}
		this.child = child; // Can be null
	}

	// NEXT

	@Override
	public Text getNext() {
		return next;
	}

	/**
	 * Sets the next Text.
	 * <p>
	 * Internal method that is meant to only be used by Text implementations!
	 * 
	 * @param next
	 *            the next Text, or <code>null</code> to unset it
	 */
	protected void setNext(Text next) {
		if (next != null) {
			Validate.isTrue(next != this, "Cannot set self as next!");
			Validate.isTrue(next.getParent() == null, "The given next Text already has a parent!");
			((AbstractText) next).setParent(this);
		}
		if (this.next != null) {
			((AbstractText) this.next).setParent(null);
		}
		this.next = next; // Can be null
	}

	// PLACEHOLDER ARGUMENTS

	@Override
	public Text setPlaceholderArguments(MessageArguments arguments) {
		Validate.notNull(arguments, "arguments is null");

		// Delegate to childs:
		Text child = this.getChild();
		if (child != null) {
			child.setPlaceholderArguments(arguments);
		}

		// Delegate to next:
		Text next = this.getNext();
		if (next != null) {
			next.setPlaceholderArguments(arguments);
		}
		return this;
	}

	@Override
	public final Text setPlaceholderArguments(Map<String, ?> arguments) {
		return this.setPlaceholderArguments(MessageArguments.ofMap(arguments));
	}

	@Override
	public final Text setPlaceholderArguments(Object... argumentPairs) {
		assert TEMP_ARGUMENTS_MAP.isEmpty();
		try {
			StringUtils.addArgumentsToMap(TEMP_ARGUMENTS_MAP, argumentPairs);
			return this.setPlaceholderArguments(TEMP_ARGUMENTS_MAP);
		} finally {
			TEMP_ARGUMENTS_MAP.clear(); // Reset
		}
	}

	@Override
	public Text clearPlaceholderArguments() {
		// Delegate to childs:
		Text child = this.getChild();
		if (child != null) {
			child.clearPlaceholderArguments();
		}

		// Delegate to next:
		Text next = this.getNext();
		if (next != null) {
			next.clearPlaceholderArguments();
		}
		return this;
	}

	// PLAIN TEXT

	@Override
	public String toPlainText() {
		StringBuilder builder = new StringBuilder();
		this.appendPlainText(builder, false);
		return builder.toString();
	}

	@Override
	public String toPlainFormatText() {
		StringBuilder builder = new StringBuilder();
		this.appendPlainText(builder, true);
		return builder.toString();
	}

	protected void appendPlainText(StringBuilder builder, boolean formatText) {
		// Child:
		Text child = this.getChild();
		if (child != null) {
			((AbstractText) child).appendPlainText(builder, formatText);
		}

		// Next:
		Text next = this.getNext();
		if (next != null) {
			((AbstractText) next).appendPlainText(builder, formatText);
		}
	}

	@Override
	public boolean isPlainText() {
		// Child:
		Text child = this.getChild();
		if (child != null && !child.isPlainText()) {
			return false;
		}

		// Next:
		Text next = this.getNext();
		if (next != null && !next.isPlainText()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isPlainTextEmpty() {
		// Child:
		Text child = this.getChild();
		if (child != null && !child.isPlainTextEmpty()) {
			return false;
		}

		// Next:
		Text next = this.getNext();
		if (next != null && !next.isPlainTextEmpty()) {
			return false;
		}
		return true;
	}

	// UNFORMATTED TEXT

	@Override
	public String toUnformattedText() {
		return ChatColor.stripColor(this.toPlainText());
	}
}
