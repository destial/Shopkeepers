package com.nisovin.shopkeepers.config.lib.value.types;

import com.nisovin.shopkeepers.util.MinecraftEnumUtils;

/**
 * Extends {@link EnumValue}, but normalizes inputs via {@link MinecraftEnumUtils#normalizeEnumName(String)} when
 * parsing the enum value from a given input String.
 *
 * @param <E>
 *            the enum type
 */
public class MinecraftEnumValue<E extends Enum<E>> extends EnumValue<E> {

	public MinecraftEnumValue(Class<E> enumClass) {
		super(enumClass);
	}

	@Override
	protected String normalize(String input) {
		return MinecraftEnumUtils.normalizeEnumName(input);
	}
}
