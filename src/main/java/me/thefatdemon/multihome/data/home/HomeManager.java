package me.thefatdemon.multihome.data.home;

import me.thefatdemon.multihome.MultiHome;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Base class for home location database objects.
 * @author MadManMarkAu
 */
public abstract class HomeManager {
	protected final MultiHome plugin;
	
	/**
	 * @param plugin The plug-in.
	 */
	public HomeManager(MultiHome plugin) {
		this.plugin = plugin;
	}

	/**
	 * Deletes all homes from the database.
	 */
	abstract public void clearHomes();

	/**
	 * Returns a HomeEntry object for the specified home. If home is not found, returns null. 
	 * @param player Owner of the home.
	 * @param name Name of the owner's home location.
	 */
	public final HomeEntry getHome(Player player, String name) {
		return this.getHome(player.getUniqueId(), name);
	}

	/**
	 * Returns a HomeEntry object for the specified home. If home is not found, returns null. 
	 * @param uuid UUID of player
	 * @param name Name of the owner's home location.
	 */
	abstract public HomeEntry getHome(UUID uuid, String name);

	/**
	 * Adds the home location for the specified player. If home location already exists, updates the location.
	 * @param player Owner of the home.
	 * @param name Name of the owner's home.
	 * @param location Location the home.
	 */
	public final void addHome(Player player, String name, Location location) {
		this.addHome(player.getUniqueId(), name, location);
	}
	
	/**
	 * Adds the home location for the specified player. If home location already exists, updates the location.
	 * @param uuid UUID of the owner of the home.
	 * @param name Name of the owner's home.
	 * @param location Location the home.
	 */
	abstract public void addHome(UUID uuid, String name, Location location);

	/**
	 * Remove an existing home.
	 * @param player Owner of the home.
	 * @param name Name of the owner's home location.
	 */
	public final void removeHome(Player player, String name) {
		this.removeHome(player.getUniqueId(), name);
	}
	
	/**
	 * Remove an existing home.
	 * @param player Owner of the home.
	 * @param name Name of the owner's home location.
	 */
	abstract public void removeHome(UUID player, String name);
	
	/**
	 * Check the home database for a player.
	 * @param player Player to check database for.
	 * @return boolean True if player exists in database, otherwise false.
	 */
	public final boolean getUserExists(Player player) {
		return this.getUserExists(player.getUniqueId());
	}
	
	/**
	 * Check the home database for a player.
	 * @param player Player to check database for.
	 * @return boolean True if player exists in database, otherwise false.
	 */
	abstract public boolean getUserExists(UUID player);

	/**
	 * Get the number of homes a player has set.
	 * @param player Player to check home list for.
	 * @return int Number of home locations set.
	 */
	public final int getUserHomeCount(Player player) {
		return this.getUserHomeCount(player.getUniqueId());
	}

	/**
	 * Get the number of homes a player has set.
	 * @param player Player to check home list for.
	 * @return int Number of home locations set.
	 */
	abstract public int getUserHomeCount(UUID player);
	
	/**
	 * Retrieve a list of player home locations from the database. If player not found, returns a blank list.
	 * @param player Player to retrieve home list for.
	 * @return ArrayList<HomeEntry> List of home locations.
	 */
	public final ArrayList<HomeEntry> listUserHomes(Player player) {
		return this.listUserHomes(player.getUniqueId());
	}
	
	/**
	 * Retrieve a list of player home locations from the database. If player not found, returns a blank list.
	 * @param player Player to retrieve home list for.
	 * @return ArrayList<HomeEntry> List of home locations.
	 */
	abstract public ArrayList<HomeEntry> listUserHomes(UUID player);
	
	/**
	 * Imports the list of home locations passed. Does not overwrite existing home locations.
	 * @param homes List of players and homes to import.
	 * @param overwrite True to overwrite existing entries.
	 */
	abstract public void importHomes(ArrayList<HomeEntry> homes, boolean overwrite);
}
