package com.nisovin.shopkeepers.util;

import java.util.function.Supplier;

/**
 * A lazily calculated value.
 *
 * @param <T>
 *            the type of the lazily calculated value
 */
public final class Lazy<T> {

	private final Supplier<T> supplier;
	private boolean calculated = false;
	private T value = null;

	/**
	 * Creates a new {@link Lazy}.
	 * <p>
	 * The value is lazily calculated by the provided {@link Supplier} the first time it is requested.
	 * 
	 * @param supplier
	 *            the supplier that calculates the value, not <code>null</code>
	 */
	public Lazy(Supplier<T> supplier) {
		Validate.notNull(supplier, "supplier is null");
		this.supplier = supplier;
	}

	/**
	 * Gets the value if present, and otherwise calculates it first.
	 * 
	 * @return the value, can be <code>null</code> if the calculated value is <code>null</code>
	 */
	public T get() {
		if (calculated) {
			return value;
		} else {
			value = supplier.get(); // Can be null
			calculated = true;
			return value;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Lazy [calculated=");
		builder.append(calculated);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	// Note on hashCode and equals: Lazy instances are compared based on their identity and not their current value,
	// because it makes no sense for Lazy instances to be considered equal if they have not yet been calculated.
}
