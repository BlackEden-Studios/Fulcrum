package com.bestudios.fulcrum.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Unit tests for the GameTime utility class.
 * Tests verify the correct calculation of time intervals for scheduling tasks.
 */
class GameTimeTest {

  // Fixed reference date for testing time calculations
  private static final LocalDate REF_DATE = LocalDate.of(2025, 1, 1);

  /*
   * Tests for the game day calculations
   */
  @Test
  void testGameDay_CorrectDay() {
    LocalDate day = GameTime.getGameDay(LocalDateTime.of(2025, 10, 10, 10, 0));
    Assertions.assertEquals(LocalDate.of(2025,10,10), day, "Should calculate the game day as 10/10/2025");
  }

  @Test
  void testGameDay_CorrectDay_LeapYear() {
    LocalDate day = GameTime.getGameDay(LocalDateTime.of(2024, 2, 29, 10, 0));
    Assertions.assertEquals(LocalDate.of(2024,2,29), day, "Should calculate the game day as 2/29/2024");
  }

  @Test
  void testGameDay_BeforeDailyReset() {
    LocalDate day = GameTime.getGameDay(LocalDateTime.of(2025, 1, 1, 4, 0));
    Assertions.assertEquals(LocalDate.of(2024,12,31), day, "Should calculate the game day as 31/12/2024");
  }

  /*
   * Tests for the localTimeOfNextMinuteMark method
   */
  @Test
  void testTimeOfNextMark_CurrentlyBeforeTarget() {
    // If the current time is 9:44:30, next :55 should be 09:55:00
    LocalDateTime testTime = REF_DATE.atTime(9, 44, 30);
    LocalDateTime expectedTime = REF_DATE.atTime(9, 55, 0);

    Assertions.assertEquals(expectedTime, GameTime.timeOfNextMark(testTime, 55),
            "Should calculate 9:55:00 from 9:44:30");
  }

  @Test
  void testTimeOfNextMark_CurrentlyAfterTarget() {
    // If current time is 9:44:30, next :30 should be 10:30:00 (Next Hour)
    LocalDateTime testTime = REF_DATE.atTime(9, 44, 30);
    LocalDateTime expectedTime = REF_DATE.atTime(10, 30, 0);

    Assertions.assertEquals(expectedTime, GameTime.timeOfNextMark(testTime, 30),
            "Should calculate 10:30:00 from 9:44:30");
  }

  @Test
  void testTimeOfNextMark_ExactlyOnTarget() {
    // If current time is exactly 10:00:00, next :00 should be 11:00:00 (Next occurrence)
    LocalDateTime testTime = REF_DATE.atTime(10, 0, 0);
    LocalDateTime expectedTime = REF_DATE.atTime(11, 0, 0);

    Assertions.assertEquals(expectedTime, GameTime.timeOfNextMark(testTime, 0),
            "Should calculate 11:00:00 when already at target time");
  }

  /*
   * Tests for the localTimeOfNextOccurrence method
   */
  @Test
  void testTimeOfNextMark_CorrectInterval() {
    // This test uses the static method that relies on LocalDateTime.now(),
    // so we can only check the Time component logic unless we mock the clock.
    // However, the method signature (Integer, Integer) implies a target hour/minute.

    // We can test the overloaded method that takes 'start' to be safe and deterministic:
    LocalDateTime start = REF_DATE.atTime(9, 44, 0);
    LocalDateTime expected = REF_DATE.atTime(10, 0, 0);

    Assertions.assertEquals(expected, GameTime.timeOfNextMark(start, 10, 0),
            "Should calculate 10:00:00 from 9:44:00");
  }

  @Test
  void testTimeOfNextMark_WrongHoursInput() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameTime.timeOfNextMark(24, 0),
            "Should throw IllegalArgumentException for hour >= 24");
  }

  @Test
  void testTimeOfNextMark_WrongMinutesInput() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameTime.timeOfNextMark(10, -5),
            "Should throw IllegalArgumentException for invalid minute in time slice");
  }

  /*
   * Tests for the secondsUntilNextOccurrence method
   */
  @Test
  void testSecondsUntilNextMark_CorrectInterval() {
    LocalDateTime testTime = REF_DATE.atTime(9, 0, 0);
    // Target 10:00:00 -> 1 hour
    long expectedSeconds = 3600;
    Assertions.assertEquals(expectedSeconds, GameTime.secondsUntilNextMark(testTime, 10, 0),
            "Should calculate 3600 seconds from 9:00:00");
  }

  @Test
  void testSecondsUntilNextMark_NearMidnight() {
    LocalDateTime testTime = REF_DATE.atTime(23, 0, 0);
    // Target 00:01:00 (Next Day) -> 1 hour and 1 minute
    long expectedSeconds = 3660;
    Assertions.assertEquals(expectedSeconds, GameTime.secondsUntilNextMark(testTime, 0, 1),
            "Should calculate 3660 seconds from 23:00:00");
  }

  @Test
  void testSecondsUntilNextMark_CloseMidnight() {
    LocalDateTime testTime = REF_DATE.atTime(23, 59, 59);
    // Target 00:00:00 (Next Day) -> 1 second
    long expectedSeconds = 1;
    Assertions.assertEquals(expectedSeconds, GameTime.secondsUntilNextMark(testTime, 0, 0),
            "Should calculate 1 seconds from 23:59:59");
  }

  @Test
  void testSecondsUntilNextMark_InvalidHour_Negative() {
    Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextMark(-1, 0),
            "Should throw IllegalArgumentException for negative hour"
    );
  }

  @Test
  void testSecondsUntilNextMark_InvalidHour_TooLarge() {
    Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextMark(24, 0),
            "Should throw IllegalArgumentException for hour >= 24"
    );
  }

  @Test
  void testSecondsUntilNextMark_InvalidMinute_Negative() {
    Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextMark(10, -5),
            "Should throw IllegalArgumentException for invalid minute in time slice"
    );
  }

  /*
   * Tests for the secondsUntilNextMinuteMark method
   */
  @Test
  void testSecondsUntilNextMark_CurrentlyBeforeTarget_MinuteOnly() {
    // If the current time is 9:44:30, next :55 should be 10 mins 30s
    LocalDateTime testTime = REF_DATE.atTime(9, 44, 30);
    long seconds = GameTime.secondsUntilNextMark(testTime, 55);
    Assertions.assertEquals(630, seconds, "Should calculate 630 seconds from 9:44:30 to 09:55:00");
  }

  @Test
  void testSecondsUntilNextMark_CurrentlyAfterTarget_MinuteOnly() {
    // If current time is 9:44:30, next :30 should be 45m 30s
    LocalDateTime testTime = REF_DATE.atTime(9, 44, 30);
    long seconds = GameTime.secondsUntilNextMark(testTime, 30);
    Assertions.assertEquals(2730, seconds, "Should calculate 2730 seconds from 9:44:30 to 10:30:00");
  }

  @Test
  void testSecondsUntilNextMark_ExactlyOnTarget_MinuteOnly() {
    // If current time is exactly 10:00:00, next :00 should be 1 hour
    LocalDateTime testTime = REF_DATE.atTime(10, 0, 0);
    long seconds = GameTime.secondsUntilNextMark(testTime, 0);
    Assertions.assertEquals(3600, seconds, "Should calculate 3600 seconds when already at target time");
  }

  @Test
  void testSecondsUntilNextMinuteMark_WithSecondsMark() {
    // If current time is 14:23:45, next :30 should be 6m 15s
    LocalDateTime testTime = REF_DATE.atTime(14, 23, 45);
    long seconds = GameTime.secondsUntilNextMark(testTime, 30);
    Assertions.assertEquals(375, seconds, "Should calculate 375 seconds from 14:23:45 to 14:30:00");
  }

  @Test
  void testSecondsUntilNextMark_NearMidnight_MinuteOnly() {
    // If current time is 23:55:00, next :00 should be 5 minutes
    LocalDateTime testTime = REF_DATE.atTime(23, 55, 0);
    long seconds = GameTime.secondsUntilNextMark(testTime, 0);
    Assertions.assertEquals(300, seconds, "Should calculate 300 seconds from 23:55:00 to midnight");
  }


  @Test
  void testBoundaryCondition_Midnight() {
    // Test exact midnight boundary
    LocalDateTime midnight = REF_DATE.atStartOfDay(); // 00:00:00
    long seconds = GameTime.secondsUntilNextMark(midnight, 0);

    Assertions.assertEquals(3600, seconds,
            "At midnight (:00), next :00 should be 1 hour away");
  }

  @Test
  void testBoundaryCondition_LastSecondBeforeTarget() {
    LocalDateTime testTime = REF_DATE.atTime(9, 59, 59);
    long seconds = GameTime.secondsUntilNextMark(testTime, 0);

    Assertions.assertEquals(1, seconds,
            "One second before target should return 1 second");
  }

  /*
   * Tests for the secondsUntilNextInterval method
   */
  @Test
  void testSecondsUntilNextInterval_15Minutes() {
    // 9:44:00 -> next 15m is 9:45:00 (60s)
    LocalDateTime testTime = REF_DATE.atTime(9, 44, 0);
    int nextInterval = 15;
    long expectedSeconds = GameTime.secondsUntilNextInterval(testTime.toLocalTime(), nextInterval);
    Assertions.assertEquals(60, expectedSeconds, "Should calculate 60 seconds to next 15-minute interval");
  }

  @Test
  void testSecondsUntilNextInterval_OnInterval() {
    // 10:00:00 -> next 15m is 10:15:00 (900s)
    LocalDateTime testTime = REF_DATE.atTime(10, 0, 0);
    int nextInterval = 15;
    long expectedSeconds = GameTime.secondsUntilNextInterval(testTime.toLocalTime(), nextInterval);
    Assertions.assertEquals(900, expectedSeconds, "Should calculate 900 seconds to next interval when on interval boundary");
  }

  @Test
  void testSecondsUntilNextInterval_30Minutes() {
    // 10:25:00 -> next 30m is 10:30:00 (300s)
    LocalDateTime testTime = REF_DATE.atTime(10, 25, 0);
    int nextInterval = 30;
    long expectedSeconds = GameTime.secondsUntilNextInterval(testTime.toLocalTime(), nextInterval);
    Assertions.assertEquals(300, expectedSeconds, "Should calculate 300 seconds to next 30-minute interval");
  }

  @Test
  void testSecondsUntilNextInterval_CrossingHourBoundary() {
    // 10:55:00 -> next 60m is 11:00:00 (300s)
    LocalDateTime testTime = REF_DATE.atTime(10, 55, 0);
    int nextInterval = 60;
    long expectedSeconds = GameTime.secondsUntilNextInterval(testTime.toLocalTime(), nextInterval);
    Assertions.assertEquals(300, expectedSeconds, "Should calculate 300 seconds when crossing hour boundary");
  }

  @Test
  void testSecondsUntilNextInterval_CrossingMidnight() {
    // 23:55:00 -> next 60m is 00:00:00 (300s)
    // Note: secondsUntilNextInterval uses LocalTime logic, so it wraps around day
    LocalDateTime testTime = REF_DATE.atTime(23, 55, 0);
    int nextInterval = 60;
    long expectedSeconds = GameTime.secondsUntilNextInterval(testTime.toLocalTime(), nextInterval);
    Assertions.assertEquals(300, expectedSeconds, "Should calculate 300 seconds when crossing midnight");
  }

  @Test
  void testSecondsUntilNextInterval_InvalidInterval() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextInterval(LocalTime.now(), 61),
            "Should throw IllegalArgumentException for interval = 61");
  }

  @Test
  void testSecondsUntilNextInterval_InvalidInterval_Negative() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextInterval(LocalTime.now(), -1),
            "Should throw IllegalArgumentException for interval < 1");
  }

  @Test
  void testSecondsUntilNextInterval_InvalidInterval_Zero() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextInterval(LocalTime.now(), 0),
            "Should throw IllegalArgumentException for interval = 0");
  }

  @Test
  void testSecondsUntilNextInterval_InvalidInterval_TooLarge() {
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextInterval(LocalTime.now(), 1500),
            "Should throw IllegalArgumentException for interval > 24");
  }

  /*
   * Tests for the secondsToTicks and ticksToSeconds methods
   */

  @Test
  void testSecondsToTicks_OneSecond() {
    long ticks = GameTime.secondsToTicks(1);
    Assertions.assertEquals(20L, ticks, "One second should equal 20 ticks");
  }

  @Test
  void testSecondsToTicks_TenSeconds() {
    long ticks = GameTime.secondsToTicks(10);
    Assertions.assertEquals(200L, ticks, "Ten seconds should equal 200 ticks");
  }

  @Test
  void testSecondsToTicks_OneHour() {
    long ticks = GameTime.secondsToTicks(3600);
    Assertions.assertEquals(72000L, ticks, "One hour (3600 seconds) should equal 72000 ticks");
  }

  @Test
  void testSecondsToTicks_Zero() {
    long ticks = GameTime.secondsToTicks(0);
    Assertions.assertEquals(0L, ticks, "Zero seconds should equal zero ticks");
  }

  @Test
  void testTicksToSeconds_TwentyTicks() {
    long seconds = GameTime.ticksToSeconds(20);
    Assertions.assertEquals(1L, seconds, "Twenty ticks should equal 1 second");
  }

  @Test
  void testTicksToSeconds_TwoHundredTicks() {
    long seconds = GameTime.ticksToSeconds(200);
    Assertions.assertEquals(10L, seconds, "Two hundred ticks should equal 10 seconds");
  }

  @Test
  void testTicksToSeconds_OneHour() {
    long seconds = GameTime.ticksToSeconds(72000);
    Assertions.assertEquals(3600L, seconds, "72000 ticks should equal 3600 seconds (1 hour)");
  }

  @Test
  void testTicksToSeconds_RoundingDown() {
    long seconds = GameTime.ticksToSeconds(25);
    Assertions.assertEquals(1L, seconds, "25 ticks should round down to 1 second");
  }

  @Test
  void testInvalidInterval_NotDivisor() {
    Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextInterval(7),
            "Should throw IllegalArgumentException for interval that doesn't divide 60"
    );
  }

  @Test
  void testInvalidInterval_InvalidDivisor() {
    Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> GameTime.secondsUntilNextInterval(25),
            "Should throw IllegalArgumentException for interval 25 (doesn't divide 60)"
    );
  }

  @Test
  void testValidIntervals_AllDivisors() {
    // Test all valid divisors of 60
    int[] validIntervals = {1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30};

    for (int interval : validIntervals) {
      Assertions.assertDoesNotThrow(
              () -> GameTime.secondsUntilNextInterval(interval),
              "Should not throw exception for valid interval: " + interval
      );
    }
  }

  @Test
  void testConversionRoundTrip_SecondsToTicksAndBack() {
    long originalSeconds = 120L;
    long ticks = GameTime.secondsToTicks(originalSeconds);
    long convertedBackSeconds = GameTime.ticksToSeconds(ticks);

    Assertions.assertEquals(originalSeconds, convertedBackSeconds,
            "Converting seconds to ticks and back should preserve the value");
  }
}