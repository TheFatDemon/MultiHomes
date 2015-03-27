package me.thefatdemon.multihome;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
*
* @author Sleaker
*
*/
public class HomePermissions {
	private static PermissionsHandler handler;
	private static Plugin permissionPlugin = null;
	private static Permission vault = null;

	private enum PermissionsHandler {
		VAULT, PERMISSIONSEX, PERMISSIONS, PERMISSIONSBUKKIT, SUPERPERMS, NONE
	}

	public static boolean initialize(JavaPlugin plugin) {
		Plugin perm = Bukkit.getServer().getPluginManager().getPlugin("Permissions");
		Plugin permex = Bukkit.getServer().getPluginManager().getPlugin("PermissionsEx");
		Plugin bukkitperms = Bukkit.getServer().getPluginManager().getPlugin("PermissionsBukkit");
		Plugin bukkitperms1_1 = Bukkit.getServer().getPluginManager().getPlugin("PermissionsBukkit-1.1");
		RegisteredServiceProvider<Permission> vaultPermissionProvider = null;
		
		try {
			vaultPermissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		} catch (NoClassDefFoundError | Exception ignored) {
		}

        if (vaultPermissionProvider != null) {
        	vault = vaultPermissionProvider.getProvider();
			handler = PermissionsHandler.VAULT;
			Messaging.logInfo("Using Vault for permissions system.", plugin);
			return true;
        } else if (permex != null) {
			permissionPlugin = permex;
			handler = PermissionsHandler.PERMISSIONSEX;
			Messaging.logInfo("Using PermissionsEx for permissions system.", plugin);
			return true;
		} else if (bukkitperms != null) {
			permissionPlugin = bukkitperms;
			handler = PermissionsHandler.PERMISSIONSBUKKIT;
			Messaging.logInfo("Using PermissionsBukkit for permissions system.", plugin);
			return true;
		} else if (bukkitperms1_1 != null) {
			permissionPlugin = bukkitperms1_1;
			handler = PermissionsHandler.PERMISSIONSBUKKIT;
			Messaging.logInfo("Using PermissionsBukkit for permissions system.", plugin);
			return true;
		} else if (perm != null) {
			permissionPlugin = perm;
			handler = PermissionsHandler.PERMISSIONS;
			Messaging.logInfo("Using Permissions for permissions system.", plugin);
			return true;
		} else {
			handler = PermissionsHandler.SUPERPERMS;
			Messaging.logWarning("A permission plugin was not detected! Defaulting to CraftBukkit permissions system.", plugin);
			Messaging.logWarning("Groups disabled. All players defaulting to \"default\" group.", plugin);
			return true;
		}
	}

	public static boolean has(Player player, String permission) {
		boolean blnHasPermission;

		switch (handler) {
			case VAULT:
				blnHasPermission = vault.has(player, permission);
				break;
			case PERMISSIONSEX:
				blnHasPermission = PermissionsEx.getPermissionManager().has(player, permission);
				break;
            // Removed Legacy Permissions Plugin
			case PERMISSIONSBUKKIT:
				blnHasPermission = player.hasPermission(permission);
				break;
			case SUPERPERMS:
				blnHasPermission = player.hasPermission(permission);
				break;
			default:
				blnHasPermission = player.isOp();
				break;
		}

		return blnHasPermission;
	}

	public static String getGroup(Player player) {
		String[] groups;
		
		if (player != null) {
			switch (handler) {
				case VAULT:
					return vault.getPrimaryGroup(player);
					
				case PERMISSIONSEX:
					groups = (String[]) PermissionsEx.getPermissionManager().getUser(player).getParentIdentifiers().toArray();
					
					if (groups.length > 0) {
						return groups[0];
					}
					break;
                // Removed Legacy Permissions Plugin
				case SUPERPERMS:
					break; // Groups not supported.
			}
		}

		return "default";
	}
}
