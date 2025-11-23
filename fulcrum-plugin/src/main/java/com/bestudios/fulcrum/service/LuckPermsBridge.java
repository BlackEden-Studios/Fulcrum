package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.service.permission.PermissionsService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * LuckPerms Permission Service Bridge.
 * <p>
 * Implements the PermissionsService interface to provide permission checks and modifications
 * using the LuckPerms API.
 * <p>
 * All methods are executed asynchronously to prevent blocking the main server thread.
 * <p>
 * Make sure to handle the returned CompletableFutures appropriately.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see PermissionsService
 */
public class LuckPermsBridge implements PermissionsService {

  /** The plugin instance */
  private final Plugin plugin;
  /** The service priority */
  private final ServicePriority priority;
  /** The LuckPerms API instance */
  private final LuckPerms api;
  /** The name of the permission provider */
  private static final String PROVIDER = "LuckPerms";

  /**
   * Constructor for LuckPermsBridge.
   *
   * @param plugin   The plugin instance
   * @param priority The service priority
   */
  public LuckPermsBridge(Plugin plugin, ServicePriority priority) {
    this.plugin = plugin;
    this.priority = priority;
    var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    this.api = (provider != null) ? provider.getProvider() : null;
  }

  /**
   * Helper method to perform an action with a loaded User.
   *
   * @param uniqueId The UUID of the user
   * @param action   The action to perform with the User
   * @return A CompletableFuture containing the result of the action
   */
  private CompletableFuture<Boolean> with(@NotNull final UUID uniqueId, @NotNull final Function<User, Boolean> action) {
    return CompletableFuture.supplyAsync(() -> {
      // If the user is not loaded, load it async
      if (!api.getUserManager().isLoaded(uniqueId))
        api.getUserManager().loadUser(uniqueId).join();
      User user = api.getUserManager().getUser(uniqueId);
      if (user == null) return false;
      // Return the action result when done
      return action.apply(user);
    });
  }

  @Override
  public CompletableFuture<Boolean> has(@NotNull final UUID playerID, @NotNull final String permission) {
    return with(playerID, user -> user.getCachedData()
                                            .getPermissionData()
                                            .checkPermission(permission)
                                            .asBoolean());
  }

  @Override
  public CompletableFuture<Boolean> has(@NotNull final Player player, @NotNull final String permission) {
    return has(player.getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> has(@NotNull final String playerName, @NotNull final String permission) {
    return has(Bukkit.getOfflinePlayer(playerName).getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> has(@NotNull final OfflinePlayer player, @NotNull final String permission) {
    return has(player.getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> add(@NotNull UUID playerID, @NotNull String permission) {
    return with(playerID, user -> {
      user.data().add(PermissionNode.builder(permission).build());
      api.getUserManager().saveUser(user);
      return true;
    });
  }

  @Override
  public CompletableFuture<Boolean> add(@NotNull Player player, @NotNull String permission) {
    return add(player.getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> add(@NotNull String playerName, @NotNull String permission) {
    return add(Bukkit.getOfflinePlayer(playerName).getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> add(@NotNull OfflinePlayer player, @NotNull String permission) {
    return add(player.getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> remove(@NotNull UUID playerID, @NotNull String permission) {
    return with(playerID, user -> {
      user.data().remove(PermissionNode.builder(permission).build());
      api.getUserManager().saveUser(user);
      return true;
    });
  }

  @Override
  public CompletableFuture<Boolean> remove(@NotNull Player player, @NotNull String permission) {
    return remove(player.getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> remove(@NotNull String playerName, @NotNull String permission) {
    return remove(Bukkit.getOfflinePlayer(playerName).getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> remove(@NotNull OfflinePlayer player, @NotNull String permission) {
    return remove(player.getUniqueId(), permission);
  }

  @Override
  public CompletableFuture<Boolean> is(@NotNull UUID playerID, @NotNull String groupName) {
    return with(playerID, user -> user.getCachedData().getPermissionData().checkPermission("group." + groupName).asBoolean());
  }

  @Override
  public CompletableFuture<Boolean> is(@NotNull Player player, @NotNull String groupName) {
    return is(player.getUniqueId(), groupName);
  }

  @Override
  public CompletableFuture<Boolean> is(@NotNull String playerName, @NotNull String groupName) {
    return is(Bukkit.getOfflinePlayer(playerName).getUniqueId(), groupName);
  }

  @Override
  public CompletableFuture<Boolean> is(@NotNull OfflinePlayer player, @NotNull String groupName) {
    return is(player.getUniqueId(), groupName);
  }

  @Override
  public ServicePriority getPriority() {
    return this.priority;
  }

  @Override
  public String getPluginName() {
    return this.PROVIDER;
  }

  @Override
  public boolean isAvailable() {
    return this.api != null;
  }

  @Override
  public String getPluginVersion() {
    return this.plugin.getServer().getPluginManager().getPlugin(this.PROVIDER).getDescription().getVersion();
  }
}
