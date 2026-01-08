package com.bestudios.fulcrum.api.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A cross-server position wrapper that stores world coordinates independently
 * of Bukkit Location.
 * Designed to hold references to locations from different servers without
 * requiring active World objects.
 *
 * @param plugin    the plugin that created this position
 * @param worldName the name of the world
 * @param x         the X coordinate
 * @param y         the Y coordinate
 * @param z         the Z coordinate
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public record Position(JavaPlugin plugin, String worldName, double x, double y, double z) {

  /**
   * Creates a new Position from an existing Bukkit Location.
   *
   * @param location the Bukkit location to extract coordinates from
   * @return a new Position with the extracted coordinates
   */
  public static @NotNull Position fromBukkitLocation(@NotNull JavaPlugin plugin, @NotNull Location location) {
    if (location.getWorld() == null)
      throw new IllegalArgumentException("Location must have a valid world");

    return new Position(plugin, location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
  }

  /**
   * Converts this Position to a Bukkit Location if the world is available on this
   * server.
   *
   * @return a new Bukkit Location, or null if the world is not loaded on this
   *         server
   */
  public @Nullable Location toBukkitLocation() {
    World world = Bukkit.getWorld(worldName);
    if (world == null)
      return null;

    return new Location(world, x, y, z);
  }

  /**
   * Gets the World object if available on this server.
   *
   * @return the World object, or null if the world is not loaded on this server
   */
  public @Nullable World getWorld() {
    return Bukkit.getWorld(worldName);
  }

  /**
   * Checks if the world is available on this server.
   *
   * @return true if the world exists and is loaded on this server
   */
  public boolean isWorldAvailable() {
    return Bukkit.getWorld(worldName) != null;
  }

  /**
   * Calculates the distance to another Position.
   *
   * @param other the other Position
   * @return the distance, or -1 if the positions are in different worlds
   */
  public double distance(Position other) {
    if (!this.worldName.equals(other.worldName))
      return -1;

    double dx = this.x - other.x;
    double dy = this.y - other.y;
    double dz = this.z - other.z;

    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  /**
   * Calculates the squared distance to another Position.
   * More efficient than distance() when you only need to compare distances.
   *
   * @param other the other Position
   * @return the squared distance, or -1 if the positions are in different worlds
   */
  public double distanceSquared(Position other) {
    if (!this.worldName.equals(other.worldName))
      return -1;

    double dx = this.x - other.x;
    double dy = this.y - other.y;
    double dz = this.z - other.z;

    return dx * dx + dy * dy + dz * dz;
  }

  /**
   * Calculates the 2D distance (ignoring Y coordinate) to another Position.
   *
   * @param other the other Position
   * @return the 2D distance, or -1 if the positions are in different worlds
   */
  public double distance2D(Position other) {
    if (!this.worldName.equals(other.worldName)) {
      return -1;
    }

    double dx = this.x - other.x;
    double dz = this.z - other.z;

    return Math.sqrt(dx * dx + dz * dz);
  }

  /**
   * Creates a new Position with an offset.
   *
   * @param offsetX the x offset
   * @param offsetY the y offset
   * @param offsetZ the z offset
   * @return a new Position with the offset applied
   */
  public Position add(double offsetX, double offsetY, double offsetZ) {
    return new Position(plugin, worldName, x + offsetX, y + offsetY, z + offsetZ);
  }

  /**
   * Creates a new Position with a negative offset.
   *
   * @param offsetX the x offset to subtract
   * @param offsetY the y offset to subtract
   * @param offsetZ the z offset to subtract
   * @return a new Position with the offset subtracted
   */
  public @NotNull Position subtract(double offsetX, double offsetY, double offsetZ) {
    return add(-offsetX, -offsetY, -offsetZ);
  }

  /**
   * Creates a new Position at the center of the current block.
   *
   * @return a new Position with coordinates adjusted to the block center
   */
  public @NotNull Position getBlockCenter() {
    return new Position(plugin, worldName, x + 0.5, y + 0.5, z + 0.5);
  }

  /**
   * Creates a new Position at the specified Y level.
   *
   * @param newY the new Y coordinate
   * @return a new Position with the updated Y coordinate
   */
  public @NotNull Position withY(double newY) {
    return new Position(plugin, worldName, x, newY, z);
  }

  /**
   * Checks if this Position is in the same world as another Position.
   *
   * @param other the other Position
   * @return true if both positions are in the same world
   */
  public boolean isSameWorld(Position other) {
    return this.worldName.equals(other.worldName);
  }

  /**
   * Checks if this Position is in the same world as a Bukkit World.
   *
   * @param world the Bukkit World to compare
   * @return true if this Position is in the same world as the provided World
   */
  public boolean isSameWorld(World world) {
    return this.worldName.equals(world.getName());
  }

  /**
   * Checks if this Position is in the same world as a given world name.
   *
   * @param worldName the name of the world to compare
   * @return true if this Position is in the same world as the provided world name
   */
  public boolean isSameWorld(String worldName) {
    return this.worldName.equals(worldName);
  }

  /**
   * Checks if this Position is in the same world as a Bukkit Location.
   *
   * @param location the Bukkit Location to compare
   * @return true if this Position is in the same world as the provided Location
   */
  public boolean isSameWorld(Location location) {
    return this.worldName.equals(location.getWorld().getName());
  }

  /**
   * Checks if this Position is within a certain distance of another Position.
   *
   * @param other       the other Position
   * @param maxDistance the maximum distance
   * @return true if the positions are within the specified distance
   */
  public boolean isWithinDistance(Position other, double maxDistance) {
    if (!isSameWorld(other)) {
      return false;
    }
    return distanceSquared(other) <= maxDistance * maxDistance;
  }

  /**
   * Checks if this Position is within a cubic region defined by two corners.
   *
   * @param corner1 the first corner of the region
   * @param corner2 the second corner of the region
   * @return true if this position is within the region
   */
  public boolean isWithinRegion(Position corner1, Position corner2) {
    if (!isSameWorld(corner1) || !isSameWorld(corner2)) {
      return false;
    }

    double minX = Math.min(corner1.x, corner2.x);
    double maxX = Math.max(corner1.x, corner2.x);
    double minY = Math.min(corner1.y, corner2.y);
    double maxY = Math.max(corner1.y, corner2.y);
    double minZ = Math.min(corner1.z, corner2.z);
    double maxZ = Math.max(corner1.z, corner2.z);

    return x >= minX && x <= maxX &&
           y >= minY && y <= maxY &&
           z >= minZ && z <= maxZ;
  }

  /**
   * Returns a string representation suitable for configuration storage.
   * Format: "worldName,x,y,z"
   *
   * @return a serializable string representation
   */
  public String serialize() {
    return String.format("%s,%.6f,%.6f,%.6f", worldName, x, y, z);
  }

  /**
   * Creates a Position from a serialized string.
   *
   * @param serialized the serialized string in format "worldName,x,y,z"
   * @return a new Position
   */
  public static @NotNull Position deserialize(@NotNull JavaPlugin plugin, @NotNull String serialized) {
    try {
      // Validate the input string
      if (serialized.trim().isEmpty())
        throw new NumberFormatException();
      // Split the string by commas
      String[] parts = serialized.split(",");
      if (parts.length != 4)
        throw new NumberFormatException();
      // Parse the world name and coordinates
      return new Position(
          plugin,
          parts[0].trim(),
          Double.parseDouble(parts[1].trim()),
          Double.parseDouble(parts[2].trim()),
          Double.parseDouble(parts[3].trim()));
    } catch (NumberFormatException e) {
      plugin.getLogger().warning("Failed to deserialize a Position: defaulting to spawn location");
      return new Position(
          plugin,
          plugin.getServer().getWorlds().getFirst().getName(),
          plugin.getServer().getWorlds().getFirst().getSpawnLocation().getX(),
          plugin.getServer().getWorlds().getFirst().getSpawnLocation().getY(),
          plugin.getServer().getWorlds().getFirst().getSpawnLocation().getZ());
    }
  }

  /**
   * Serializes the Position to a byte array.
   * <p>
   * Format:
   * <ul>
   *   <li>[worldName length (int)]</li>
   *   <li>[worldName (utf)]</li>
   *   <li>[x (double)]</li>
   *   <li>[y (double)]</li>
   *   <li>[z (double)]</li>
   * </ul>
   * </p>
   * @return the serialized byte array
   */
  public byte @NotNull [] toBytes() {
    try (java.io.ByteArrayOutputStream sh = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(sh)) {
      dos.writeUTF(worldName);
      dos.writeDouble(x);
      dos.writeDouble(y);
      dos.writeDouble(z);
      return sh.toByteArray();
    } catch (java.io.IOException e) {
      throw new RuntimeException("Failed to serialize Position", e);
    }
  }

  /**
   * Deserializes a Position from a byte array.
   *
   * @param bytes the byte array containing the serialized Position
   * @return the deserialized Position
   */
  public static @NotNull Position fromBytes(@NotNull JavaPlugin plugin, byte @NotNull [] bytes) {
    if (bytes.length == 0)
      throw new IllegalArgumentException("Byte array cannot be null or empty");

    try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
        java.io.DataInputStream dis = new java.io.DataInputStream(bais)) {
      String worldName = dis.readUTF();
      double x = dis.readDouble();
      double y = dis.readDouble();
      double z = dis.readDouble();
      return new Position(plugin, worldName, x, y, z);
    } catch (java.io.IOException e) {
      throw new RuntimeException("Failed to deserialize Position", e);
    }
  }

  /**
   * Creates a Position from a string format "x y z" in a specified world.
   *
   * @param worldName   the world name
   * @param coordinates the coordinates string in format "x y z"
   * @return a new Position
   * @throws IllegalArgumentException if the coordinate format is invalid
   */
  public static Position fromCoordinateString(@NotNull JavaPlugin plugin, @NotNull String worldName,
      @NotNull String coordinates) {

    if (worldName.trim().isEmpty())
      throw new IllegalArgumentException("World name cannot be null or empty");

    if (coordinates.trim().isEmpty())
      throw new IllegalArgumentException("Coordinates string cannot be null or empty");

    String[] parts = coordinates.trim().split("\\s+");
    if (parts.length != 3)
      throw new IllegalArgumentException("Invalid coordinate format: " + coordinates + " (expected format: x y z)");

    try {
      double x = Double.parseDouble(parts[0]);
      double y = Double.parseDouble(parts[1]);
      double z = Double.parseDouble(parts[2]);

      return new Position(plugin, worldName, x, y, z);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid coordinate values in: " + coordinates, e);
    }
  }
}
