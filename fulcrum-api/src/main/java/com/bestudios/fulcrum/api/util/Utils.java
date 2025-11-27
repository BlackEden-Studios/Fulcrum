package com.bestudios.fulcrum.api.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for common tasks in Fulcrum plugins.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public final class Utils {

  public static String capitalizeFirst(String str) {
    if (str == null || str.isEmpty()) return str;
    return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1);
  }

  /**
   * Compares two version strings.
   *
   * @param v1 the older version string
   * @param v2 the newer version string
   * @return the result of the comparison. <p> "0" if equal,<p>"-1" if v1 &lt; v2,<p>"1" if v1 &gt; v2
   */
  public static int compareVersions(@NotNull String v1, @NotNull String v2) {
    Objects.requireNonNull(v1, "Version string cannot be null");
    Objects.requireNonNull(v2, "Version string cannot be null");

    // Check for equality
    if (v1.equals(v2)) return 0;

    // Split the string
    final String[] parts1 = v1.split("\\.");
    final String[] parts2 = v2.split("\\.");

    // Determine the maximum length to iterate (e.g., comparing "1.2" vs. "1.2.1")
    final int length = Math.max(parts1.length, parts2.length);

    // Iterate
    for (int i = 0; i < length; i++) {
      final int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
      final int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

      // Compare the current parts
      final int result = Integer.compare(p1, p2);
      if (result != 0) {
        return result;
      }
    }

    // If we finished the loop without returning, they are equal
    return 0;
  }

  /**
   * Loads a YAML configuration from within the plugin JAR resources.
   *
   * @param resourcePath The path to the resource relative to the root of the plugin package.
   * @return The {@link YamlConfiguration} loaded from the resource, or {@code null} if not found.
   */
  public static @NotNull YamlConfiguration loadFromResources(Plugin plugin, String resourcePath) {
    try (InputStream stream = plugin.getResource(resourcePath)) {
      // If the resource is not found, return an empty configuration.
      if (stream == null) return new YamlConfiguration();
      try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
        // Load the configuration
        return YamlConfiguration.loadConfiguration(reader);
      } catch (IOException e) {
        plugin.getLogger().warning("Failed to close reader stream: " + e.getMessage());
      }
    } catch (IOException e) {
      plugin.getLogger().warning("Failed to close input stream: " + e.getMessage());
    }
    return new YamlConfiguration();

  }

  /**
   * Returns a message indicating that the specified object is required to be non-empty.
   * @param objName The name of the object to be checked.
   * @return A message indicating that the specified object is required to be non-empty.
   */
  public static @NotNull String messageRequireNonEmpty(String objName) {
    return "The " + objName + " cannot be empty";
  }

  /**
   * Returns a message indicating that the specified object is required to be non-null.
   * @param objName The name of the object to be checked.
   * @return A message indicating that the specified object is required to be non-null.
   */
  public static @NotNull String messageRequireNonNull(String objName) {
    return "The " + objName + " cannot be null";
  }

  /** Private Constructor */
  private Utils() { }

}
