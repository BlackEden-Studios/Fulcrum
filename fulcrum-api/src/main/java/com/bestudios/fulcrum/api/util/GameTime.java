package com.bestudios.fulcrum.api.util;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Utility class for calculating time-related values in Fulcrum plugins.
 * <p>
 * This class provides methods for calculating the current "game day" and
 * determining if a given date is the current "game day".
 * <p>
 * It also provides methods for calculating the number of seconds until the given minute mark or time.
 * @author Bestialus
 * @version 1.0
 * @since   1.0
*/
public class GameTime {

  /** Time when the new day starts (6 AM) */
  private static final LocalTime NEW_DAY_TIME = LocalTime.of(6, 0);
  /** Date format for the plugin */
  private static final String DATE_FORMAT = "yyyy-MM-dd";

  /**
   * Calculates the current "game day" based on 6 AM reset.
   * A new "game day" starts at 6 AM.
   * @return The current "game day" as a LocalDate object
   */
  public static LocalDate getGameDay() { return getGameDay(LocalDateTime.now()); }

  /**
   * Calculates the current "game day" based on the given LocalDateTime.
   * A new "game day" starts at 6 AM.
   * @param now The LocalDateTime to use for calculating the current "game day"
  */
  public static LocalDate getGameDay(@NotNull LocalDateTime now) {
    return now.toLocalTime().isBefore(NEW_DAY_TIME) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
  }

  /**
   * Checks if the given date is the current "game day"
   * A new "game day" starts at 6 AM
   * @return true if the given date matches the current game day
   */
  public static boolean isCurrentDate(@NotNull LocalDate date) {
    return date.equals(GameTime.getGameDay());
  }

  /**
   * Checks if the given date is the current "game day"
   * A new "game day" starts at 6 AM
   * @return true if the given date matches the current game day
   */
  public static boolean isCurrentDate(@NotNull String dateString) {
    try {
      LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
      return isCurrentDate(date);
    } catch (DateTimeParseException e) {
      // If there's any error parsing the date, assume it's not the current day
      return false;
    }
  }

  /**
   * Checks if the given date is in the current week.
   * A week starts on Monday and ends on Sunday.
   * @return true if the given date is in the current week
   */
  public static boolean isCurrentWeek(@NotNull LocalDate date) {
    // Check if the week is the same
    return date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
               .isEqual(GameTime.getGameDay().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
  }

  /**
   * Checks if the given date is in the current week.
   * A week starts on Monday and ends on Sunday.
   * @return true if the given date is in the current week
   */
  public static boolean isCurrentWeek(String dateString) {
    try {
      LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT));
      return isCurrentWeek(date);
    } catch (DateTimeParseException e) {
      // If there's any error parsing the date, assume it's not current week
      return false;
    }
  }

  /**
   * Calculates the time of the next occurrence of a specific minute mark from a given time.
   * For example, if it's currently 9:44:30 and minuteMark is 0, this returns 10:00:00.
   *
   * @param start  The starting time
   * @param minute The target minute mark (0-59). Use 0 for the top of the hour.
   * @return The time of the next occurrence of the specified minute
   * @throws IllegalArgumentException if minuteMark is not between 0 and 59
   */
  public static LocalDateTime timeOfNextMark(@NotNull LocalDateTime start, @NotNull Integer minute) {
    return timeOfNextMark(start, null, minute);
  }

  /**
   * Calculates the time of the next occurrence of a specific minute mark.
   * For example, if it's currently 9:44:30 and minuteMark is 0, this returns 10:00:00.
   *
   * @param minute The target minute mark (0-59). Use 0 for the top of the hour.
   * @return The time of the next occurrence of the specified minute
   * @throws IllegalArgumentException if minuteMark is not between 0 and 59
   */
  public static LocalDateTime timeOfNextMark(@NotNull Integer minute) {
    return timeOfNextMark(LocalDateTime.now(), minute);
  }

  /**
   * Calculates the time of the next occurrence of a specific hour and minute.
   * For example, if you want the next 10:00, this returns 10:00:00.
   *
   * @param hour   The target hour (0-23)
   * @param minute The target minute (0-59)
   * @return The time of the next occurrence of the specified time
   * @throws IllegalArgumentException if hour or minute values are out of range
   */
  public static LocalDateTime timeOfNextMark(Integer hour, Integer minute) {
    return timeOfNextMark(LocalDateTime.now(), hour, minute);
  }

  /**
   * Calculates the time of the next occurrence of a specific hour and minute, from a specific date.
   * For example, if you want the next 10:00, this returns 10:00:00.
   *
   * @param start  The starting day
   * @param hour   The target hour (0-23)
   * @param minute The target minute (0-59)
   * @return The time of the next occurrence of the specified time
   * @throws IllegalArgumentException if hour or minute values are out of range
   */
  public static LocalDateTime timeOfNextMark(
          @NotNull LocalDateTime start,
          Integer hour,
          @NotNull Integer minute
  ) {
    validateHours(hour);
    validateMinutes(minute);

    LocalDateTime candidate = start.truncatedTo(ChronoUnit.MINUTES)
                                   .withMinute(minute);

    if (hour != null) {
      candidate = candidate.withHour(hour);
      if (!candidate.isAfter(start)) return candidate.plusDays(1);
    } else if (!candidate.isAfter(start)) return candidate.plusHours(1);
    return candidate;
  }

  /**
   * Calculates the number of seconds from now until the next occurrence of a specific hour and minute.
   * For example, if it's currently 9:44:30, and you want the next 10:00, this returns the seconds until then.
   *
   * @param hour   The target hour (0-23)
   * @param minute The target minute (0-59)
   * @return The number of seconds until the next occurrence of the specified time
   * @throws IllegalArgumentException if hour or minute values are out of range
   */
  public static long secondsUntilNextMark(LocalDateTime start, Integer hour, Integer minute) {
    return start.until(timeOfNextMark(hour, minute), ChronoUnit.SECONDS);
  }

  /**
   * Calculates the number of seconds from now until the next occurrence of a specific hour and minute.
   * For example, if it's currently 9:44:30, and you want the next 10:00, this returns the seconds until then.
   *
   * @param hour   The target hour (0-23)
   * @param minute The target minute (0-59)
   * @return The number of seconds until the next occurrence of the specified time
   * @throws IllegalArgumentException if hour or minute values are out of range
   */
  public static long secondsUntilNextMark(Integer hour, Integer minute) {
    return secondsUntilNextMark(LocalDateTime.now(), hour, minute);
  }

  /**
   * Calculates the number of seconds from a given time until the next occurrence of a specific minute mark.
   * Useful for testing or calculating delays based on a specific starting time.
   *
   * @param start      The starting time
   * @param minute The target minute mark (0-59)
   * @return The number of seconds until the next occurrence
   * @throws IllegalArgumentException if minuteMark is not between 0 and 59
   */
  public static long secondsUntilNextMark(@NotNull LocalDateTime start, Integer minute) {
    return secondsUntilNextMark(start, null, minute);
  }

  /**
   * Calculates the number of seconds from now until the next occurrence of a specific minute mark.
   * For example, if it's currently 9:44:30 and minuteMark is 0, this returns the seconds until 10:00:00.
   *
   * @param minute The target minute mark (0-59). Use 0 for the top of the hour.
   * @return The number of seconds until the next occurrence of the specified minute
   * @throws IllegalArgumentException if minuteMark is not between 0 and 59
   */
  public static long secondsUntilNextMark(Integer minute) {
    return secondsUntilNextMark(LocalDateTime.now(), null, minute);
  }

  /**
   * Calculates the number of seconds from now until the next interval boundary.
   * For example, with a 15-minute interval, if it's 9:44, this returns seconds until 9:45.
   * If it's 9:46, it returns seconds until 10:00.
   *
   * @param from The starting time
   * @param intervalMinutes The interval size in minutes (e.g., 15 for quarter-hour intervals)
   * @return The number of seconds until the next interval boundary
   * @throws IllegalArgumentException if intervalMinutes is not a valid divisor of 1440
   */
  public static long secondsUntilNextInterval(LocalTime from, int intervalMinutes) {
    validateHours(intervalMinutes);
    // Check if the interval is a divisor of 1440 (the number of minutes in a day)
    if ((24*60) % intervalMinutes != 0)
      throw new IllegalArgumentException(
              "Interval must be a divisor of 1440 (the number of minutes in a day), got: " + intervalMinutes);

    int currentMinute = from.getMinute() + from.getHour() * 60;
    int minutesToNextInterval = intervalMinutes - ( currentMinute % intervalMinutes );

    return minutesToNextInterval * 60L;
  }

  /**
   * Calculates the number of seconds from now until the next interval boundary.
   * For example, with a 15-minute interval, if it's 9:44, this returns seconds until 9:45.
   * If it's 9:46, it returns seconds until 10:00.
   *
   * @param intervalMinutes The interval size in minutes (e.g., 15 for quarter-hour intervals)
   * @return The number of seconds until the next interval boundary
   * @throws IllegalArgumentException if intervalMinutes is not a valid divisor of 1440
   */
  public static long secondsUntilNextInterval(int intervalMinutes) {
    return secondsUntilNextInterval(LocalTime.now(), intervalMinutes);
  }

  /**
   * Converts seconds to Bukkit/Paper scheduler ticks.
   * Bukkit runs at 20 ticks per second (in ideal conditions).
   *
   * @param seconds The number of seconds
   * @return The equivalent number of ticks
   */
  public static long secondsToTicks(long seconds) {
    return seconds * 20L;
  }

  /**
   * Converts ticks to seconds.
   *
   * @param ticks The number of ticks
   * @return The equivalent number of seconds
   */
  public static long ticksToSeconds(long ticks) {
    return ticks / 20L;
  }

  /**
   * Validates the input integer as a minute digit
   * @param minute The input to validate as minute
   * @throws IllegalArgumentException if the input is not a valid minute
   */
  private static void validateMinutes(Integer minute) {
    if (minute == null) return;
    if (minute < 0 || minute > 59)
      throw new IllegalArgumentException("Minute mark must be between 0 and 59, got: " + minute);
  }

  /**
   * Validates the input integer as an hour digit
   * @param hour The input to validate as hour
   * @throws IllegalArgumentException if the input is not a valid hour
   */
  private static void validateHours(Integer hour) {
    if (hour == null) return;
    if (hour < 0 || hour > 23)
      throw new IllegalArgumentException("Hour slice must be between 0 and 23, got: " + hour);
  }
}
