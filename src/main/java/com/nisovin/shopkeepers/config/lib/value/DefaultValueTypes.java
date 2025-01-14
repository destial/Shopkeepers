package com.nisovin.shopkeepers.config.lib.value;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import com.nisovin.shopkeepers.config.lib.value.types.BooleanValue;
import com.nisovin.shopkeepers.config.lib.value.types.DoubleValue;
import com.nisovin.shopkeepers.config.lib.value.types.EntityTypeValue;
import com.nisovin.shopkeepers.config.lib.value.types.EnumValue;
import com.nisovin.shopkeepers.config.lib.value.types.IntegerValue;
import com.nisovin.shopkeepers.config.lib.value.types.ItemDataValue;
import com.nisovin.shopkeepers.config.lib.value.types.ListValue;
import com.nisovin.shopkeepers.config.lib.value.types.LongValue;
import com.nisovin.shopkeepers.config.lib.value.types.MaterialValue;
import com.nisovin.shopkeepers.config.lib.value.types.SoundEffectValue;
import com.nisovin.shopkeepers.config.lib.value.types.StringValue;
import com.nisovin.shopkeepers.config.lib.value.types.TextValue;
import com.nisovin.shopkeepers.text.Text;
import com.nisovin.shopkeepers.util.ItemData;
import com.nisovin.shopkeepers.util.SoundEffect;

/**
 * Registry of default value types of settings.
 */
public class DefaultValueTypes {

	private static final ValueTypeRegistry registry = new ValueTypeRegistry();

	static {
		registry.register(String.class, StringValue.INSTANCE);
		registry.register(Boolean.class, BooleanValue.INSTANCE);
		registry.register(boolean.class, BooleanValue.INSTANCE);
		registry.register(Integer.class, IntegerValue.INSTANCE);
		registry.register(int.class, IntegerValue.INSTANCE);
		registry.register(Double.class, DoubleValue.INSTANCE);
		registry.register(double.class, DoubleValue.INSTANCE);
		registry.register(Long.class, LongValue.INSTANCE);
		registry.register(long.class, LongValue.INSTANCE);

		registry.register(Text.class, TextValue.INSTANCE);
		registry.register(Material.class, MaterialValue.INSTANCE);
		registry.register(ItemData.class, ItemDataValue.INSTANCE);
		registry.register(SoundEffect.class, SoundEffectValue.INSTANCE);
		registry.register(EntityType.class, EntityTypeValue.INSTANCE);

		// The following more general value type providers are only used for types which didn't match any of the above:

		registry.register(ValueTypeProviders.forTypePattern(TypePatterns.forBaseType(Enum.class), new Function<Type, ValueType<?>>() {
			@SuppressWarnings("unchecked")
			@Override
			public ValueType<?> apply(Type type) {
				assert type instanceof Enum<?>;
				Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) type;
				return this.newEnumValueType(enumClass);
			}

			@SuppressWarnings("unchecked")
			private <E extends Enum<E>> EnumValue<E> newEnumValueType(Class<? extends Enum<?>> enumClass) {
				return new EnumValue<>((Class<E>) enumClass);
			}
		}));
		registry.register(ValueTypeProviders.forTypePattern(TypePatterns.forClass(List.class), (type) -> {
			assert type instanceof ParameterizedType;
			Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
			ValueType<?> elementValueType = DefaultValueTypes.get(elementType);
			if (elementValueType == null) {
				throw new IllegalArgumentException("Unsupported element type: " + elementType.getTypeName());
			}
			return new ListValue<>(elementValueType);
		}));
	}

	public static <T> ValueType<T> get(Type type) {
		return registry.getValueType(type);
	}

	private DefaultValueTypes() {
	}
}
