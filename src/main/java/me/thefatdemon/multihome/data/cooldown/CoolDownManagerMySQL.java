package me.thefatdemon.multihome.data.cooldown;

import me.thefatdemon.multihome.Messaging;
import me.thefatdemon.multihome.MultiHome;
import me.thefatdemon.multihome.Settings;

import java.sql.*;
import java.util.Date;
import java.util.UUID;

public class CoolDownManagerMySQL extends CoolDownManager {
	private final String url; // Database URL to connect to.
	private final String user; // MySQL user to connect as.
	private final String password; // Password for MySQL user.

	public CoolDownManagerMySQL(MultiHome plugin) {
		super(plugin);

		// Save settings
		this.url = Settings.getDataStoreSettingString("sql", "url");
		this.user = Settings.getDataStoreSettingString("sql", "user");
		this.password = Settings.getDataStoreSettingString("sql", "pass");

		// Test connection
		try {
			Connection connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			} else {
				connection.close();
			}
		} catch (SQLException e) {
			Messaging.logSevere("Failed to contact MySQL server!", this.plugin);
			return;
		}
	}

	@Override
	public void clearCooldowns() {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			// Clear warmups in database
			statement = connection.prepareStatement("DELETE FROM `cooldowns`;");
			statement.execute();
		} catch (Exception e) {
			Messaging.logSevere("Failed to clear cooldowns!", this.plugin);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {} // Eat errors
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {} // Eat errors
			}
		}
	}

	@Override
	public CoolDownEntry getCooldown(UUID player) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			updateCooldownExpiry(connection);

			statement = connection.prepareStatement("SELECT * FROM `cooldowns` WHERE LOWER(`player`) = LOWER(?);");
			statement.setString(1, player.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				try {
					return new CoolDownEntry(resultSet.getString("player"), resultSet.getTimestamp("expiry"));
				} catch (Exception ignored) {}

			}
			
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get cooldown!", this.plugin);
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ignored) {} // Eat errors
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) {} // Eat errors
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {} // Eat errors
			}
		}

		return null;
	}

	@Override
	public void addCooldown(CoolDownEntry cooldown) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			// Remove cooldown from database
			statement = connection.prepareStatement("DELETE FROM `cooldowns` WHERE LOWER(`player`) = LOWER(?);");
			statement.setString(1, cooldown.getPlayer());
			statement.execute();
			statement.close();
			statement = null;

			// Insert cooldown into database
			statement = connection.prepareStatement("INSERT INTO `cooldowns` (`player`, `expiry`) VALUES (?, ?);");
			statement.setString(1, cooldown.getPlayer());
			statement.setTimestamp(2, new Timestamp(cooldown.getExpiry().getTime()));
			statement.execute();
			statement.close();
			statement = null;

			updateCooldownExpiry(connection);
		} catch (Exception e) {
			Messaging.logSevere("Failed to set cooldown!", this.plugin);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {} // Eat errors
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {} // Eat errors
			}
		}
	}

	@Override
	public void removeCooldown(UUID player) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			updateCooldownExpiry(connection);
			
			// Remove cooldown from database
			statement = connection.prepareStatement("DELETE FROM `cooldowns` WHERE LOWER(`player`) = LOWER(?);");
			statement.setString(1, player.toString());
			statement.execute();
			statement.close();
			statement = null;
		} catch (Exception e) {
			Messaging.logSevere("Failed to remove cooldown!", this.plugin);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) {} // Eat errors
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ignored) {} // Eat errors
			}
		}
	}

	private void updateCooldownExpiry(Connection connection) {
		Date now = new Date();
		PreparedStatement statement = null;

		try {
			statement = connection.prepareStatement("DELETE FROM `cooldowns` WHERE `expiry` < ?;");
			statement.setTimestamp(1, new Timestamp(now.getTime()));
			statement.execute();
		} catch (SQLException ignored) {
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {} // Eat errors
			}
		}
	}

}
