package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.property.EnumProperty;
import com.nisovin.shopkeepers.property.Property;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.defaults.EditorHandler;
import com.nisovin.shopkeepers.util.EnumUtils;
import com.nisovin.shopkeepers.util.ItemUtils;
import com.nisovin.shopkeepers.util.Validate;

public class ZombieVillagerShop extends ZombieShop<ZombieVillager> {

	private final Property<Profession> professionProperty = new EnumProperty<Profession>(shopkeeper, Profession.class, "profession", Profession.NONE);

	public ZombieVillagerShop(	LivingShops livingShops, SKLivingShopObjectType<ZombieVillagerShop> livingObjectType,
								AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ConfigurationSection configSection) {
		super.load(configSection);
		professionProperty.load(configSection);
	}

	@Override
	public void save(ConfigurationSection configSection) {
		super.save(configSection);
		professionProperty.save(configSection);
	}

	@Override
	protected void onSpawn(ZombieVillager entity) {
		super.onSpawn(entity);
		this.applyProfession(entity);
	}

	@Override
	public List<EditorHandler.Button> createEditorButtons() {
		List<EditorHandler.Button> editorButtons = super.createEditorButtons();
		editorButtons.add(this.getProfessionEditorButton());
		return editorButtons;
	}

	// PROFESSION

	public Profession getProfession() {
		return professionProperty.getValue();
	}

	public void setProfession(Profession profession) {
		Validate.notNull(profession, "Profession is null!");
		professionProperty.setValue(profession);
		shopkeeper.markDirty();
		this.applyProfession(this.getEntity()); // Null if not spawned
	}

	public void cycleProfession(boolean backwards) {
		this.setProfession(EnumUtils.cycleEnumConstant(Profession.class, this.getProfession(), backwards));
	}

	private void applyProfession(ZombieVillager entity) {
		if (entity == null) return;
		entity.setVillagerProfession(this.getProfession());
	}

	private ItemStack getProfessionEditorItem() {
		ItemStack iconItem;
		switch (this.getProfession()) {
		case ARMORER:
			iconItem = new ItemStack(Material.BLAST_FURNACE);
			break;
		case BUTCHER:
			iconItem = new ItemStack(Material.SMOKER);
			break;
		case CARTOGRAPHER:
			iconItem = new ItemStack(Material.CARTOGRAPHY_TABLE);
			break;
		case CLERIC:
			iconItem = new ItemStack(Material.BREWING_STAND);
			break;
		case FARMER:
			iconItem = new ItemStack(Material.WHEAT); // Instead of COMPOSTER
			break;
		case FISHERMAN:
			iconItem = new ItemStack(Material.FISHING_ROD); // Instead of BARREL
			break;
		case FLETCHER:
			iconItem = new ItemStack(Material.FLETCHING_TABLE);
			break;
		case LEATHERWORKER:
			iconItem = new ItemStack(Material.LEATHER); // Instead of CAULDRON
			break;
		case LIBRARIAN:
			iconItem = new ItemStack(Material.LECTERN);
			break;
		case MASON:
			iconItem = new ItemStack(Material.STONECUTTER);
			break;
		case SHEPHERD:
			iconItem = new ItemStack(Material.LOOM);
			break;
		case TOOLSMITH:
			iconItem = new ItemStack(Material.SMITHING_TABLE);
			break;
		case WEAPONSMITH:
			iconItem = new ItemStack(Material.GRINDSTONE);
			break;
		case NITWIT:
			iconItem = new ItemStack(Material.LEATHER_CHESTPLATE);
			ItemUtils.setLeatherColor(iconItem, Color.GREEN);
			break;
		case NONE:
		default:
			iconItem = new ItemStack(Material.BARRIER);
			break;
		}
		assert iconItem != null;
		ItemUtils.setDisplayNameAndLore(iconItem, Messages.buttonZombieVillagerProfession, Messages.buttonZombieVillagerProfessionLore);
		return iconItem;
	}

	private EditorHandler.Button getProfessionEditorButton() {
		return new EditorHandler.ShopkeeperActionButton() {
			@Override
			public ItemStack getIcon(EditorHandler.Session session) {
				return getProfessionEditorItem();
			}

			@Override
			protected boolean runAction(InventoryClickEvent clickEvent, Player player) {
				boolean backwards = clickEvent.isRightClick();
				cycleProfession(backwards);
				return true;
			}
		};
	}
}
