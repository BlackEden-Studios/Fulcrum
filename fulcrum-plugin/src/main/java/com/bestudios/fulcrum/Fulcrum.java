package com.bestudios.fulcrum;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.service.database.DatabaseService;
import com.bestudios.fulcrum.api.util.Lock;
import com.bestudios.fulcrum.database.RedisDatabaseGateway;
import com.bestudios.fulcrum.service.FulcrumServicesRegistry;
import com.bestudios.fulcrum.system.MenuListener;
import com.bestudios.fulcrum.system.TimeTracker;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;

public final class Fulcrum extends FulcrumPlugin {

  /** The database gateway */
  private DatabaseService database;
  /** The time tracker */
  private BukkitTask timeTracker;

  /**
   * @return the singleton instance of the plugin
   */
  public static Fulcrum getInstance() {
    return getPlugin(Fulcrum.class);
  }

  @Override
  protected void showPluginTitle() {
    this.getLogger().info(
"""
     \n
                         @@
                         @@
                        @@@
                       @@@@
                     @@@ @@  @@
                   @@@   @@   @@@                   Version: 1.0.0         Paper: 1.21         Author: Bestialus
                 @@@     @@     @@@
               @@@       @@       @@@
             @@@        @@@@        @@@             _____________   ______      _______________  ___   ______   ____
           @@@        @@@  @@@        @@@          /  _______/  /  /  /  /     /  ______/  __  \\/  /  /  /   | /   /
         @@@        @@@      @@@        @@@       /  /      /  /  /  /  /     /  /     /  / /  /  /  /  /    |/   /
        @@@        @@          @@        @@@     /  /___   /  /  /  /  /     /  /     /  /_/  /  /  /  /  /|__/  /
         @@@        @@@      @@@        @@@     /  ____/  /  /  /  /  /     /  /     /  __, _/  /  /  /  /   /  /
           @@@        @@@  @@@        @@@      /  /      /  /__/  /  / ____/  /_____/  / |  |\\  \\_/  /  /   /  /
             @@@        @@@@        @@@       /__/       \\_______/________/\\_______/ _/  |__| \\_____/__/   /__/
               @@@                @@@
                 @@@            @@@                ______                                             __
                   @@@        @@@                 / ____/________ _____ ___  ___ _      ______  _____/ /__
                     @@@    @@@                  / /_  / ___/ __ `/ __ `__ \\/ _ \\ | /| / / __ \\/ ___/ //_/
                       @@@@@@                   / __/ / /  / /_/ / / / / / /  __/ |/ |/ / /_/ / /  / ,<
                         @@                    /_/   /_/   \\__,_/_/ /_/ /_/\\___/|__/|__/\\____/_/  /_/|_|
     \n
     """
    );

  }

  @Override
  protected void registerPluginCommands() {
    // No additional commands to register in the core plugin}
  }

  @Override
  protected void initializationTasks() {
    // Initialize database gateway
    database = new RedisDatabaseGateway(this, ServicePriority.Highest);
    if (!database.initialize(new FulcrumLock(), this.getConfig()))
      this.getLogger().severe("Failed to initialize database gateway!");
    // Initialize service registry
    this.servicesRegistry  = new FulcrumServicesRegistry(this, database);
    this.servicesRegistry.registerServices();
    // Register centralized listeners
    this.getServer().getPluginManager().registerEvents(MenuListener.INSTANCE, this);
    // Register time tracker task
    this.timeTracker = this.getServer().getScheduler()
                                       .runTaskTimer(this, TimeTracker.getInstance(), 20, 20);
  }

  @Override
  protected void terminationTasks() {
    // Terminate database gateway
    if (!this.database.shutdown(new FulcrumLock()))
      this.getLogger().severe("Failed to terminate database gateway!");
    else this.getLogger().info("Database gateway terminated successfully.");

    // Terminate time tracker
    this.timeTracker.cancel();
    // Terminate any services
    this.getServer().getServicesManager().unregisterAll(this);
  }

  /**
   * A lock that validates operations on the services
   */
  public static final class FulcrumLock implements Lock {
    private FulcrumLock() {}

    @Override
    public void lock() {}
  }

}
