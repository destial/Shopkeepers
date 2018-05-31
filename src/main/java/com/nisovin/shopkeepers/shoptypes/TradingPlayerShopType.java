package com.nisovin.shopkeepers.shoptypes;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.shopkeepers.Settings;
import com.nisovin.shopkeepers.ShopkeeperCreateException;
import com.nisovin.shopkeepers.api.ShopCreationData;
import com.nisovin.shopkeepers.api.ShopCreationData.PlayerShopCreationData;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.util.StringUtils;
import com.nisovin.shopkeepers.util.Utils;

public class TradingPlayerShopType extends AbstractPlayerShopType<TradingPlayerShopkeeper> {

	TradingPlayerShopType() {
		super("trade", ShopkeepersPlugin.PLAYER_TRADE_PERMISSION);
	}

	@Override
	public TradingPlayerShopkeeper loadShopkeeper(ConfigurationSection config) throws ShopkeeperCreateException {
		this.validateConfigSection(config);
		TradingPlayerShopkeeper shopkeeper = new TradingPlayerShopkeeper(config);
		this.registerShopkeeper(shopkeeper);
		return shopkeeper;
	}

	@Override
	public TradingPlayerShopkeeper createShopkeeper(ShopCreationData creationData) throws ShopkeeperCreateException {
		this.validateCreationData(creationData);
		TradingPlayerShopkeeper shopkeeper = new TradingPlayerShopkeeper((PlayerShopCreationData) creationData);
		this.registerShopkeeper(shopkeeper);
		return shopkeeper;
	}

	@Override
	public String getCreatedMessage() {
		return Settings.msgTradeShopCreated;
	}

	@Override
	public boolean matches(String identifier) {
		identifier = StringUtils.normalize(identifier);
		if (super.matches(identifier)) return true;
		return identifier.startsWith("trad");
	}

	@Override
	protected void onSelect(Player player) {
		Utils.sendMessage(player, Settings.msgSelectedTradeShop);
	}
}
