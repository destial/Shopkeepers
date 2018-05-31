package com.nisovin.shopkeepers.shopobjects.living;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.AbstractShopObjectType;
import com.nisovin.shopkeepers.Settings;
import com.nisovin.shopkeepers.ShopObject;
import com.nisovin.shopkeepers.api.ShopCreationData;
import com.nisovin.shopkeepers.api.Shopkeeper;
import com.nisovin.shopkeepers.util.StringUtils;
import com.nisovin.shopkeepers.util.Utils;

public class LivingEntityObjectType extends AbstractShopObjectType {

	protected final EntityType entityType;
	protected final List<String> aliases;

	protected LivingEntityObjectType(EntityType entityType, List<String> aliases, String identifier, String permission) {
		super(identifier, permission);
		this.entityType = entityType;
		assert entityType.isAlive();
		// assert: aliases are normalized
		this.aliases = (aliases != null ? aliases : Collections.emptyList());
	}

	public EntityType getEntityType() {
		return entityType;
	}

	@Override
	public boolean hasPermission(Player player) {
		return super.hasPermission(player) || Utils.hasPermission(player, "shopkeeper.entity.*");
	}

	@Override
	protected ShopObject createObject(Shopkeeper shopkeeper, ShopCreationData creationData) {
		return new LivingEntityShop(shopkeeper, creationData, this);
	}

	@Override
	public boolean isEnabled() {
		return Settings.enabledLivingShops.contains(entityType.name());
	}

	@Override
	public boolean matches(String identifier) {
		identifier = StringUtils.normalize(identifier);
		if (super.matches(identifier)) return true;
		for (String alias : aliases) {
			if (identifier.startsWith(alias)) return true;
		}
		return false;
	}

	@Override
	protected void onSelect(Player player) {
		// TODO translation support for the entity type name?
		Utils.sendMessage(player, Settings.msgSelectedLivingShop, "{type}", entityType.name());
	}

	@Override
	public boolean needsSpawning() {
		return true; // despawn shop entities on chunk unload, and spawn them again on chunk load
	}
}
