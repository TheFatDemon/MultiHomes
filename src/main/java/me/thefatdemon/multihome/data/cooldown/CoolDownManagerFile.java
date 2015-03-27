package me.thefatdemon.multihome.data.cooldown;

import me.thefatdemon.multihome.Messaging;
import me.thefatdemon.multihome.MultiHome;
import me.thefatdemon.multihome.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class CoolDownManagerFile extends CoolDownManager {
    private final File cooldownsFile;
	private HashMap<String, CoolDownEntry> cooldownEntries = new HashMap<String, CoolDownEntry>();

	/**
	 * @param plugin The plug-in.
	 */
	public CoolDownManagerFile(MultiHome plugin) {
		super(plugin);
		this.cooldownsFile = new File(plugin.getDataFolder(), "cooldowns.txt");
		
		loadCooldowns();
	}

	public void clearCooldowns() {
		this.cooldownEntries.clear();

		saveCooldowns();
	}

	public CoolDownEntry getCooldown(UUID player) {
		if (this.cooldownEntries.containsKey(player.toString())) {
			return this.cooldownEntries.get(player.toString());
		}
		
		return null;
	}

	public void addCooldown(CoolDownEntry cooldown) {
		
		if (this.cooldownEntries.containsKey(cooldown.getPlayer().toLowerCase())) {
			this.cooldownEntries.remove(cooldown.getPlayer().toLowerCase());
		}

		// Set new warmup
		this.cooldownEntries.put(cooldown.getPlayer().toLowerCase(), cooldown);

		updateCooldownExpiry();
		saveCooldowns();
	}

	public void removeCooldown(UUID player) {
		if (this.cooldownEntries.containsKey(player.toString())) {
			this.cooldownEntries.remove(player.toString());

			updateCooldownExpiry();
			saveCooldowns();
		}
	}

	/**
	 * Scans through the cooldown list, removing expired cooldowns.
	 */
	private void updateCooldownExpiry() {
		Date now = new Date();

		// Remove expired cooldowns.
		ArrayList<String> removeList = new ArrayList<String>();
		for (Entry<String, CoolDownEntry> entry : this.cooldownEntries.entrySet()) {
			if (entry.getValue().getExpiry().getTime() <= now.getTime()) {
				removeList.add(entry.getKey());
			}
		}
		
		for (String entry : removeList) {
			this.cooldownEntries.remove(entry);
		}
		
		saveCooldowns();
	}

	/**
	 * Saves cooldowns to data folder.
	 */
	private void saveCooldowns() {
		try {
			FileWriter fstream = new FileWriter(this.cooldownsFile);
			BufferedWriter writer = new BufferedWriter(fstream);

			writer.write("# Stores user cooldown times." + Util.newLine());
			writer.write("# <username>;<expiry>" + Util.newLine());
			writer.write(Util.newLine());

			for (Entry<String, CoolDownEntry> entry : this.cooldownEntries.entrySet()) {
				writer.write(entry.getValue().getPlayer() + ";" + Long.toString(entry.getValue().getExpiry().getTime()) + Util.newLine());
			}
			writer.close();
		} catch (Exception e) {
			Messaging.logSevere("Could not write the cooldowns file.", this.plugin);
		}
	}

	/**
	 * Load the cooldown list from file.
	 */
	private void loadCooldowns() {
		Date now = new Date();
		
		try {
			if (this.cooldownsFile.exists()) {
				FileReader fstream = new FileReader(this.cooldownsFile);
				BufferedReader reader = new BufferedReader(fstream);
	
				String line = reader.readLine().trim();
	
				this.cooldownEntries.clear();
	
				while (line != null) {
					if (!line.startsWith("#") && line.length() > 0) {
						String[] values = line.split(";");
	
						try {
							if (values.length == 2) {
								Date expiry = new Date(Long.parseLong(values[1]));
								
								if (expiry.getTime() > now.getTime()) {
									if (!this.cooldownEntries.containsKey(values[0].toLowerCase())) {
										this.cooldownEntries.put(values[0], new CoolDownEntry(values[0], expiry));
									}
								}
							}
						} catch (Exception ignored) {
						}
					}
	
					line = reader.readLine();
				}
	
				reader.close();
			}
		} catch (Exception e) {
			Messaging.logSevere("Could not read the cooldowns file.", this.plugin);
		}
	}
}
