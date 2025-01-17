package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.property.BooleanProperty;
import com.nisovin.shopkeepers.property.EnumProperty;
import com.nisovin.shopkeepers.property.Property;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.defaults.EditorHandler;
import com.nisovin.shopkeepers.util.EnumUtils;
import com.nisovin.shopkeepers.util.ItemUtils;

public class WolfShop extends SittableShop<Wolf> {

	private final Property<Boolean> angryProperty = new BooleanProperty(shopkeeper, "angry", false);
	private final Property<DyeColor> collarColorProperty = new EnumProperty<DyeColor>(shopkeeper, DyeColor.class, "collarColor", null) {
		@Override
		public boolean isNullable() {
			return true; // Null to indicate 'no collar' / untamed
		}
	};

	public WolfShop(LivingShops livingShops, SKLivingShopObjectType<WolfShop> livingObjectType,
					AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ConfigurationSection configSection) {
		super.load(configSection);
		angryProperty.load(configSection);
		collarColorProperty.load(configSection);
	}

	@Override
	public void save(ConfigurationSection configSection) {
		super.save(configSection);
		angryProperty.save(configSection);
		collarColorProperty.save(configSection);
	}

	@Override
	protected void onSpawn(Wolf entity) {
		super.onSpawn(entity);
		this.applyAngry(entity);
		this.applyCollarColor(entity);
	}

	@Override
	public List<EditorHandler.Button> createEditorButtons() {
		List<EditorHandler.Button> editorButtons = super.createEditorButtons();
		editorButtons.add(this.getAngryEditorButton());
		editorButtons.add(this.getCollarColorEditorButton());
		return editorButtons;
	}

	// ANGRY

	public boolean isAngry() {
		return angryProperty.getValue();
	}

	public void setAngry(boolean angry) {
		angryProperty.setValue(angry);
		shopkeeper.markDirty();
		this.applyAngry(this.getEntity()); // Null if not spawned
	}

	public void cycleAngry() {
		this.setAngry(!this.isAngry());
	}

	private void applyAngry(Wolf entity) {
		if (entity == null) return;
		entity.setAngry(this.isAngry());
	}

	private ItemStack getAngryEditorItem() {
		ItemStack iconItem = new ItemStack(this.isAngry() ? Material.RED_WOOL : Material.WHITE_WOOL);
		ItemUtils.setDisplayNameAndLore(iconItem, Messages.buttonWolfAngry, Messages.buttonWolfAngryLore);
		return iconItem;
	}

	private EditorHandler.Button getAngryEditorButton() {
		return new EditorHandler.ShopkeeperActionButton() {
			@Override
			public ItemStack getIcon(EditorHandler.Session session) {
				return getAngryEditorItem();
			}

			@Override
			protected boolean runAction(InventoryClickEvent clickEvent, Player player) {
				cycleAngry();
				return true;
			}
		};
	}

	// COLLAR COLOR

	public DyeColor getCollarColor() {
		return collarColorProperty.getValue();
	}

	public void setCollarColor(DyeColor collarColor) {
		collarColorProperty.setValue(collarColor);
		shopkeeper.markDirty();
		this.applyCollarColor(this.getEntity()); // Null if not spawned
	}

	public void cycleCollarColor(boolean backwards) {
		this.setCollarColor(EnumUtils.cycleEnumConstantNullable(DyeColor.class, this.getCollarColor(), backwards));
	}

	private void applyCollarColor(Wolf entity) {
		if (entity == null) return;
		DyeColor collarColor = this.getCollarColor();
		if (collarColor == null) {
			// No collar / untamed:
			entity.setTamed(false);
		} else {
			entity.setTamed(true); // Only tamed cats will show the collar
			entity.setCollarColor(collarColor);
		}
	}

	private ItemStack getCollarColorEditorItem() {
		DyeColor collarColor = this.getCollarColor();
		ItemStack iconItem;
		if (collarColor == null) {
			iconItem = new ItemStack(Material.BARRIER);
		} else {
			iconItem = new ItemStack(ItemUtils.getWoolType(collarColor));
		}
		ItemUtils.setDisplayNameAndLore(iconItem, Messages.buttonCollarColor, Messages.buttonCollarColorLore);
		return iconItem;
	}

	private EditorHandler.Button getCollarColorEditorButton() {
		return new EditorHandler.ShopkeeperActionButton() {
			@Override
			public ItemStack getIcon(EditorHandler.Session session) {
				return getCollarColorEditorItem();
			}

			@Override
			protected boolean runAction(InventoryClickEvent clickEvent, Player player) {
				boolean backwards = clickEvent.isRightClick();
				cycleCollarColor(backwards);
				return true;
			}
		};
	}
}
