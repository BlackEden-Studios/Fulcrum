package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.cache.PlayerDataLoader;
import com.bestudios.fulcrum.api.cache.PlayerDataSaver;
import com.bestudios.fulcrum.api.cache.SessionCache;
import com.bestudios.fulcrum.api.service.claim.ClaimsService;
import com.bestudios.fulcrum.api.service.team.TeamsService;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.events.LandTrustPlayerEvent;
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.land.LandWorld;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Bridge class integrating the Lands plugin with the Fulcrum API.
 * Implements both ClaimsService and TeamsService interfaces.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ClaimsService
 * @see TeamsService
 */
public class LandsBridge implements ClaimsService, TeamsService {

  public static final String PROVIDER = "Lands";
  /** The plugin instance */
  private final Plugin plugin;
  /** The service priority for this integration */
  private final ServicePriority priority;
  /** The Lands API integration instance */
  private final LandsIntegration api;
  /** The Lands world instance for the main world */
  private final LandWorld world;
  /** Cache for player land data */
  private final SessionCache<Land> landCache;

  /**
   * Constructor for the LandsBridge.
   *
   * @param pluginRef       The plugin instance
   * @param servicePriority The service priority for this integration
   * @throws IllegalStateException if the Lands plugin is not found or not enabled
   */
  public LandsBridge(Plugin pluginRef, ServicePriority servicePriority) {
    // Check if the Lands plugin is found and enabled
    World bukkitWorld = pluginRef.getServer().getWorld("world");
    if (bukkitWorld == null) throw new IllegalStateException("World 'world' not found");
    if (!pluginRef.getServer().getPluginManager().isPluginEnabled(PROVIDER))
      throw new IllegalStateException(PROVIDER + " plugin is not enabled");
    // Initialize instance variables
    this.plugin = pluginRef;
    this.priority = servicePriority;
    this.api = LandsIntegration.of(pluginRef);
    this.world = this.api.getWorld(bukkitWorld);
    if (this.world == null) throw new IllegalStateException(PROVIDER + " is not enabled for world 'world'");
    this.landCache = new SessionCache<>(pluginRef, new LandDataSaver(), new LandDataLoader());
    // Register event listeners to keep the cache updated
    pluginRef.getServer().getPluginManager().registerEvents(new CacheListener(), pluginRef);
  }

  /*
   * ClaimsService methods
   */

  @Override
  public boolean supportsLevels() {
    return true;
  }

  @Override
  public boolean isLocationClaimed(@NotNull Location location) {
    if (world.getLandByChunk(location.getBlockX(), location.getBlockZ()) != null) return true;
    return world.getLandByUnloadedChunk(location.getBlockX(), location.getBlockZ()) != null;
  }

  @Override
  public int getDomainLevel(@NotNull Location homeBlock) {
    if (!supportsLevels()) return 0;
    // Check if the location is in a loaded chunk
    Land land = world.getLandByChunk(homeBlock.getBlockX(), homeBlock.getBlockZ());
    if (land != null) return land.getLevel().getPosition();
    // Check if the location is in an unloaded chunk
    Land unloadedLand = world.getLandByUnloadedChunk(homeBlock.getBlockX(), homeBlock.getBlockZ());
    return unloadedLand != null ? unloadedLand.getLevel().getPosition() : 0;
  }

  @Override
  public boolean canBuildAt(@NotNull Location location, @NotNull UUID playerID) {
    return checkAreaFlag(location, playerID, Flags.BLOCK_PLACE);
  }

  @Override
  public boolean canBreakAt(@NotNull Location location, @NotNull UUID playerID) {
    return checkAreaFlag(location, playerID, Flags.BLOCK_BREAK);
  }

  @Override
  public boolean canPVPAt(@NotNull Location location, @NotNull UUID attackerID, @NotNull UUID targetID) {
    return api.canPvP(Objects.requireNonNull(Bukkit.getPlayer(attackerID)),
                      Objects.requireNonNull(Bukkit.getPlayer(targetID)),
                      location,
                      true,
                      true
    );
  }

  /**
   * Checks if a player has a flag in an area.
   * @param location The location to check.
   * @param playerID The player to check.
   * @param flag     The flag to check for.
   * @return True if the player has the flag, false otherwise.
   */
  private boolean checkAreaFlag(Location location, UUID playerID, RoleFlag flag) {
    Area area = world.getArea(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    if (area == null) return true; // Wilderness
    return area.hasRoleFlag(playerID, flag);
  }

  /*
   * TeamsService methods
   */

  @Override
  public @Nullable String getTeamName(@NotNull UUID teamID) {
    return "";
  }

  @Override
  public @NotNull UUID getUniqueId(@NotNull String teamName) {
    return Objects.requireNonNull(api.getLandByName(teamName)).getOwnerUID();
  }

  @Override
  public boolean addPlayer(@NotNull UUID teamID, @NotNull UUID playerID) {
    return false;
  }

  @Override
  public boolean removePlayer(@NotNull UUID teamID, @NotNull UUID playerID) {
    return false;
  }

  @Override
  public boolean hasPlayer(@NotNull UUID teamID, @NotNull UUID playerID) {
    return false;
  }

  @Override
  public boolean areAllies(@NotNull UUID playerOne, @NotNull UUID playerTwo) {
    var land1 = landCache.get(playerOne);
    var land2 = landCache.get(playerTwo);
    if (land1 == null || land2 == null) return false;
    return landCache.get(playerOne).getULID().equals(landCache.get(playerTwo).getULID());
  }

  /*
   * Service interface methods
   */

  @Override
  public ServicePriority getPriority() {
    return priority;
  }

  @Override
  public String getPluginName() {
    return PROVIDER;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public String getPluginVersion() {
    return Objects.requireNonNull(this.plugin.getServer().getPluginManager().getPlugin(PROVIDER)).getPluginMeta()
                  .getVersion();
  }

  /**
   * Data loader for player land data.
   * Loads the first land owned by the player.
   */
  private final class LandDataLoader implements PlayerDataLoader<Land> {

    @Override
    public @Nullable Land load(UUID playerID) {
      LandPlayer landPlayer = api.getLandPlayer(playerID);
      if (landPlayer.getLands().isEmpty()) return null;
      // For simplicity, return the first land the player owns
      return landPlayer.getLands().iterator().next();
    }
  }

  /**
  * Data saver for player land data.
  * Does nothing.
  */
  private final class LandDataSaver implements PlayerDataSaver<Land> {

    @Override
    public boolean save(Map.Entry<UUID, Land> data) {
      return false;
    }
  }

  /**
   * Listener to update the land cache on player land trust/untrust events.
   */
  private class CacheListener implements Listener {

    // Handler for when a player joins a land
    @EventHandler
    public void onPlayerLandJoin(LandTrustPlayerEvent event) {
      assert event.getLandPlayer() != null;
      landCache.put(event.getLandPlayer().getPlayer().getUniqueId(), event.getLand());
    }

    // Handler for when a player leaves a land
    @EventHandler
    public void onPlayerLandLeave(LandUntrustPlayerEvent event) {
      if (event.getArea() != null) return; // Ignore if the event is for an area, not a land
      assert event.getLandPlayer() != null;
      landCache.remove(event.getLandPlayer().getPlayer().getUniqueId());
    }
  }
}
