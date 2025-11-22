package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.cache.PlayerDataLoader;
import com.bestudios.fulcrum.api.cache.SessionCache;
import com.bestudios.fulcrum.api.service.claim.ClaimsService;
import com.bestudios.fulcrum.api.service.team.TeamsService;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.events.LandTrustPlayerEvent;
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.flags.type.Flags;
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
   * @param plugin   The plugin instance
   * @param priority The service priority for this integration
   * @throws IllegalStateException if the Lands plugin is not found or not enabled
   */
  public LandsBridge(Plugin plugin, ServicePriority priority) {
    this.plugin = plugin;
    this.priority = priority;
    World world = plugin.getServer().getWorld("world");
    if (world == null)
      throw new IllegalStateException("World 'world' not found");
    if (!plugin.getServer().getPluginManager().isPluginEnabled("Lands"))
      throw new IllegalStateException("Lands plugin is not enabled");
    this.api = LandsIntegration.of(plugin);
    this.world = this.api.getWorld(world);
    if (this.world == null)
      throw new IllegalStateException("Lands is not enabled for world 'world'");
    // Initialize the land cache
    this.landCache = new SessionCache<>(plugin.getServer().getMaxPlayers(), plugin, new LandDataLoader());
    // Register event listeners to keep the cache updated
    plugin.getServer().getPluginManager().registerEvents(new CacheListener(), plugin);
  }

  @Override
  public ServicePriority getPriority() {
    return priority;
  }

  @Override
  public String getPluginName() {
    return "Lands";
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public String getPluginVersion() {
    return Objects.requireNonNull(this.plugin.getServer().getPluginManager().getPlugin("Lands")).getPluginMeta().getVersion();
  }

  /*
   * ClaimsService methods
   */

  @Override
  public boolean supportsLevels() {
    return true;
  }

  @Override
  public boolean isLocationClaimed(@NotNull final Location location) {
    if (world.getLandByChunk(location.getBlockX(), location.getBlockZ()) != null) return true;
    return world.getLandByUnloadedChunk(location.getBlockX(), location.getBlockZ()) != null;
  }

  @Override
  public int getDomainLevel(@NotNull final Location homeBlock) {
    if (!supportsLevels()) return 0;
    Land land = world.getLandByChunk(homeBlock.getBlockX(), homeBlock.getBlockZ());
    if (land != null) land.getLevel().getPosition();
    Land unloadedLand = world.getLandByUnloadedChunk(homeBlock.getBlockX(), homeBlock.getBlockZ());
    return unloadedLand != null ? unloadedLand.getLevel().getPosition() : 0;
  }

  @Override
  public boolean canBuildAt(@NotNull final Location location, @NotNull final UUID playerID) {
    Area area = world.getArea(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    if (area == null)
      return true; // Wilderness
    return area.hasRoleFlag(playerID, Flags.BLOCK_PLACE);
  }

  @Override
  public boolean canBreakAt(@NotNull final Location location, @NotNull final UUID playerID) {
    Area area = world.getArea(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    if (area == null)
      return true; // Wilderness
    return area.hasRoleFlag(playerID, Flags.BLOCK_BREAK);
  }

  @Override
  public boolean canPVPAt(@NotNull final Location location, @NotNull final UUID attackerID, @NotNull final UUID targetID) {
    return api.canPvP(Objects.requireNonNull(Bukkit.getPlayer(attackerID)),
                      Objects.requireNonNull(Bukkit.getPlayer(targetID)),
                      location,
                      true,
                      true
    );
  }

  /*
   * TeamsService methods
   */
  @Override
  public @Nullable String getTeamName(@NotNull final UUID teamID) {
    return "";
  }

  @Override
  public @NotNull final UUID getUniqueId(@NotNull String teamName) {
    return Objects.requireNonNull(api.getLandByName(teamName)).getOwnerUID();
  }

  @Override
  public boolean addPlayer(@NotNull final UUID teamID, @NotNull final UUID playerID) {
    return false;
  }

  @Override
  public boolean removePlayer(@NotNull final UUID teamID, @NotNull final UUID playerID) {
    return false;
  }

  @Override
  public boolean hasPlayer(@NotNull final UUID teamID, @NotNull final UUID playerID) {
    return false;
  }

  @Override
  public boolean areAllies(@NotNull final UUID playerOne, @NotNull final UUID playerTwo) {
    return landCache.get(playerOne).getULID().equals(landCache.get(playerTwo).getULID());
  }

  /**
   * Data loader for player land data.
   * Loads the first land owned by the player.
   */
  private final class LandDataLoader implements PlayerDataLoader<Land> {

    @Override
    public Land load(UUID playerID) {
      LandPlayer landPlayer = api.getLandPlayer(playerID);
      if (landPlayer.getLands().isEmpty())
        return null;
      else
        // For simplicity, return the first land the player owns
        return landPlayer.getLands().iterator().next();
    }
  }

  /**
   * Listener to update the land cache on player land trust/untrust events.
   */
  public class CacheListener implements Listener {
    // Private constructor to prevent instantiation
    private CacheListener() {}
    // Handler for when a player joins a land
    @EventHandler
    public void onPlayerLandJoin(LandTrustPlayerEvent event) {
      assert event.getLandPlayer() != null;
      landCache.put(event.getLandPlayer().getPlayer().getUniqueId(), event.getLand());
    }
    // Handler for when a player leaves a land
    public void onPlayerLandLeave(LandUntrustPlayerEvent event) {
      if (event.getArea() != null) return; // Ignore if the event is for an area, not a land
      assert event.getLandPlayer() != null;
      landCache.remove(event.getLandPlayer().getPlayer().getUniqueId());
    }
  }
}
