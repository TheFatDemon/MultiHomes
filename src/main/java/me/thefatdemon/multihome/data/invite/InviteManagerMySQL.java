package me.thefatdemon.multihome.data.invite;

import me.thefatdemon.multihome.Messaging;
import me.thefatdemon.multihome.MultiHome;
import me.thefatdemon.multihome.Settings;
import net.madmanmarkau.MultiHome.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class InviteManagerMySQL extends InviteManager {
	private final String url; // Database URL to connect to.
	private final String user; // MySQL user to connect as.
	private final String password; // Password for MySQL user.

	public InviteManagerMySQL(MultiHome plugin) {
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
			e.printStackTrace();
		}
	}

	@Override
	public void clearInvites() {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			statement = connection.prepareStatement("DELETE FROM `invites`;");
			statement.execute();
		} catch (SQLException e) {
			Messaging.logSevere("Failed to clear invites!", this.plugin);
			e.printStackTrace();
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
	public InviteEntry getInvite(UUID owner, String home, UUID target) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			updateInviteExpiry(connection);

			statement = connection.prepareStatement("SELECT * FROM `invites` WHERE LOWER(`source`) = LOWER(?) AND LOWER(`home`) = LOWER(?) AND (`target` = '*' OR LOWER(`target`) = LOWER(?));");
			statement.setString(1, owner.toString());
			statement.setString(2, home);
			statement.setString(3, target.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				try {
					return new InviteEntry(resultSet.getString("source"),
										   resultSet.getString("home"),
										   resultSet.getString("target"),
										   resultSet.getTimestamp("expires"),
										   resultSet.getString("reason"));
				} catch (Exception ex) {}

			}
			
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get invite!", this.plugin);
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
	public void addInvite(UUID owner, String home, UUID target, Date expiry, String reason) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		boolean exists = false;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			statement = connection.prepareStatement("SELECT COUNT(*) FROM `invites` WHERE LOWER(`source`) = LOWER(?) AND LOWER(`home`) = LOWER(?) AND LOWER(`target`) = LOWER(?);");
			statement.setString(1, owner.toString());
			statement.setString(2, home);
			statement.setString(3, target.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				exists = resultSet.getInt(1) > 0;
			}

			if (exists) {
				statement = connection.prepareStatement("UPDATE `invites` SET `source` = ?, `home` = ?, `target` = ?, `expires` = ?, `reason` = ? WHERE LOWER(`source`) = LOWER(?) AND LOWER(`home`) = LOWER(?) AND LOWER(`target`) = LOWER(?)");

				statement.setString(1, owner.toString());
				statement.setString(2, home);
				statement.setString(3, target.toString());
				if (expiry == null) {
					statement.setNull(4, Types.TIMESTAMP);
				} else {
					statement.setTimestamp(4, new Timestamp(expiry.getTime()));
				}
				statement.setString(5, reason);
				statement.setString(6, owner.toString());
				statement.setString(7, home);
				statement.setString(8, target.toString());
				statement.execute();
			} else {
				statement = connection.prepareStatement("INSERT INTO `invites` (`source`, `home`, `target`, `expires`, `reason`) VALUES (?, ?, ?, ?, ?);");

				statement.setString(1, owner.toString());
				statement.setString(2, home);
				statement.setString(3, target.toString());
				if (expiry == null) {
					statement.setNull(4, Types.TIMESTAMP);
				} else {
					statement.setTimestamp(4, new Timestamp(expiry.getTime()));
				}
				statement.setString(5, reason);
				statement.execute();
			}

			updateInviteExpiry(connection);

		} catch (SQLException e) {
			Messaging.logSevere("Failed to add invite!", this.plugin);
			e.printStackTrace();
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

	@Override
	public void removeInvite(UUID owner, String home, UUID target) {
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			statement = connection.prepareStatement("DELETE FROM `invites` WHERE LOWER(`source`) = LOWER(?) AND LOWER(`home`) = LOWER(?) AND LOWER(`target`) = LOWER(?);");
			statement.setString(1, owner.toString());
			statement.setString(2, home);
			statement.setString(3, target.toString());
			statement.execute();

			updateInviteExpiry(connection);

		} catch (SQLException e) {
			Messaging.logSevere("Failed to remove invite!", this.plugin);
			e.printStackTrace();
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

	@Override
	public ArrayList<InviteEntry> listPlayerInvitesToMe(UUID target) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<InviteEntry> output = new ArrayList<InviteEntry> ();

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			updateInviteExpiry(connection);

			statement = connection.prepareStatement("SELECT * FROM `invites` WHERE LOWER(`target`) = LOWER(?);");
			statement.setString(1, target.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				do {
					output.add(new InviteEntry(resultSet.getString("source"),
							   resultSet.getString("home"),
							   resultSet.getString("target"),
							   resultSet.getTimestamp("expires"),
							   resultSet.getString("reason")));
				} while (resultSet.next());
			}
			
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get all invites to player!", this.plugin);
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

		return output;
	}

	@Override
	public ArrayList<InviteEntry> listPlayerInvitesToOthers(UUID owner) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		ArrayList<InviteEntry> output = new ArrayList<InviteEntry> ();

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			updateInviteExpiry(connection);

			statement = connection.prepareStatement("SELECT * FROM `invites` WHERE LOWER(`source`) = LOWER(?);");
			statement.setString(1, owner.toString());
			resultSet = statement.executeQuery();
			if (resultSet.first()) {
				do {
					output.add(new InviteEntry(resultSet.getString("source"),
							   resultSet.getString("home"),
							   resultSet.getString("target"),
							   resultSet.getTimestamp("expires"),
							   resultSet.getString("reason")));
				} while (resultSet.next());
			}
			
		} catch (SQLException e) {
			Messaging.logSevere("Failed to get all invites from player!", this.plugin);
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

		return output;
	}

	@Override
	public void importInvites(ArrayList<InviteEntry> invites, boolean overwrite) {
		Connection connection = null;
		PreparedStatement statementExists = null;
		PreparedStatement statementInsert = null;
		PreparedStatement statementUpdate = null;
		ResultSet resultSet = null;
		boolean recordExists;

		try {
			connection = DriverManager.getConnection(this.url, this.user, this.password);
			if (!connection.isValid(100)) {
				throw new SQLException();
			}

			updateInviteExpiry(connection);

			statementExists = connection.prepareStatement("SELECT COUNT(*) FROM `invites` WHERE LOWER(`source`) = LOWER(?) AND LOWER(`home`) = LOWER(?) AND LOWER(`target`) = LOWER(?);");
			statementInsert = connection.prepareStatement("INSERT INTO `invites` (`source`, `home`, `target`, `expires`, `reason`) VALUES (?, ?, ?, ?, ?)");
			statementUpdate = connection.prepareStatement("UPDATE `invites` SET `source` = ?, `home` = ?, `target` = ?, `expires` = ?, `reason` = ? WHERE LOWER(`source`) = LOWER(?) AND LOWER(`home`) = LOWER(?) AND LOWER(`target`) = LOWER(?);");
		
			for (InviteEntry thisEntry : invites) {
				// Determine if entry exists.
				recordExists = false;
				statementExists.setString(1, thisEntry.getInviteSource());
				statementExists.setString(2, thisEntry.getInviteHome());
				statementExists.setString(3, thisEntry.getInviteTarget());
				resultSet = statementExists.executeQuery();
				if (resultSet.first()) {
					recordExists = resultSet.getInt(1) > 0;
				}
				resultSet.close();
				resultSet = null;
				
				// Save the entry, if required.
				if (recordExists) {
					if (overwrite) {
						statementInsert.setString(1, thisEntry.getInviteSource());
						statementInsert.setString(2, thisEntry.getInviteHome());
						statementInsert.setString(3, thisEntry.getInviteTarget());
						if (thisEntry.getInviteExpires() == null) {
							statementInsert.setNull(4, Types.TIMESTAMP);
						} else {
							statementInsert.setTimestamp(4, new Timestamp(thisEntry.getInviteExpires().getTime()));
						}
						statementInsert.setString(5, thisEntry.getInviteReason());
						statementInsert.setString(6, thisEntry.getInviteSource());
						statementInsert.setString(7, thisEntry.getInviteHome());
						statementInsert.setString(8, thisEntry.getInviteTarget());
						statementUpdate.execute();
					}
				} else {
					statementInsert.setString(1, thisEntry.getInviteSource());
					statementInsert.setString(2, thisEntry.getInviteHome());
					statementInsert.setString(3, thisEntry.getInviteTarget());
					if (thisEntry.getInviteExpires() == null) {
						statementInsert.setNull(4, Types.TIMESTAMP);
					} else {
						statementInsert.setTimestamp(4, new Timestamp(thisEntry.getInviteExpires().getTime()));
					}
					statementInsert.setString(5, thisEntry.getInviteReason());
					statementInsert.execute();
				}
			}

			updateInviteExpiry(connection);

		} catch (SQLException e) {
			Messaging.logSevere("Failed to import invites!", this.plugin);
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {} // Eat errors
			}

			if (statementExists != null) {
				try {
					statementExists.close();
				} catch (SQLException ex) {} // Eat errors
			}

			if (statementInsert != null) {
				try {
					statementInsert.close();
				} catch (SQLException ex) {} // Eat errors
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {} // Eat errors
			}
		}
	}


	/**
	 * Scans through the invites list, removing expired invites.
	 */
	private void updateInviteExpiry(Connection connection) {
		Date now = new Date();
		PreparedStatement statement = null;

		try {
			statement = connection.prepareStatement("DELETE FROM `invites` WHERE `expires` < ?;");
			statement.setTimestamp(1, new Timestamp(now.getTime()));
			statement.execute();
		} catch (SQLException e) {
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {} // Eat errors
			}
		}
	}
}
