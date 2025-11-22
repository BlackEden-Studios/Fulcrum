package com.bestudios.fulcrum.api.util;

/**
 * Utility class for timers handling in the Fulcrum framework.
 * TimerInfo allows maintaining multiple in-game timers while running a single instantiated timer at run-time.
 * <p></p>
 * This class provides both static time tracking (global elapsed time) and instance-specific
 * timer functionality for objects that need to track durations or expiration times.
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 */
public class TimerInfo {

  /**
   * Global elapsed time counter shared across all timer instances.
   * This value is incremented by the TimerInfoUpdater.
   */
  private static long elapsedTime = 0;

  /**
   * Gets the current global elapsed time.
   *
   * @return The current global elapsed time in seconds
   */
  public static long getElapsedTime() {
    return elapsedTime;
  }

  /**
   * Sets the global elapsed time to a specific value.
   * This method is primarily used by the TimerInfoUpdater.
   *
   * @param elapsedTime The new elapsed time value in seconds
   */
  public static void setElapsedTime(long elapsedTime) {
    TimerInfo.elapsedTime = elapsedTime;
  }

  /**
   * The time at which this timer will expire, relative to the global elapsed time.
   */
  private long expirationTime;

  /**
   * The total duration of this timer in seconds.
   */
  private final long duration;

  /**
   * Creates a new timer with the specified duration.
   * The expiration time is calculated by adding the duration to the current global elapsed time.
   *
   * @param duration The duration of this timer in seconds
   */
  public TimerInfo(long duration) {
    this.expirationTime = duration + elapsedTime;
    this.duration = duration;
  }

  /**
   * Creates a new timer by extending the expiration time of the specified timer.
   * The lasting expiration time is calculated by subtracting the specified timer's expiration time from the current global elapsed time.
   *
   * @param timer The timer to copy the duration from
   */
  public TimerInfo(TimerInfo timer, int duration) {
    this(timer.getExpirationTime() - elapsedTime + duration);
  }

  /**
   * Checks if this timer is still valid (has not expired).
   * A timer is valid if its expiration time is greater than or equal to the current global elapsed time.
   *
   * @return true if the timer is still valid, false if it has expired
   */
  public boolean isValid() {
    return (this.expirationTime >= TimerInfo.elapsedTime);
  }

  /**
   * Updates the remaining time by reducing the expiration time by the specified value.
   * This effectively shortens the remaining duration of the timer.
   *
   * @param value The amount to reduce the expiration time by, in seconds
   */
  public void updateRemainingTime(int value) {
    this.expirationTime = this.expirationTime - value;
  }

  /**
   * Gets the original duration of this timer.
   *
   * @return The original duration in seconds
   */
  public long getDuration() {
    return this.duration;
  }

  /**
   * Gets the absolute expiration time of this timer.
   * This is the globally elapsed time at which the timer will expire.
   *
   * @return The expiration time in seconds
   */
  public long getExpirationTime() {
    return this.expirationTime;
  }

  /**
   * Sets the expiration time to a specific value.
   * This allows manually adjusting when the timer will expire.
   *
   * @param expirationTime The new expiration time in seconds
   */
  public void setExpirationTime(int expirationTime) {
    this.expirationTime = expirationTime;
  }
}
