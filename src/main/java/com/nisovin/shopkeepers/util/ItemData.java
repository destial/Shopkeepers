package com.nisovin.shopkeepers.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;

/**
 * An immutable object that stores item type and meta data information.
 */
public class ItemData implements Cloneable {

	public static class ItemDataDeserializeException extends Exception {

		private static final long serialVersionUID = -6637983932875623362L;

		public ItemDataDeserializeException(String message) {
			super(message);
		}

		public ItemDataDeserializeException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class InvalidItemTypeException extends ItemDataDeserializeException {

		private static final long serialVersionUID = -6123823171023440870L;

		public InvalidItemTypeException(String message) {
			super(message);
		}

		public InvalidItemTypeException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static final String META_TYPE_KEY = "meta-type";
	private static final String DISPLAY_NAME_KEY = "display-name";
	private static final String LORE_KEY = "lore";

	// Special case: Omitting 'blockMaterial' for empty TILE_ENTITY item meta.
	private static final String TILE_ENTITY_BLOCK_MATERIAL_KEY = "blockMaterial";

	// Only returns null if the input data is null.
	public static ItemData deserialize(@ReadOnly Object dataObject) throws ItemDataDeserializeException {
		if (dataObject == null) return null;

		String typeName = null;
		Map<String, Object> dataMap = null;
		if (dataObject instanceof String) {
			// Load from compact representation (no additional item data):
			typeName = (String) dataObject;
			assert typeName != null;
		} else {
			if (dataObject instanceof ConfigurationSection) {
				dataMap = ((ConfigurationSection) dataObject).getValues(false); // Returns a (shallow) copy
			} else if (dataObject instanceof Map) {
				// Make a (shallow) copy of the map, since we will later insert missing data and don't want to modify
				// the original data from the config:
				dataMap = new LinkedHashMap<>();
				for (Entry<?, ?> entry : ((Map<?, ?>) dataObject).entrySet()) {
					dataMap.put(entry.getKey().toString(), entry.getValue());
				}
			} else {
				throw new ItemDataDeserializeException("Unknown item data representation: " + dataObject);
			}
			assert dataMap != null; // Assert: dataMap is a (shallow) copy

			Object typeData = dataMap.get("type");
			if (typeData != null) {
				typeName = typeData.toString();
			}
			if (typeName == null) {
				// Missing item type information:
				throw new ItemDataDeserializeException("Missing item type");
			}
			assert typeName != null;

			// Skip the meta data loading if no further data (besides the item type) is given:
			if (dataMap.size() <= 1) {
				dataMap = null;
			}
		}
		assert typeName != null;

		// Assuming up-to-date material name (performs no conversions besides basic formatting):
		Material type = ItemUtils.parseMaterial(typeName); // Can be null
		if (type == null) {
			throw new InvalidItemTypeException("Unknown item type: " + typeName);
		} else if (type.isLegacy()) {
			throw new InvalidItemTypeException("Unsupported legacy item type: " + typeName);
		} else if (!type.isItem()) {
			// Note: AIR is a valid item type. It is for example used for empty slots in inventories.
			throw new InvalidItemTypeException("Invalid item type: " + typeName);
		}

		// Create item stack (still misses meta data):
		ItemStack dataItem = new ItemStack(type);

		// Load additional meta data:
		if (dataMap != null) {
			// Prepare for meta data deserialization (assumes dataMap is modifiable):
			// Note: Additional information does not need to be removed, but simply gets ignored (eg. item type).

			// Recursively replace all config sections with Maps, because the ItemMeta deserialization expects Maps:
			ConfigUtils.convertSectionsToMaps(dataMap);

			// Determine meta type by creating the serialization of a dummy item meta:
			ItemMeta dummyItemMeta = dataItem.getItemMeta(); // Can be null
			if (dummyItemMeta == null) {
				throw new ItemDataDeserializeException("Items of type " + type.name() + " do not support meta data!");
			}
			dummyItemMeta.setDisplayName("dummy name"); // Ensure item meta is not empty
			Object metaType = dummyItemMeta.serialize().get(META_TYPE_KEY);
			if (metaType == null) {
				throw new IllegalStateException("Could not determine meta type with key '" + META_TYPE_KEY + "'!");
			}
			// Insert meta type:
			dataMap.put(META_TYPE_KEY, metaType);

			// Convert color codes for display name and lore:
			Object displayNameData = dataMap.get(DISPLAY_NAME_KEY);
			if (displayNameData instanceof String) { // Also checks for null
				dataMap.put(DISPLAY_NAME_KEY, TextUtils.colorize((String) displayNameData));
			}
			Object loreData = dataMap.get(LORE_KEY);
			if (loreData instanceof List) { // Also checks for null
				dataMap.put(LORE_KEY, TextUtils.colorizeUnknown((List<?>) loreData));
			}

			// Deserialize ItemMeta:
			ItemMeta itemMeta = ItemUtils.deserializeItemMeta(dataMap); // Can be null

			// Apply ItemMeta:
			dataItem.setItemMeta(itemMeta);
		}

		// Create ItemData:
		// Unmodifiable wrapper: Avoids creating another item copy during construction.
		ItemData itemData = new ItemData(UnmodifiableItemStack.of(dataItem));
		return itemData;
	}

	/////

	private final UnmodifiableItemStack dataItem; // Has amount of 1
	// Cache serialized item meta data, to avoid doing it again for every comparison:
	private @ReadOnly Map<String, @ReadOnly Object> serializedData = null; // Gets lazily initialized (only when needed)

	public ItemData(Material type) {
		// Unmodifiable wrapper: Avoids creating another item copy during construction.
		this(UnmodifiableItemStack.of(new ItemStack(type)));
	}

	// The display name and lore are expected to use Minecraft's color codes.
	public ItemData(Material type, String displayName, @ReadOnly List<String> lore) {
		// Unmodifiable wrapper: Avoids creating another item copy during construction.
		this(UnmodifiableItemStack.of(ItemUtils.createItemStack(type, 1, displayName, lore)));
	}

	public ItemData(ItemData otherItemData, String displayName, @ReadOnly List<String> lore) {
		// Unmodifiable wrapper: Avoids creating another item copy during construction.
		this(UnmodifiableItemStack.of(ItemUtils.createItemStack(otherItemData, 1, displayName, lore)));
	}

	/**
	 * Creates a new {@link ItemData} with the data of the given item stack.
	 * <p>
	 * If the given item stack is an {@link UnmodifiableItemStack}, it is assumed to be immutable and the
	 * {@link ItemData} is allowed store it without making a copy of it first.
	 * 
	 * @param dataItem
	 *            the data item, not <code>null</code>
	 */
	public ItemData(@ReadOnly ItemStack dataItem) {
		this(ItemUtils.unmodifiableCopyWithAmount(dataItem, 1));
	}

	// dataItem is assumed to be immutable.
	public ItemData(UnmodifiableItemStack dataItem) {
		Validate.notNull(dataItem, "dataItem is null");
		this.dataItem = ItemUtils.unmodifiableCopyWithAmount(dataItem.asItemStack(), 1);
	}

	public Material getType() {
		return dataItem.getType();
	}

	// Creates a copy of this ItemData, but changes the item type. If the new type matches the previous type, the
	// current ItemData is returned.
	// Any incompatible meta data is removed.
	public ItemData withType(Material type) {
		Validate.notNull(type, "type is null");
		Validate.isTrue(type.isItem(), () -> "type is not an item: " + type);
		if (this.getType() == type) return this;
		ItemStack newDataItem = this.createItemStack();
		newDataItem.setType(type);
		// Unmodifiable wrapper: Avoids creating another item copy during construction.
		return new ItemData(UnmodifiableItemStack.of(newDataItem));
	}

	// Not null.
	private Map<String, Object> getSerializedData() {
		// Lazily cache the serialized data:
		if (serializedData == null) {
			ItemMeta itemMeta = dataItem.getItemMeta();
			serializedData = ItemUtils.serializeItemMeta(itemMeta);
			if (serializedData == null) {
				// Ensure that the field is not null after initialization:
				serializedData = Collections.emptyMap();
			}
		}
		assert serializedData != null;
		return serializedData;
	}

	public boolean hasItemMeta() {
		return !this.getSerializedData().isEmpty(); // Equivalent to dataItem.hasItemMeta()
	}

	public ItemMeta getItemMeta() {
		// Returns a copy, therefore cannot modify the original data:
		return dataItem.getItemMeta();
	}

	// Creates an item stack with an amount of 1.
	public ItemStack createItemStack() {
		return this.createItemStack(1);
	}

	public ItemStack createItemStack(int amount) {
		return ItemUtils.copyWithAmount(dataItem, amount);
	}

	public boolean isSimilar(@ReadOnly ItemStack other) {
		return dataItem.isSimilar(other);
	}

	public boolean isSimilar(UnmodifiableItemStack other) {
		return other != null && other.isSimilar(dataItem);
	}

	public boolean matches(@ReadOnly ItemStack item) {
		return this.matches(item, false); // Not matching partial lists
	}

	public boolean matches(UnmodifiableItemStack item) {
		return this.matches(ItemUtils.asItemStackOrNull(item));
	}

	public boolean matches(@ReadOnly ItemStack item, boolean matchPartialLists) {
		// Same type and matching data:
		return ItemUtils.matchesData(item, this.getType(), this.getSerializedData(), matchPartialLists);
	}

	public boolean matches(UnmodifiableItemStack item, boolean matchPartialLists) {
		return this.matches(ItemUtils.asItemStackOrNull(item), matchPartialLists);
	}

	public boolean matches(ItemData itemData) {
		return this.matches(itemData, false); // Not matching partial lists
	}

	// Given ItemData is of same type and has data matching this ItemData.
	public boolean matches(ItemData itemData, boolean matchPartialLists) {
		if (itemData == null) return false;
		if (itemData.getType() != this.getType()) return false;
		return ItemUtils.matchesData(itemData.getSerializedData(), this.getSerializedData(), matchPartialLists);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ItemData [data=");
		builder.append(dataItem);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataItem.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ItemData)) return false;
		ItemData other = (ItemData) obj;
		if (!dataItem.equals(other.dataItem)) return false;
		return true;
	}

	@Override
	public ItemData clone() {
		return new ItemData(dataItem); // Clones the item internally
	}

	public Object serialize() {
		Map<String, Object> serializedData = this.getSerializedData();
		if (serializedData.isEmpty()) {
			// Use a more compact representation if there is no additional item data:
			return dataItem.getType().name();
		}

		Map<String, Object> dataMap = new LinkedHashMap<>();
		dataMap.put("type", dataItem.getType().name());

		for (Entry<String, Object> entry : serializedData.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			// Omitting any data which can be easily restored during deserialization:
			// Omit meta type key:
			if (META_TYPE_KEY.equals(key)) continue;

			// Omit 'blockMaterial' for empty TILE_ENTITY item meta:
			if (TILE_ENTITY_BLOCK_MATERIAL_KEY.equals(key)) {
				// Check if specific meta type only contains unspecific meta data:
				ItemMeta specificItemMeta = dataItem.getItemMeta();
				// TODO Relies on some material with unspecific item meta.
				ItemMeta unspecificItemMeta = Bukkit.getItemFactory().asMetaFor(specificItemMeta, Material.STONE);
				if (Bukkit.getItemFactory().equals(unspecificItemMeta, specificItemMeta)) {
					continue; // Skip 'blockMaterial' entry
				}
			}

			// Use alternative color codes for display name and lore:
			if (DISPLAY_NAME_KEY.equals(key)) {
				if (value instanceof String) {
					value = TextUtils.decolorize((String) value);
				}
			} else if (LORE_KEY.equals(key)) {
				if (value instanceof List) {
					value = TextUtils.decolorizeUnknown((List<?>) value);
				}
			}

			// Move into data map: Avoiding a deep copy, since it is assumed to not be needed.
			dataMap.put(key, value);
		}
		return dataMap;
	}
}
