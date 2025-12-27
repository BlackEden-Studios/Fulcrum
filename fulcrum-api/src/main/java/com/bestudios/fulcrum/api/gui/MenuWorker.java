package com.bestudios.fulcrum.api.gui;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A worker responsible for populating a {@link MenuData} object.
 * <br>
 * The worker holds a reference to the plugin that scheduled it (for creating tasks)
 * and the data container it is responsible for filling.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Menu
 * @see MenuData
 */
public interface MenuWorker {

  /**
   * Gets the plugin that owns this worker.
   * Useful for scheduling BukkitRunnables inside the worker logic.
   * @return The plugin instance.
   */
  @NotNull JavaPlugin getPlugin();

  /**
   * Gets the data container this worker is populating.
   * @return The MenuData instance.
   */
  @NotNull MenuData getData();

  /**
   * Starts the population process.
   * <br>
   * When the process is complete, this worker MUST ensure that
   * {@link MenuData#markAsReady()} is called.
   */
  void start();

  /**
   * Performs a data update.
   * <br>
   * This method should force the MenuData to be "Not Ready" (Busy) while the action is running,
   * preventing invalid renders. Once finished, it should mark the data as "Ready" again.
   * @param action The logic to perform the update.
   */
  void update(Runnable action);

}