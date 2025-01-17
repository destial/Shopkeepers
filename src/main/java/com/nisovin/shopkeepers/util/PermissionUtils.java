package com.nisovin.shopkeepers.util;

import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public class PermissionUtils {

	private PermissionUtils() {
	}

	/**
	 * Performs a permissions check and logs debug information about it.
	 * 
	 * @param permissible
	 *            the permissible, not <code>null</code>
	 * @param permission
	 *            the permission
	 * @return <code>true</code> if the permissible has the specified permission, or <code>false</code> otherwise
	 */
	public static boolean hasPermission(Permissible permissible, String permission) {
		assert permissible != null;
		boolean hasPermission = permissible.hasPermission(permission);
		if (!hasPermission && (permissible instanceof Player)) {
			Log.debug(() -> "Player '" + ((Player) permissible).getName() + "' does not have permission '" + permission + "'.");
		}
		return hasPermission;
	}

	/**
	 * {@link PluginManager#addPermission(Permission) Registers} the given permission node, if it is not already
	 * registered.
	 * 
	 * @param permissionNode
	 *            the permission node, not <code>null</code> or empty
	 * @param permissionProvider
	 *            function that is lazily invoked if the permission is not yet registered and then provides the new
	 *            permission to register
	 * @return <code>true</code> if the permission got newly registered, <code>false</code> if it was already registered
	 */
	public static boolean registerPermission(String permissionNode, Function<String, Permission> permissionProvider) {
		Validate.notEmpty(permissionNode, "permissionNode is null or empty");
		Validate.notNull(permissionProvider, "permissionProvider is null");

		PluginManager pluginManager = Bukkit.getPluginManager();
		if (pluginManager.getPermission(permissionNode) != null) {
			// The permission is already registered:
			return false;
		}

		Permission permission = permissionProvider.apply(permissionNode);
		Validate.notNull(permission, "permissionProvider returned a null permission");
		pluginManager.addPermission(permission);
		return true;
	}

	/**
	 * {@link PluginManager#addPermission(Permission) Registers} the given permission node, if it is not already
	 * registered.
	 * <p>
	 * The permission will use {@link PermissionDefault#FALSE} as its default.
	 * 
	 * @param permissionNode
	 *            the permission node, not <code>null</code> or empty
	 * @return <code>true</code> if the permission got newly registered, <code>false</code> if it was already registered
	 * @see PermissionUtils#registerPermission(String, Function)
	 */
	public static boolean registerPermission(String permissionNode) {
		return registerPermission(permissionNode, node -> new Permission(node, PermissionDefault.FALSE));
	}
}
