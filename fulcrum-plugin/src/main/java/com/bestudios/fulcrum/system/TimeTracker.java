package com.bestudios.fulcrum.system;

import com.bestudios.fulcrum.Fulcrum;
import com.bestudios.fulcrum.api.event.TimerUpdateEvent;
import com.bestudios.fulcrum.api.util.TimerInfo;

import java.time.Duration;
import java.time.LocalTime;

/**
 * TimeTracker is responsible for tracking elapsed time and triggering periodic events.
 * This class follows the Singleton pattern and implements Runnable to be executed at scheduled intervals.
 * <p></p>
 * The class calculates the time remaining until the next daily reset (read from the configuration file)
 * and triggers a TimerUpdateEvent when that time is reached.
 */
public class TimeTracker implements Runnable {

  /**
   * The time in seconds until the next daily reset occurs.
   * This value is calculated based on the current time and the configured reset hour.
   */
  private static final long updateTime = Duration.between(LocalTime.now(),
          LocalTime.of(Fulcrum.getInstance().getConfig().getInt("daily_reset_hour", 6), 0, 0)).toSeconds();

  /**
   * Returns the time in seconds until the next scheduled daily reset.
   *
   * @return seconds until the next reset
   */
  public static long getUpdateTime() {
    return updateTime;
  }

    /*
    public static void setUpdateTime(long newTime) { updateTime = newTime; }
     */

  /**
   * Singleton instance of the TimeTracker
   */
  private static final TimeTracker instance = new TimeTracker();

  /**
   * Private constructor to enforce the Singleton pattern.
   */
  private TimeTracker() {
  }

  /**
   * Returns the singleton instance of TimeTracker.
   *
   * @return The single instance of TimeTracker
   */
  public static TimeTracker getInstance() {
    return instance;
  }

  /**
   * Executed at scheduled intervals to update the elapsed time.
   * Increments the elapsed time counter and triggers a TimerUpdateEvent
   * when the elapsed time reaches a multiple of the update time.
   */
  @Override
  public void run() {
    TimerInfo.setElapsedTime((TimerInfo.getElapsedTime() + 1));
    if (TimerInfo.getElapsedTime() % updateTime == 0) {
      Fulcrum.getInstance().getServer().getPluginManager().callEvent(new TimerUpdateEvent());
    }
  }
}