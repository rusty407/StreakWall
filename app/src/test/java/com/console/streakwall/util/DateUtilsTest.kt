package com.console.streakwall.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DateUtilsTest {

    private val newYork = ZoneId.of("America/New_York")

    @Test
    fun `day number is 1 on the start date itself`() {
        val start = LocalDate.of(2026, 1, 1)
        val millis = DateUtils.startOfDayMillis(start)

        assertEquals(0, DateUtils.daysSince(millis, newYork, today = start))
        assertEquals(1, DateUtils.dayNumber(millis, newYork, today = start))
    }

    @Test
    fun `day number is 8 seven days after start`() {
        val start = LocalDate.of(2026, 1, 1)
        val millis = DateUtils.startOfDayMillis(start)

        assertEquals(8, DateUtils.dayNumber(millis, newYork, today = start.plusDays(7)))
    }

    @Test
    fun `count is exact across the spring-forward DST transition`() {
        // US clocks skipped 2:00-3:00 AM on 2026-03-08. Calendar-day math must still say 2.
        val start = LocalDate.of(2026, 3, 7)
        val millis = DateUtils.startOfDayMillis(start)

        assertEquals(2, DateUtils.daysSince(millis, newYork, today = LocalDate.of(2026, 3, 9)))
    }

    @Test
    fun `count is exact across the fall-back DST transition`() {
        val start = LocalDate.of(2026, 10, 31)
        val millis = DateUtils.startOfDayMillis(start)

        assertEquals(3, DateUtils.daysSince(millis, newYork, today = LocalDate.of(2026, 11, 3)))
    }

    @Test
    fun `start date does not shift when the device timezone changes`() {
        val start = LocalDate.of(2026, 6, 15)
        val millis = DateUtils.startOfDayMillis(start)

        assertEquals(start, DateUtils.toLocalDate(millis))
        val today = LocalDate.of(2026, 6, 20)
        val zones = listOf("Pacific/Auckland", "Asia/Tokyo", "Europe/London", "America/Los_Angeles", "Pacific/Pago_Pago")
        zones.forEach { zone ->
            assertEquals(zone, 5, DateUtils.daysSince(millis, ZoneId.of(zone), today))
        }
    }

    @Test
    fun `daysSince never goes negative for a future start date`() {
        val millis = DateUtils.startOfDayMillis(LocalDate.of(2026, 6, 20))

        assertEquals(0, DateUtils.daysSince(millis, newYork, today = LocalDate.of(2026, 6, 10)))
    }

    @Test
    fun `midnight delay is exact on a normal day`() {
        val now = ZonedDateTime.of(2026, 6, 10, 21, 30, 0, 0, newYork)

        assertEquals(2.5 * 3_600_000, DateUtils.millisUntilNextMidnight(now).toDouble(), 0.0)
    }

    @Test
    fun `midnight delay accounts for the 23 hour spring-forward day`() {
        // 00:30 EST on 2026-03-08; midnight is 00:00 EDT the next day, only 22.5h away.
        val now = ZonedDateTime.of(2026, 3, 8, 0, 30, 0, 0, newYork)

        assertEquals(22.5 * 3_600_000, DateUtils.millisUntilNextMidnight(now).toDouble(), 0.0)
    }

    @Test
    fun `midnight delay accounts for the 25 hour fall-back day`() {
        // 00:30 EDT on 2026-11-01; midnight is 00:00 EST the next day, 24.5h away.
        val now = ZonedDateTime.of(2026, 11, 1, 0, 30, 0, 0, newYork)

        assertEquals(24.5 * 3_600_000, DateUtils.millisUntilNextMidnight(now).toDouble(), 0.0)
    }

    @Test
    fun `midnight delay is a full day just after midnight`() {
        val now = ZonedDateTime.of(2026, 6, 10, 0, 0, 0, 0, newYork)

        assertEquals(24 * 3_600_000L, DateUtils.millisUntilNextMidnight(now))
    }
}
