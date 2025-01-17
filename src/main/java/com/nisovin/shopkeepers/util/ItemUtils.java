package com.nisovin.shopkeepers.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack;
import com.nisovin.shopkeepers.util.annotations.ReadOnly;
import com.nisovin.shopkeepers.util.annotations.ReadWrite;

/**
 * Utility functions related to materials, items and inventories.
 */
public final class ItemUtils {

	private ItemUtils() {
	}

	public static int MAX_STACK_SIZE = 64;

	// Material utilities:

	/**
	 * Parses the {@link Material} from the given input String.
	 * <p>
	 * This is similar to {@link Material#matchMaterial(String)}, but may have small performance benefits, or be more
	 * lenient in the inputs it accepts.
	 * 
	 * @param input
	 *            the input String
	 * @return the parsed Material, or <code>null</code>
	 */
	public static Material parseMaterial(String input) {
		if (input == null) return null;
		// Format the input:
		String materialName = MinecraftEnumUtils.normalizeEnumName(input);

		// Lookup the material:
		Material material = Material.getMaterial(materialName);
		return material;
	}

	/**
	 * Checks if the given material is a container.
	 * 
	 * @param material
	 *            the material
	 * @return <code>true</code> if the material is a container
	 */
	public static boolean isContainer(Material material) {
		// TODO This list of container materials needs to be updated with each MC update.
		if (material == null) return false;
		if (isChest(material)) return true; // Includes trapped chest
		if (isShulkerBox(material)) return true;
		switch (material) {
		case BARREL:
		case BREWING_STAND:
		case DISPENSER:
		case DROPPER:
		case HOPPER:
		case FURNACE:
		case BLAST_FURNACE:
		case SMOKER:
		case ENDER_CHEST: // Note: Has no BlockState of type Container.
			return true;
		default:
			return false;
		}
	}

	public static boolean isChest(Material material) {
		return material == Material.CHEST || material == Material.TRAPPED_CHEST;
	}

	public static boolean isShulkerBox(Material material) {
		if (material == null) return false;
		switch (material) {
		case SHULKER_BOX:
		case WHITE_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case LIGHT_GRAY_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case BLACK_SHULKER_BOX:
			return true;
		default:
			return false;
		}
	}

	public static boolean isSign(Material material) {
		if (material == null) return false;
		return material.data == org.bukkit.block.data.type.Sign.class || material.data == org.bukkit.block.data.type.WallSign.class;
	}

	public static boolean isRail(Material material) {
		if (material == null) return false;
		switch (material) {
		case RAIL:
		case POWERED_RAIL:
		case DETECTOR_RAIL:
		case ACTIVATOR_RAIL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Formats the name of the given {@link Material} to a more user-friendly representation. See also
	 * {@link EnumUtils#formatEnumName(String)}.
	 * 
	 * @param material
	 *            the material
	 * @return the formatted material name
	 */
	public static String formatMaterialName(Material material) {
		return EnumUtils.formatEnumName(material != null ? material.name() : "");
	}

	/**
	 * Formats the {@link Material} name of the given {@link ItemStack} to a more user-friendly representation. See also
	 * {@link #formatMaterialName(Material)}.
	 * 
	 * @param itemStack
	 *            the item stack
	 * @return the formatted material name
	 */
	public static String formatMaterialName(@ReadOnly ItemStack itemStack) {
		return formatMaterialName(itemStack != null ? itemStack.getType() : null);
	}

	// ItemStack utilities:

	/**
	 * Checks if the given {@link ItemStack} is empty.
	 * <p>
	 * The item stack is considered 'empty' if it is <code>null</code>, it is of type {@link Material#AIR}, or its
	 * amount is less than or equal to zero.
	 * 
	 * @param itemStack
	 *            the item stack
	 * @return <code>true</code> if the item stack is empty
	 */
	public static boolean isEmpty(@ReadOnly ItemStack itemStack) {
		return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0;
	}

	public static boolean isEmpty(UnmodifiableItemStack itemStack) {
		return isEmpty(asItemStackOrNull(itemStack));
	}

	public static ItemStack getNullIfEmpty(@ReadOnly ItemStack itemStack) {
		return isEmpty(itemStack) ? null : itemStack;
	}

	public static UnmodifiableItemStack getNullIfEmpty(UnmodifiableItemStack itemStack) {
		return isEmpty(itemStack) ? null : itemStack;
	}

	public static ItemStack getOrEmpty(@ReadOnly ItemStack itemStack) {
		if (!isEmpty(itemStack)) return itemStack;
		if (itemStack != null && itemStack.getType() == Material.AIR) return itemStack;
		return new ItemStack(Material.AIR);
	}

	public static ItemStack cloneOrNullIfEmpty(@ReadOnly ItemStack itemStack) {
		return isEmpty(itemStack) ? null : itemStack.clone();
	}

	public static ItemStack copyOrNullIfEmpty(UnmodifiableItemStack itemStack) {
		return isEmpty(itemStack) ? null : itemStack.copy();
	}

	public static ItemStack copyOrNull(UnmodifiableItemStack itemStack) {
		return (itemStack != null) ? itemStack.copy() : null;
	}

	public static UnmodifiableItemStack unmodifiableCloneIfModifiable(@ReadOnly ItemStack itemStack) {
		if (itemStack == null) return null;
		if (itemStack instanceof UnmodifiableItemStack) return (UnmodifiableItemStack) itemStack;
		return UnmodifiableItemStack.of(itemStack.clone());
	}

	public static UnmodifiableItemStack unmodifiableOrNullIfEmpty(@ReadOnly ItemStack itemStack) {
		return UnmodifiableItemStack.of(getNullIfEmpty(itemStack));
	}

	@Deprecated
	public static ItemStack asItemStackOrNull(UnmodifiableItemStack itemStack) {
		return (itemStack != null) ? itemStack.asItemStack() : null;
	}

	/**
	 * Creates a {@link ItemStack#clone() copy} of the given {@link ItemStack} with a stack size of {@code 1}.
	 * 
	 * @param itemStack
	 *            the item stack to copy
	 * @return the copy, or <code>null</code> if the given item stack is <code>null</code>
	 */
	public static ItemStack copySingleItem(@ReadOnly ItemStack itemStack) {
		return copyWithAmount(itemStack, 1);
	}

	/**
	 * Creates a {@link ItemStack#clone() copy} of the given {@link ItemStack} with the specified stack size.
	 * 
	 * @param itemStack
	 *            the item stack to copy
	 * @param amount
	 *            the stack size of the copy
	 * @return the copy, or <code>null</code> if the given item stack is <code>null</code>
	 */
	public static ItemStack copyWithAmount(@ReadOnly ItemStack itemStack, int amount) {
		if (itemStack == null) return null;
		ItemStack copy = itemStack.clone();
		copy.setAmount(amount);
		return copy;
	}

	public static ItemStack copyWithAmount(UnmodifiableItemStack itemStack, int amount) {
		return copyWithAmount(asItemStackOrNull(itemStack), amount);
	}

	// Returns the same item stack if its amount already matches the target amount and the item stack is already
	// unmodifiable.
	public static UnmodifiableItemStack unmodifiableCopyWithAmount(@ReadOnly ItemStack itemStack, int amount) {
		if (itemStack == null) return null;
		if (itemStack.getAmount() != amount) {
			return UnmodifiableItemStack.of(ItemUtils.copyWithAmount(itemStack, amount));
		} else {
			return ItemUtils.unmodifiableCloneIfModifiable(itemStack);
		}
	}

	public static int trimItemAmount(@ReadOnly ItemStack itemStack, int amount) {
		return trimItemAmount(itemStack.getType(), amount);
	}

	// Trims the amount between 1 and the item's max-stack-size.
	public static int trimItemAmount(Material itemType, int amount) {
		return MathUtils.trim(amount, 1, itemType.getMaxStackSize());
	}

	/**
	 * Increases the amount of the given {@link ItemStack}.
	 * <p>
	 * This makes sure that the item stack's amount ends up to be at most {@link ItemStack#getMaxStackSize()}, and that
	 * empty item stacks are represented by <code>null</code>.
	 * 
	 * @param itemStack
	 *            the item stack, can be empty
	 * @param amountToIncrease
	 *            the amount to increase, can be negative to decrease
	 * @return the resulting item stack, or <code>null</code> if the item stack ends up being empty
	 */
	public static ItemStack increaseItemAmount(@ReadWrite ItemStack itemStack, int amountToIncrease) {
		if (isEmpty(itemStack)) return null;
		int newAmount = Math.min(itemStack.getAmount() + amountToIncrease, itemStack.getMaxStackSize());
		if (newAmount <= 0) return null;
		itemStack.setAmount(newAmount);
		return itemStack;
	}

	/**
	 * Decreases the amount of the given {@link ItemStack}.
	 * <p>
	 * This makes sure that the item stack's amount ends up to be at most {@link ItemStack#getMaxStackSize()}, and that
	 * empty item stacks are represented by <code>null</code>.
	 * 
	 * @param itemStack
	 *            the item stack, can be empty
	 * @param amountToDescrease
	 *            the amount to decrease, can be negative to increase
	 * @return the resulting item, or <code>null</code> if the item ends up being empty
	 */
	public static ItemStack descreaseItemAmount(@ReadWrite ItemStack itemStack, int amountToDescrease) {
		return increaseItemAmount(itemStack, -amountToDescrease);
	}

	/**
	 * Gets the amount of the given {@link ItemStack}, and returns <code>0</code> if the item stack is
	 * {@link #isEmpty(ItemStack) empty}.
	 * 
	 * @param itemStack
	 *            the item stack, can be empty
	 * @return the item stack's amount, or <code>0</code> if the item stack is empty
	 */
	public static int getItemStackAmount(@ReadOnly ItemStack itemStack) {
		return isEmpty(itemStack) ? 0 : itemStack.getAmount();
	}

	/**
	 * Gets the amount of the given {@link UnmodifiableItemStack}, and returns <code>0</code> if the item stack is
	 * {@link #isEmpty(UnmodifiableItemStack) empty}.
	 * 
	 * @param itemStack
	 *            the item stack, can be empty
	 * @return the item stack's amount, or <code>0</code> if the item stack is empty
	 */
	public static int getItemStackAmount(UnmodifiableItemStack itemStack) {
		return getItemStackAmount(asItemStackOrNull(itemStack));
	}

	// The display name and lore are expected to use Minecraft's color codes.
	public static ItemStack createItemStack(Material type, int amount, String displayName, @ReadOnly List<String> lore) {
		assert type != null && type.isItem();
		// TODO Return null in case of type AIR?
		ItemStack itemStack = new ItemStack(type, amount);
		return ItemUtils.setDisplayNameAndLore(itemStack, displayName, lore);
	}

	// The display name and lore are expected to use Minecraft's color codes.
	public static ItemStack createItemStack(ItemData itemData, int amount, String displayName, @ReadOnly List<String> lore) {
		Validate.notNull(itemData, "itemData is null");
		return setDisplayNameAndLore(itemData.createItemStack(amount), displayName, lore);
	}

	// The display name and lore are expected to use Minecraft's color codes.
	// Null arguments keep the previous display name or lore (instead of clearing them). TODO Change this?
	public static ItemStack setDisplayNameAndLore(@ReadWrite ItemStack item, String displayName, @ReadOnly List<String> lore) {
		if (item == null) return null;
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			if (displayName != null) {
				meta.setDisplayName(displayName);
			}
			if (lore != null) {
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
		}
		return item;
	}

	// Null to remove display name.
	// The display name is expected to use Minecraft's color codes.
	public static ItemStack setDisplayName(@ReadWrite ItemStack itemStack, String displayName) {
		if (itemStack == null) return null;
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta == null) return itemStack;
		if (displayName == null && !itemMeta.hasDisplayName()) {
			return itemStack;
		}
		itemMeta.setDisplayName(displayName); // Null will clear the display name
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack setLeatherColor(@ReadWrite ItemStack leatherArmorItem, Color color) {
		if (leatherArmorItem == null) return null;
		ItemMeta meta = leatherArmorItem.getItemMeta();
		if (meta instanceof LeatherArmorMeta) {
			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
			leatherMeta.setColor(color);
			leatherArmorItem.setItemMeta(leatherMeta);
		}
		return leatherArmorItem;
	}

	// TODO This can be removed once Bukkit provides a non-deprecated mapping itself.
	public static Material getWoolType(DyeColor dyeColor) {
		switch (dyeColor) {
		case ORANGE:
			return Material.ORANGE_WOOL;
		case MAGENTA:
			return Material.MAGENTA_WOOL;
		case LIGHT_BLUE:
			return Material.LIGHT_BLUE_WOOL;
		case YELLOW:
			return Material.YELLOW_WOOL;
		case LIME:
			return Material.LIME_WOOL;
		case PINK:
			return Material.PINK_WOOL;
		case GRAY:
			return Material.GRAY_WOOL;
		case LIGHT_GRAY:
			return Material.LIGHT_GRAY_WOOL;
		case CYAN:
			return Material.CYAN_WOOL;
		case PURPLE:
			return Material.PURPLE_WOOL;
		case BLUE:
			return Material.BLUE_WOOL;
		case BROWN:
			return Material.BROWN_WOOL;
		case GREEN:
			return Material.GREEN_WOOL;
		case RED:
			return Material.RED_WOOL;
		case BLACK:
			return Material.BLACK_WOOL;
		case WHITE:
		default:
			return Material.WHITE_WOOL;
		}
	}

	public static Material getCarpetType(DyeColor dyeColor) {
		switch (dyeColor) {
		case ORANGE:
			return Material.ORANGE_CARPET;
		case MAGENTA:
			return Material.MAGENTA_CARPET;
		case LIGHT_BLUE:
			return Material.LIGHT_BLUE_CARPET;
		case YELLOW:
			return Material.YELLOW_CARPET;
		case LIME:
			return Material.LIME_CARPET;
		case PINK:
			return Material.PINK_CARPET;
		case GRAY:
			return Material.GRAY_CARPET;
		case LIGHT_GRAY:
			return Material.LIGHT_GRAY_CARPET;
		case CYAN:
			return Material.CYAN_CARPET;
		case PURPLE:
			return Material.PURPLE_CARPET;
		case BLUE:
			return Material.BLUE_CARPET;
		case BROWN:
			return Material.BROWN_CARPET;
		case GREEN:
			return Material.GREEN_CARPET;
		case RED:
			return Material.RED_CARPET;
		case BLACK:
			return Material.BLACK_CARPET;
		case WHITE:
		default:
			return Material.WHITE_CARPET;
		}
	}

	public static boolean isDamageable(@ReadOnly ItemStack itemStack) {
		if (itemStack == null) return false;
		return isDamageable(itemStack.getType());
	}

	public static boolean isDamageable(Material type) {
		return (type.getMaxDurability() > 0);
	}

	public static int getDurability(@ReadOnly ItemStack itemStack) {
		assert itemStack != null;
		// Checking if the item is damageable is cheap in comparison to retrieving the ItemMeta:
		if (!isDamageable(itemStack)) return 0;

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta instanceof Damageable) { // Also checks for null ItemMeta
			return ((Damageable) itemMeta).getDamage();
		} // Else: Unexpected, since we already checked that the item is damageable above.
		return 0;
	}

	public static String getSimpleItemInfo(UnmodifiableItemStack item) {
		if (item == null) return "empty";
		StringBuilder sb = new StringBuilder();
		sb.append(item.getAmount()).append('x').append(item.getType());
		return sb.toString();
	}

	public static String getSimpleRecipeInfo(TradingRecipe recipe) {
		if (recipe == null) return "none";
		StringBuilder sb = new StringBuilder();
		sb.append("[item1=").append(getSimpleItemInfo(recipe.getItem1()))
				.append(", item2=").append(getSimpleItemInfo(recipe.getItem2()))
				.append(", result=").append(getSimpleItemInfo(recipe.getResultItem())).append("]");
		return sb.toString();
	}

	/**
	 * Same as {@link UnmodifiableItemStack#equals(ItemStack)}, but takes into account that the given item stacks might
	 * both be <code>null</code>.
	 * 
	 * @param unmodifiableItemStack
	 *            an unmodifiable item stack
	 * @param itemStack
	 *            an item stack to compare with
	 * @return <code>true</code> if the item stacks are equal, or both <code>null</code>
	 */
	public static boolean equals(UnmodifiableItemStack unmodifiableItemStack, @ReadOnly ItemStack itemStack) {
		if (unmodifiableItemStack == null) return (itemStack == null);
		return unmodifiableItemStack.equals(itemStack);
	}

	/**
	 * Same as {@link ItemStack#isSimilar(ItemStack)}, but takes into account that the given item stacks might both be
	 * <code>null</code>.
	 * 
	 * @param item1
	 *            an item stack
	 * @param item2
	 *            another item stack
	 * @return <code>true</code> if the item stacks are similar, or both <code>null</code>
	 */
	public static boolean isSimilar(@ReadOnly ItemStack item1, @ReadOnly ItemStack item2) {
		if (item1 == null) return (item2 == null);
		return item1.isSimilar(item2);
	}

	/**
	 * Same as {@link UnmodifiableItemStack#isSimilar(UnmodifiableItemStack)}, but takes into account that the given
	 * item stacks might both be <code>null</code>.
	 * 
	 * @param item1
	 *            an item stack
	 * @param item2
	 *            another item stack
	 * @return <code>true</code> if the item stacks are similar, or both <code>null</code>
	 */
	public static boolean isSimilar(UnmodifiableItemStack item1, UnmodifiableItemStack item2) {
		return isSimilar(asItemStackOrNull(item1), asItemStackOrNull(item2));
	}

	/**
	 * Same as {@link UnmodifiableItemStack#isSimilar(ItemStack)}, but takes into account that the given item stacks
	 * might both be <code>null</code>.
	 * 
	 * @param item1
	 *            an item stack
	 * @param item2
	 *            another item stack
	 * @return <code>true</code> if the item stacks are similar, or both <code>null</code>
	 */
	public static boolean isSimilar(UnmodifiableItemStack item1, @ReadOnly ItemStack item2) {
		return isSimilar(asItemStackOrNull(item1), item2);
	}

	/**
	 * Checks if the given item matches the specified attributes.
	 * 
	 * @param item
	 *            the item
	 * @param type
	 *            the item type
	 * @param displayName
	 *            the displayName, or <code>null</code> or empty to ignore it
	 * @param lore
	 *            the item lore, or <code>null</code> or empty to ignore it
	 * @return <code>true</code> if the item has similar attributes
	 */
	public static boolean isSimilar(@ReadOnly ItemStack item, Material type, String displayName, @ReadOnly List<String> lore) {
		if (item == null) return false;
		if (item.getType() != type) return false;

		boolean checkDisplayName = (displayName != null && !displayName.isEmpty());
		boolean checkLore = (lore != null && !lore.isEmpty());
		if (!checkDisplayName && !checkLore) return true;

		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta == null) return false;

		// Compare display name:
		if (checkDisplayName) {
			if (!itemMeta.hasDisplayName() || !displayName.equals(itemMeta.getDisplayName())) {
				return false;
			}
		}

		// Compare lore:
		if (checkLore) {
			if (!itemMeta.hasLore() || !lore.equals(itemMeta.getLore())) {
				return false;
			}
		}

		return true;
	}

	// ITEM DATA MATCHING

	public static boolean matchesData(@ReadOnly ItemStack item, @ReadOnly ItemStack data) {
		return matchesData(item, data, false); // Not matching partial lists
	}

	public static boolean matchesData(UnmodifiableItemStack item, UnmodifiableItemStack data) {
		return matchesData(item, data, false); // Not matching partial lists
	}

	// Same type and contains data.
	public static boolean matchesData(UnmodifiableItemStack item, UnmodifiableItemStack data, boolean matchPartialLists) {
		return matchesData(asItemStackOrNull(item), asItemStackOrNull(data), matchPartialLists);
	}

	// Same type and contains data.
	public static boolean matchesData(@ReadOnly ItemStack item, @ReadOnly ItemStack data, boolean matchPartialLists) {
		if (item == data) return true;
		if (data == null) return true;
		if (item == null) return false;
		// Compare item types:
		if (item.getType() != data.getType()) return false;

		// Check if meta data is contained in item:
		return matchesData(item.getItemMeta(), data.getItemMeta(), matchPartialLists);
	}

	public static boolean matchesData(@ReadOnly ItemStack item, Material dataType, @ReadOnly Map<String, @ReadOnly Object> data, boolean matchPartialLists) {
		assert dataType != null;
		if (item == null) return false;
		if (item.getType() != dataType) return false;
		if (data == null || data.isEmpty()) return true;
		return matchesData(item.getItemMeta(), data, matchPartialLists);
	}

	public static boolean matchesData(UnmodifiableItemStack item, Material dataType, @ReadOnly Map<String, @ReadOnly Object> data, boolean matchPartialLists) {
		return matchesData(asItemStackOrNull(item), dataType, data, matchPartialLists);
	}

	public static boolean matchesData(@ReadOnly ItemMeta itemMetaData, @ReadOnly ItemMeta dataMetaData) {
		return matchesData(itemMetaData, dataMetaData, false); // Not matching partial lists
	}

	// Checks if the meta data contains the other given meta data.
	// Similar to Minecraft's NBT data matching (trading does not match partial lists, but data specified in commands
	// does), but there are a few differences: Minecraft requires explicitly specified empty lists to perfectly match in
	// all cases, and some data is treated as list in Minecraft but as map in Bukkit (eg. enchantments). But the
	// behavior is the same if not matching partial lists.
	public static boolean matchesData(ItemMeta itemMetaData, ItemMeta dataMetaData, boolean matchPartialLists) {
		if (itemMetaData == dataMetaData) return true;
		if (dataMetaData == null) return true;
		if (itemMetaData == null) return false;

		// TODO Maybe there is a better way of doing this in the future..
		Map<String, Object> itemMetaDataMap = itemMetaData.serialize();
		Map<String, Object> dataMetaDataMap = dataMetaData.serialize();
		return matchesData(itemMetaDataMap, dataMetaDataMap, matchPartialLists);
	}

	public static boolean matchesData(@ReadOnly ItemMeta itemMetaData, @ReadOnly Map<String, @ReadOnly Object> data, boolean matchPartialLists) {
		if (data == null || data.isEmpty()) return true;
		if (itemMetaData == null) return false;
		Map<String, Object> itemMetaDataMap = itemMetaData.serialize();
		return matchesData(itemMetaDataMap, data, matchPartialLists);
	}

	public static boolean matchesData(@ReadOnly Map<String, @ReadOnly Object> itemData, @ReadOnly Map<String, @ReadOnly Object> data, boolean matchPartialLists) {
		return _matchesData(itemData, data, matchPartialLists);
	}

	private static boolean _matchesData(@ReadOnly Object target, @ReadOnly Object data, boolean matchPartialLists) {
		if (target == data) return true;
		if (data == null) return true;
		if (target == null) return false;

		// Check if map contains given data:
		if (data instanceof Map) {
			if (!(target instanceof Map)) return false;
			Map<?, ?> targetMap = (Map<?, ?>) target;
			Map<?, ?> dataMap = (Map<?, ?>) data;
			for (Entry<?, ?> entry : dataMap.entrySet()) {
				Object targetValue = targetMap.get(entry.getKey());
				if (!_matchesData(targetValue, entry.getValue(), matchPartialLists)) {
					return false;
				}
			}
			return true;
		}

		// Check if list contains given data:
		if (matchPartialLists && data instanceof List) {
			if (!(target instanceof List)) return false;
			List<?> targetList = (List<?>) target;
			List<?> dataList = (List<?>) data;
			// If empty list is explicitly specified, then target list has to be empty as well:
			/*if (dataList.isEmpty()) {
				return targetList.isEmpty();
			}*/
			// Avoid loop (TODO: only works if dataList doesn't contain duplicate entries):
			if (dataList.size() > targetList.size()) {
				return false;
			}
			for (Object dataEntry : dataList) {
				boolean dataContained = false;
				for (Object targetEntry : targetList) {
					if (_matchesData(targetEntry, dataEntry, matchPartialLists)) {
						dataContained = true;
						break;
					}
				}
				if (!dataContained) {
					return false;
				}
			}
			return true;
		}

		// Check if objects are equal:
		return data.equals(target);
	}

	// PREDICATES

	private static final Predicate<@ReadOnly ItemStack> EMPTY_ITEMS = ItemUtils::isEmpty;
	private static final Predicate<@ReadOnly ItemStack> NON_EMPTY_ITEMS = (itemStack) -> !isEmpty(itemStack);

	/**
	 * Gets a {@link Predicate} that accepts {@link #isEmpty(ItemStack) empty} {@link ItemStack ItemStacks}.
	 * 
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> emptyItems() {
		return EMPTY_ITEMS;
	}

	/**
	 * Gets a {@link Predicate} that accepts {@link #isEmpty(ItemStack) non-empty} {@link ItemStack ItemStacks}.
	 * 
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> nonEmptyItems() {
		return NON_EMPTY_ITEMS;
	}

	/**
	 * Gets a {@link Predicate} that accepts {@link ItemStack ItemStacks} that {@link ItemData#matches(ItemStack) match}
	 * the given {@link ItemData}.
	 * 
	 * @param itemData
	 *            the ItemData, not <code>null</code>
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> matchingItems(ItemData itemData) {
		Validate.notNull(itemData, "itemData is null");
		return (itemStack) -> itemData.matches(itemStack);
	}

	/**
	 * Gets a {@link Predicate} that accepts {@link #isEmpty(ItemStack) non-empty} {@link ItemStack ItemStacks} that
	 * {@link ItemData#matches(ItemStack) match} any of the given {@link ItemData}.
	 * 
	 * @param itemDataList
	 *            the list of ItemData, not <code>null</code> and does not contain <code>null</code>
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> matchingItems(@ReadOnly List<ItemData> itemDataList) {
		Validate.notNull(itemDataList, "itemDataList is null");
		assert !itemDataList.contains(null);
		return (itemStack) -> {
			if (isEmpty(itemStack)) return false;
			for (ItemData itemData : itemDataList) {
				assert itemData != null;
				if (itemData.matches(itemStack)) {
					return true;
				} // Else: Continue.
			}
			return false;
		};
	}

	/**
	 * Gets a {@link Predicate} that accepts {@link ItemStack ItemStacks} that are {@link ItemStack#isSimilar(ItemStack)
	 * similar} to the given {@link ItemStack}.
	 * 
	 * @param itemStack
	 *            the item stack, not <code>null</code>
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> similarItems(@ReadOnly ItemStack itemStack) {
		Validate.notNull(itemStack, "itemStack is null");
		return (otherItemStack) -> itemStack.isSimilar(otherItemStack);
	}

	/**
	 * Gets a {@link Predicate} that accepts {@link ItemStack ItemStacks} that are
	 * {@link UnmodifiableItemStack#isSimilar(ItemStack) similar} to the given {@link UnmodifiableItemStack}.
	 * 
	 * @param itemStack
	 *            the item stack, not <code>null</code>
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> similarItems(UnmodifiableItemStack itemStack) {
		Validate.notNull(itemStack, "itemStack is null");
		return (otherItemStack) -> itemStack.isSimilar(otherItemStack);
	}

	/**
	 * Gets a {@link Predicate} that accepts {@link ItemStack ItemStacks} that are of the specified {@link Material
	 * type}.
	 * 
	 * @param itemType
	 *            the item type, not <code>null</code>
	 * @return the Predicate
	 */
	public static Predicate<@ReadOnly ItemStack> itemsOfType(Material itemType) {
		Validate.notNull(itemType, "itemType is null");
		return (itemStack) -> itemStack.getType() == itemType;
	}

	// ItemStack migration

	private static Inventory DUMMY_INVENTORY = null;

	// Use newItemStack.isSimilar(oldItemStack) to test whether the item was migrated.
	public static ItemStack migrateItemStack(@ReadOnly ItemStack itemStack) {
		if (itemStack == null) return null;
		if (DUMMY_INVENTORY == null) {
			DUMMY_INVENTORY = Bukkit.createInventory(null, 9);
		}

		// Inserting an ItemStack into a Minecraft inventory will convert it to a corresponding nms.ItemStack and
		// thereby trigger any Minecraft data migrations for the ItemStack.
		DUMMY_INVENTORY.setItem(0, itemStack);
		ItemStack convertedItemStack = DUMMY_INVENTORY.getItem(0);
		DUMMY_INVENTORY.setItem(0, null);
		return convertedItemStack;
	}

	public static UnmodifiableItemStack migrateItemStack(UnmodifiableItemStack itemStack) {
		return UnmodifiableItemStack.of(migrateItemStack(asItemStackOrNull(itemStack)));
	}

	// Converts the given ItemStack to conform to Spigot's internal data format by running it through Spigot's item
	// de/serialization. Use oldItemStack.isSimilar(newItemStack) to test whether the item has changed.
	// Note: This is performing much better compared to serializing and deserializing a YAML config containing the item.
	public static ItemStack convertItem(@ReadOnly ItemStack itemStack) {
		if (itemStack == null) return null;
		ItemMeta itemMeta = itemStack.getItemMeta(); // Can be null
		Map<String, Object> serializedItemMeta = serializeItemMeta(itemMeta); // Can be null
		if (serializedItemMeta == null) {
			// Item has no ItemMeta that could get converted:
			return itemStack;
		}
		ItemMeta deserializedItemMeta = deserializeItemMeta(serializedItemMeta); // Can be null
		// TODO Avoid copy (also copies the metadata again) by serializing and deserializing the complete ItemStack?
		ItemStack convertedItemStack = itemStack.clone();
		convertedItemStack.setItemMeta(deserializedItemMeta);
		return convertedItemStack;
	}

	public static int convertItems(@ReadWrite ItemStack @ReadOnly [] contents, Predicate<@ReadOnly ItemStack> filter) {
		Validate.notNull(contents, "contents is null");
		filter = PredicateUtils.orAlwaysTrue(filter);
		int convertedStacks = 0;
		for (int slot = 0; slot < contents.length; slot++) {
			ItemStack slotItem = contents[slot];
			if (isEmpty(slotItem)) continue;
			if (!filter.test(slotItem)) continue;
			ItemStack convertedItem = convertItem(slotItem);
			if (!slotItem.isSimilar(convertedItem)) {
				contents[slot] = convertedItem;
				convertedStacks += 1;
			}
		}
		return convertedStacks;
	}

	public static int convertItems(Inventory inventory, Predicate<@ReadOnly ItemStack> filter, boolean updateViewers) {
		Validate.notNull(inventory, "inventory is null");
		filter = PredicateUtils.orAlwaysTrue(filter);

		// Convert inventory contents (includes armor and off hand slots for player inventories):
		ItemStack[] contents = inventory.getContents();
		int convertedStacks = convertItems(contents, filter);
		if (convertedStacks > 0) {
			// Apply changes back to the inventory:
			setContents(inventory, contents);
		}

		if (inventory instanceof PlayerInventory) {
			// Also convert the item on the cursor:
			Player player = (Player) ((PlayerInventory) inventory).getHolder();
			ItemStack cursor = player.getItemOnCursor();
			if (!ItemUtils.isEmpty(cursor) && filter.test(cursor)) {
				ItemStack convertedCursor = ItemUtils.convertItem(cursor);
				if (!cursor.isSimilar(convertedCursor)) {
					convertedStacks += 1;
				}
			}
		}

		if (convertedStacks > 0 && updateViewers) {
			// Update inventory viewers and owner:
			if (updateViewers) {
				updateInventoryLater(inventory);
			}
		}
		return convertedStacks;
	}

	// ItemStack serialization

	private static final String ITEM_META_SERIALIZATION_KEY = "ItemMeta";

	static Map<String, Object> serializeItemMeta(@ReadOnly ItemMeta itemMeta) {
		// Check whether ItemMeta is empty; equivalent to ItemStack#hasItemMeta
		if (itemMeta != null && !Bukkit.getItemFactory().equals(itemMeta, null)) {
			return itemMeta.serialize(); // Assert: Not null or empty
		} else {
			return null;
		}
	}

	static ItemMeta deserializeItemMeta(@ReadOnly Map<String, @ReadOnly Object> itemMetaData) {
		if (itemMetaData == null) return null;
		// Get the class CraftBukkit internally uses for the deserialization:
		Class<? extends ConfigurationSerializable> serializableItemMetaClass = ConfigurationSerialization.getClassByAlias(ITEM_META_SERIALIZATION_KEY);
		if (serializableItemMetaClass == null) {
			throw new IllegalStateException("Missing ItemMeta ConfigurationSerializable class for key/alias '" + ITEM_META_SERIALIZATION_KEY + "'!");
		}
		// Can be null:
		ItemMeta itemMeta = (ItemMeta) ConfigurationSerialization.deserializeObject(itemMetaData, serializableItemMetaClass);
		return itemMeta;
	}

	// Inventory utilities:

	private static final ItemStack[] EMPTY_ITEMSTACK_ARRAY = new ItemStack[0];

	/**
	 * Returns an empty array of {@link ItemStack ItemStacks}.
	 * 
	 * @return the empty array
	 */
	public static final ItemStack[] emptyItemStackArray() {
		return EMPTY_ITEMSTACK_ARRAY;
	}

	/**
	 * Checks whether the given {@link Player} is currently viewing an inventory.
	 * <p>
	 * Because opening the own inventory does not inform the server, this method cannot detect if the player is
	 * currently viewing his own inventory or the creative mode inventory.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @return <code>true</code> if the player has currently an inventory open (that is not his own inventory)
	 */
	public static boolean hasInventoryOpen(Player player) {
		InventoryType inventoryType = player.getOpenInventory().getType();
		return inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE;
	}

	/**
	 * Checks if the given contents contains at least the specified amount of items that are accepted by the given
	 * {@link Predicate}.
	 * <p>
	 * The given Predicate is only invoked for {@link #isEmpty(ItemStack) non-empty} ItemStacks.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param predicate
	 *            the predicate, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(@ReadOnly ItemStack @ReadOnly [] contents, Predicate<@ReadOnly ItemStack> predicate, int amount) {
		Validate.notNull(predicate, "predicate is null");
		if (amount <= 0) return true;
		if (contents == null) return false;
		int remainingAmount = amount;
		for (ItemStack itemStack : contents) {
			if (isEmpty(itemStack)) continue;
			if (!predicate.test(itemStack)) continue;
			remainingAmount -= itemStack.getAmount();
			if (remainingAmount <= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given contents contains at least the specified amount of items that
	 * {@link ItemData#matches(ItemStack) match} the given {@link ItemData}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemData
	 *            the item data to check for, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(@ReadOnly ItemStack @ReadOnly [] contents, ItemData itemData, int amount) {
		return containsAtLeast(contents, matchingItems(itemData), amount);
	}

	/**
	 * Checks if the given contents contains at least the specified amount of items that are
	 * {@link ItemStack#isSimilar(ItemStack) similar} to the given {@link ItemStack}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemStack
	 *            the item stack to check for, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(@ReadOnly ItemStack @ReadOnly [] contents, @ReadOnly ItemStack itemStack, int amount) {
		return containsAtLeast(contents, similarItems(itemStack), amount);
	}

	/**
	 * Checks if the given contents contains at least the specified amount of items that are
	 * {@link UnmodifiableItemStack#isSimilar(ItemStack) similar} to the given {@link ItemStack}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemStack
	 *            the item stack to check for, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(@ReadOnly ItemStack @ReadOnly [] contents, UnmodifiableItemStack itemStack, int amount) {
		return containsAtLeast(contents, similarItems(itemStack), amount);
	}

	/**
	 * Checks if the given contents contains at least one item that {@link ItemData#matches(ItemStack) matches} the
	 * given {@link ItemData}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemData
	 *            the item data to check for, not <code>null</code>
	 * @return <code>true</code> if an item was found
	 */
	public static boolean contains(@ReadOnly ItemStack @ReadOnly [] contents, ItemData itemData) {
		return containsAtLeast(contents, itemData, 1);
	}

	/**
	 * Checks if the given contents contains at least one item that is {@link ItemStack#isSimilar(ItemStack) similar} to
	 * the given {@link ItemStack}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemStack
	 *            the item stack to check for, not <code>null</code>
	 * @return <code>true</code> if an item was found
	 */
	public static boolean contains(@ReadOnly ItemStack @ReadOnly [] contents, @ReadOnly ItemStack itemStack) {
		return containsAtLeast(contents, itemStack, 1);
	}

	// ItemStack Iterable

	/**
	 * Checks if the given contents contains at least the specified amount of items that are accepted by the given
	 * {@link Predicate}.
	 * <p>
	 * The given Predicate is only invoked for {@link #isEmpty(ItemStack) non-empty} ItemStacks.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param predicate
	 *            the predicate, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(Iterable<@ReadOnly ItemStack> contents, Predicate<@ReadOnly ItemStack> predicate, int amount) {
		Validate.notNull(predicate, "predicate is null");
		if (amount <= 0) return true;
		if (contents == null) return false;
		int remainingAmount = amount;
		for (ItemStack itemStack : contents) {
			if (isEmpty(itemStack)) continue;
			if (!predicate.test(itemStack)) continue;
			remainingAmount -= itemStack.getAmount();
			if (remainingAmount <= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given contents contains at least the specified amount of items that
	 * {@link ItemData#matches(ItemStack) match} the given {@link ItemData}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemData
	 *            the item data to check for, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(Iterable<@ReadOnly ItemStack> contents, ItemData itemData, int amount) {
		return containsAtLeast(contents, matchingItems(itemData), amount);
	}

	/**
	 * Checks if the given contents contains at least the specified amount of items that are
	 * {@link ItemStack#isSimilar(ItemStack) similar} to the given {@link ItemStack}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemStack
	 *            the item stack to check for, not <code>null</code>
	 * @param amount
	 *            the amount of items to check for
	 * @return <code>true</code> if at least the specified amount of items were found
	 */
	public static boolean containsAtLeast(Iterable<@ReadOnly ItemStack> contents, @ReadOnly ItemStack itemStack, int amount) {
		return containsAtLeast(contents, similarItems(itemStack), amount);
	}

	/**
	 * Checks if the given contents contains at least one item that {@link ItemData#matches(ItemStack) matches} the
	 * given {@link ItemData}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemData
	 *            the item data to check for, not <code>null</code>
	 * @return <code>true</code> if an item was found
	 */
	public static boolean contains(Iterable<@ReadOnly ItemStack> contents, ItemData itemData) {
		return containsAtLeast(contents, itemData, 1);
	}

	/**
	 * Checks if the given contents contains at least one item that is {@link ItemStack#isSimilar(ItemStack) similar} to
	 * the given {@link ItemStack}.
	 * 
	 * @param contents
	 *            the contents to search through
	 * @param itemStack
	 *            the item stack to check for, not <code>null</code>
	 * @return <code>true</code> if an item was found
	 */
	public static boolean contains(Iterable<@ReadOnly ItemStack> contents, @ReadOnly ItemStack itemStack) {
		return containsAtLeast(contents, itemStack, 1);
	}

	// -----

	/**
	 * Adds the given {@link ItemStack} to the given contents.
	 * <p>
	 * See {@link #addItems(ItemStack[], UnmodifiableItemStack, int)}.
	 * 
	 * @param contents
	 *            the contents to add the items to
	 * @param itemStack
	 *            the item stack to add
	 * @return the amount of items that could not be added, <code>0</code> on complete success
	 */
	public static int addItems(@ReadWrite ItemStack @ReadOnly [] contents, @ReadOnly ItemStack itemStack) {
		return addItems(contents, itemStack, getItemStackAmount(itemStack));
	}

	/**
	 * Adds the specified amount of items of the given {@link ItemStack} to the given contents.
	 * <p>
	 * See {@link #addItems(ItemStack[], UnmodifiableItemStack, int)}.
	 * 
	 * @param contents
	 *            the contents to add the items to
	 * @param item
	 *            the item to add
	 * @return the amount of items that could not be added, <code>0</code> on complete success
	 */
	public static int addItems(@ReadWrite ItemStack @ReadOnly [] contents, @ReadOnly ItemStack item, int amount) {
		return addItems(contents, UnmodifiableItemStack.of(item), amount);
	}

	/**
	 * Adds the given {@link UnmodifiableItemStack} to the given contents.
	 * <p>
	 * See {@link #addItems(ItemStack[], UnmodifiableItemStack, int)}.
	 * 
	 * @param contents
	 *            the contents to add the items to
	 * @param itemStack
	 *            the item stack
	 * @return the amount of items that could not be added, <code>0</code> on complete success
	 */
	public static int addItems(@ReadWrite ItemStack @ReadOnly [] contents, UnmodifiableItemStack itemStack) {
		return addItems(contents, itemStack, getItemStackAmount(itemStack));
	}

	/**
	 * Adds the specified amount of items of the given {@link UnmodifiableItemStack} to the given contents.
	 * <p>
	 * This first tries to fill similar partial item stacks in the contents up to the item's max stack size. Afterwards,
	 * it inserts the remaining amount of items into empty slots, splitting at the item's max stack size.
	 * <p>
	 * The given item stack to add is copied before it is inserted into empty slots of the contents array.
	 * <p>
	 * This operation does not modify the original item stacks in the contents array: If it has to modify the amount of
	 * an item stack, it first replaces it with a copy inside the contents array. Consequently, if the item stacks of
	 * the given contents array are mirroring changes to their Minecraft counterparts, those underlying Minecraft item
	 * stacks are not affected by this operation until the modified contents array is applied back to the Minecraft
	 * inventory.
	 * 
	 * @param contents
	 *            the contents to add the items to
	 * @param item
	 *            the item to add
	 * @param amount
	 *            the amount to add
	 * @return the amount of items that could not be added, <code>0</code> on complete success
	 */
	public static int addItems(@ReadWrite ItemStack @ReadOnly [] contents, UnmodifiableItemStack item, int amount) {
		Validate.notNull(contents, "contents is null");
		Validate.notNull(item, "item is null");
		Validate.isTrue(amount >= 0, "amount is negative");
		if (amount == 0) return 0;

		// Search for partially fitting item stacks:
		int maxStackSize = item.getMaxStackSize();
		int size = contents.length;
		for (int slot = 0; slot < size; slot++) {
			ItemStack slotItem = contents[slot];

			// Slot empty? - Skip, because we are currently filling existing item stacks up.
			if (isEmpty(slotItem)) continue;

			// Slot already full?
			int slotAmount = slotItem.getAmount();
			if (slotAmount >= maxStackSize) continue;

			if (item.isSimilar(slotItem)) {
				// Copy ItemStack, so we don't modify the original ItemStack:
				slotItem = slotItem.clone();
				contents[slot] = slotItem;

				int newAmount = slotAmount + amount;
				if (newAmount <= maxStackSize) {
					// Remaining amount did fully fit into this stack:
					slotItem.setAmount(newAmount);
					return 0;
				} else {
					// Did not fully fit:
					slotItem.setAmount(maxStackSize);
					amount -= (maxStackSize - slotAmount);
					assert amount != 0;
				}
			}
		}

		// We have items remaining:
		assert amount > 0;

		// Search for empty slots:
		for (int slot = 0; slot < size; slot++) {
			ItemStack slotItem = contents[slot];
			if (isEmpty(slotItem)) {
				// Found an empty slot:
				if (amount > maxStackSize) {
					// Add full stack:
					ItemStack stack = item.copy();
					stack.setAmount(maxStackSize);
					contents[slot] = stack;
					amount -= maxStackSize;
				} else {
					// The remaining amount completely fits as a single stack:
					ItemStack stack = item.copy();
					stack.setAmount(amount);
					contents[slot] = stack;
					return 0;
				}
			}
		}

		// Not all items did fit into the inventory:
		return amount;
	}

	/**
	 * Removes the specified amount of items that match the specified {@link ItemData} from the given contents.
	 * 
	 * @param contents
	 *            the contents to remove the items from
	 * @param itemData
	 *            the item data to match
	 * @param amount
	 *            the amount of matching items to remove
	 * @return the amount of items that could not be removed, or <code>0</code> if all items were removed
	 * @see #removeItems(ItemStack[], Predicate, int)
	 */
	public static int removeItems(@ReadWrite ItemStack @ReadOnly [] contents, ItemData itemData, int amount) {
		return removeItems(contents, matchingItems(itemData), amount);
	}

	/**
	 * Removes the given {@link ItemStack} from the given contents.
	 * 
	 * @param contents
	 *            the contents to remove the items from
	 * @param itemStack
	 *            the item stack to remove
	 * @return the amount of items that could not be removed, or <code>0</code> if all items were removed
	 * @see #removeItems(ItemStack[], Predicate, int)
	 */
	public static int removeItems(@ReadWrite ItemStack @ReadOnly [] contents, @ReadOnly ItemStack itemStack) {
		return removeItems(contents, similarItems(itemStack), itemStack.getAmount());
	}

	/**
	 * Removes the given {@link UnmodifiableItemStack} from the given contents.
	 * 
	 * @param contents
	 *            the contents to remove the items from
	 * @param itemStack
	 *            the item stack to remove
	 * @return the amount of items that could not be removed, or <code>0</code> if all items were removed
	 * @see #removeItems(ItemStack[], Predicate, int)
	 */
	public static int removeItems(@ReadWrite ItemStack @ReadOnly [] contents, UnmodifiableItemStack itemStack) {
		return removeItems(contents, similarItems(itemStack), itemStack.getAmount());
	}

	/**
	 * Removes the specified amount of items accepted by the given {@link Predicate} from the given contents.
	 * <p>
	 * If the specified amount is {@link Integer#MAX_VALUE}, then all items matching the Predicate are removed from the
	 * contents.
	 * <p>
	 * This operation does not modify the original item stacks in the contents array: If it has to modify the amount of
	 * an item stack, it first replaces it with a copy inside the contents array. Consequently, if the item stacks of
	 * the given contents array are mirroring changes to their Minecraft counterparts, those underlying Minecraft item
	 * stacks are not affected by this operation until the modified contents array is applied back to the Minecraft
	 * inventory.
	 * 
	 * @param contents
	 *            the contents to remove the items from
	 * @param itemMatcher
	 *            the item matcher
	 * @param amount
	 *            the amount of items to remove
	 * @return the amount of items that could not be removed, or <code>0</code> if all items were removed
	 */
	public static int removeItems(@ReadWrite ItemStack @ReadOnly [] contents, Predicate<@ReadOnly ItemStack> itemMatcher, int amount) {
		Validate.notNull(contents, "contents is null");
		Validate.notNull(itemMatcher, "itemMatcher is null");
		Validate.isTrue(amount >= 0, "amount is negative");
		if (amount == 0) return 0;

		boolean removeAll = (amount == Integer.MAX_VALUE);
		for (int slot = 0; slot < contents.length; slot++) {
			ItemStack slotItem = contents[slot];
			if (ItemUtils.isEmpty(slotItem)) continue;
			if (!itemMatcher.test(slotItem)) continue;

			if (removeAll) {
				contents[slot] = null;
			} else {
				int newAmount = slotItem.getAmount() - amount;
				if (newAmount > 0) {
					// Copy the ItemStack, so that we do not modify the original ItemStack (in case that we do not want
					// to apply the changed inventory contents afterwards):
					slotItem = slotItem.clone();
					contents[slot] = slotItem;
					slotItem.setAmount(newAmount);
					// All items were removed:
					return 0;
				} else {
					contents[slot] = null;
					amount = -newAmount;
					if (amount == 0) {
						// All items were removed:
						return 0;
					}
				}
			}
		}

		if (removeAll) return 0;
		return amount;
	}

	public static void setStorageContents(Inventory inventory, @ReadOnly ItemStack @ReadOnly [] contents) {
		setContents(inventory, contents);
	}

	public static void setContents(Inventory inventory, @ReadOnly ItemStack @ReadOnly [] contents) {
		setContents(inventory, 0, contents);
	}

	public static void setContents(Inventory inventory, int slotOffset, @ReadOnly ItemStack @ReadOnly [] contents) {
		Validate.notNull(inventory, "inventory is null");
		Validate.notNull(contents, "contents is null");
		// Assert: slotOffset is valid.
		final int length = contents.length;
		for (int slot = 0; slot < length; ++slot) {
			ItemStack newItem = contents[slot];
			int inventorySlot = slotOffset + slot;
			ItemStack currentItem = inventory.getItem(inventorySlot);
			// Only update slots that actually changed. This avoids sending the player slot update packets that are not
			// required.
			// We skip the slot if the current item already equals the new item (similar and same stack sizes). For
			// unchanged items (CraftItemStack wrappers) and items with changed stack size this is quite performant.
			if (Objects.equals(newItem, currentItem)) {
				continue;
			}
			inventory.setItem(inventorySlot, newItem); // This copies the item internally
		}
	}

	public static void updateInventoryLater(Inventory inventory) {
		// If the inventory belongs to a player, always update it for that player:
		Player owner = null;
		if (inventory instanceof PlayerInventory) {
			assert inventory.getHolder() instanceof Player;
			owner = (Player) inventory.getHolder();
			assert owner != null;
			ItemUtils.updateInventoryLater(owner);
		}
		// If there are any (other) currently viewing players, update for those as well:
		for (HumanEntity viewer : inventory.getViewers()) {
			if (viewer instanceof Player) {
				if (!viewer.equals(owner)) {
					ItemUtils.updateInventoryLater((Player) viewer);
				}
			}
		}
	}

	public static void updateInventoryLater(Player player) {
		Bukkit.getScheduler().runTask(ShopkeepersPlugin.getInstance(), () -> player.updateInventory());
	}

	// Only closes the player's open inventory view if it is still the specified view after the delay:
	public static void closeInventoryDelayed(InventoryView inventoryView) {
		Bukkit.getScheduler().runTask(ShopkeepersPlugin.getInstance(), () -> {
			InventoryView openInventoryView = inventoryView.getPlayer().getOpenInventory();
			if (inventoryView == openInventoryView) {
				inventoryView.close(); // Same as player.closeInventory()
			}
		});
	}

	public static void closeInventoryDelayed(Player player) {
		Bukkit.getScheduler().runTask(ShopkeepersPlugin.getInstance(), () -> player.closeInventory());
	}

	// This can for example be used during the handling of inventory interaction events.
	public static void setItemDelayed(Inventory inventory, int slot, @ReadOnly ItemStack itemStack) {
		assert inventory != null;
		Bukkit.getScheduler().runTask(ShopkeepersPlugin.getInstance(), () -> {
			inventory.setItem(slot, itemStack); // This copies the item internally
		});
	}

	// TODO Replace this with the corresponding Bukkit API method added in late 1.15.2
	// See https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/da9ef3c55fa3bce91f7fdcd77d50171be7297d7d
	public static ItemStack getItem(PlayerInventory playerInventory, EquipmentSlot slot) {
		if (playerInventory == null || slot == null) return null;
		switch (slot) {
		case HAND:
			return playerInventory.getItemInMainHand();
		case OFF_HAND:
			return playerInventory.getItemInOffHand();
		case FEET:
			return playerInventory.getBoots();
		case LEGS:
			return playerInventory.getLeggings();
		case CHEST:
			return playerInventory.getChestplate();
		case HEAD:
			return playerInventory.getHelmet();
		default:
			return null;
		}
	}
}
