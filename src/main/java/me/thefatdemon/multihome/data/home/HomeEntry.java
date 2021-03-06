package me.thefatdemon.multihome.data.home;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.UUID;

public class HomeEntry {
	private String uuid = "";
	private String homeName = "";
	private String world = "";
	private double X = 0, Y = 0, Z = 0;
	private float yaw = 0, pitch = 0;

	public HomeEntry() {}
	
	public HomeEntry(UUID uuid, String homeName, String world, double X, double Y, double Z, float pitch, float yaw) {
		this.setOwner(uuid.toString());
		this.setHomeName(homeName);
		this.setWorld(world);
		this.setX(X);
		this.setY(Y);
		this.setZ(Z);
		this.setPitch(pitch);
		this.setYaw(yaw);
	}
	
	public HomeEntry(UUID uuid, String homeName, Location location) {
		this.setOwner(uuid.toString());
		this.setHomeName(homeName);
		this.setWorld(location.getWorld().getName());
		this.setX(location.getX());
		this.setY(location.getY());
		this.setZ(location.getZ());
		this.setPitch(location.getPitch());
		this.setYaw(location.getYaw());
	}

	public void setHomeLocation(String world, double X, double Y, double Z, float pitch, float yaw) {
		this.setWorld(world);
		this.setX(X);
		this.setY(Y);
		this.setZ(Z);
		this.setPitch(pitch);
		this.setYaw(yaw);
	}
	
	public String getOwner() {
        return uuid;
	}

	public void setOwner(String ownerUUID) {
		this.uuid = ownerUUID;
	}

	public String getHomeName() {
		return homeName;
	}
	
	public void setHomeName(String homeName) {
		this.homeName = homeName;
	}

	public Location getHomeLocation(Server server) {
		World world;
		
		try {
			world = server.getWorld(this.world);
			
			if (world != null) {
				return new Location(server.getWorld(this.world), this.X, this.Y, this.Z, this.yaw, this.pitch);
			}
		} catch (Exception ignored) {}
		
		return null;
	}

	public void setHomeLocation(Location location) {
		this.setWorld(location.getWorld().getName());
		this.setX(location.getX());
		this.setY(location.getY());
		this.setZ(location.getZ());
		this.setPitch(location.getPitch());
		this.setYaw(location.getYaw());
	}
	
	public void setWorld(String world) {
		this.world = world;
	}

	public String getWorld() {
		return world;
	}

	public void setX(double X) {
		this.X = X;
	}

	public double getX() {
		return X;
	}

	public void setY(double Y) {
		this.Y = Y;
	}

	public double getY() {
		return Y;
	}

	public void setZ(double Z) {
		this.Z = Z;
	}

	public double getZ() {
		return Z;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getYaw() {
		return yaw;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getPitch() {
		return pitch;
	}
}
