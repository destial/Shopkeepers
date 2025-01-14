package com.nisovin.shopkeepers.commands.shopkeepers;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperArgument;
import com.nisovin.shopkeepers.commands.arguments.ShopkeeperFilter;
import com.nisovin.shopkeepers.commands.arguments.TargetShopkeeperFallback;
import com.nisovin.shopkeepers.commands.lib.Command;
import com.nisovin.shopkeepers.commands.lib.CommandContextView;
import com.nisovin.shopkeepers.commands.lib.CommandException;
import com.nisovin.shopkeepers.commands.lib.CommandInput;
import com.nisovin.shopkeepers.commands.lib.arguments.DefaultValueFallback;
import com.nisovin.shopkeepers.commands.lib.arguments.FirstOfArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.LiteralArgument;
import com.nisovin.shopkeepers.commands.lib.arguments.StringArgument;
import com.nisovin.shopkeepers.lang.Messages;
import com.nisovin.shopkeepers.util.ShopkeeperUtils.TargetShopkeeperFilter;
import com.nisovin.shopkeepers.util.TextUtils;

class CommandSetTradePerm extends Command {

	private static final String ARGUMENT_SHOPKEEPER = "shopkeeper";
	private static final String ARGUMENT_NEW_PERMISSION = "perm";
	private static final String ARGUMENT_REMOVE_PERMISSION = "-";
	private static final String ARGUMENT_QUERY_PERMISSION = "?";

	CommandSetTradePerm() {
		super("setTradePerm");

		// Set permission:
		this.setPermission(ShopkeepersPlugin.SET_TRADE_PERM_PERMISSION);

		// Set description:
		this.setDescription(Messages.commandDescriptionSettradeperm);

		// Arguments:
		this.addArgument(new TargetShopkeeperFallback(
				new ShopkeeperArgument(ARGUMENT_SHOPKEEPER, ShopkeeperFilter.ADMIN),
				TargetShopkeeperFilter.ADMIN
		));
		this.addArgument(new FirstOfArgument("permArg", Arrays.asList(
				new DefaultValueFallback<>(new LiteralArgument(ARGUMENT_QUERY_PERMISSION), ARGUMENT_QUERY_PERMISSION),
				new LiteralArgument(ARGUMENT_REMOVE_PERMISSION),
				new StringArgument(ARGUMENT_NEW_PERMISSION)
		), true, true));
	}

	@Override
	protected void execute(CommandInput input, CommandContextView context) throws CommandException {
		CommandSender sender = input.getSender();

		Shopkeeper shopkeeper = context.get(ARGUMENT_SHOPKEEPER);
		assert shopkeeper != null && (shopkeeper instanceof AdminShopkeeper);

		String newTradePerm = context.get(ARGUMENT_NEW_PERMISSION);
		boolean removePerm = context.has(ARGUMENT_REMOVE_PERMISSION);
		String currentTradePerm = ((AdminShopkeeper) shopkeeper).getTradePermission();
		if (currentTradePerm == null) currentTradePerm = "-";

		if (removePerm) {
			// Remove trade permission:
			assert newTradePerm == null;
			TextUtils.sendMessage(sender, Messages.tradePermRemoved, "perm", currentTradePerm);
		} else if (newTradePerm != null) {
			// Set trade permission:
			TextUtils.sendMessage(sender, Messages.tradePermSet, "perm", newTradePerm);
		} else {
			// Display current trade permission:
			TextUtils.sendMessage(sender, Messages.tradePermView, "perm", currentTradePerm);
			return;
		}

		// Set trade permission:
		((AdminShopkeeper) shopkeeper).setTradePermission(newTradePerm);

		// Save:
		shopkeeper.save();
	}
}
