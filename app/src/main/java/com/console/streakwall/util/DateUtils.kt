package com.console.streakwall.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/**
 * All date math works in [LocalDate], never by dividing millisecond differences, so DST
 * transitions can't produce off-by-one-hour errors.
 *
 * The start date is persisted as the UTC midnight of the chosen calendar date. That is the
 * same convention Compose's DatePicker uses, and it makes the stored value zone-independent:
 * the calendar date the user picked stays the same even if the device later changes timezone
 * (travel, or an automatic timezone update). Only "today" is resolved in the device's zone.
 */
object DateUtils {

    /** Number of whole calendar days between the stored start date and [today]. */
    fun daysSince(
        startDateMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zoneId)
    ): Int = ChronoUnit.DAYS.between(toLocalDate(startDateMillis), today).toInt().coerceAtLeast(0)

    /** The 1-indexed "Day N" shown to the user; day 1 is the start date itself. */
    fun dayNumber(
        startDateMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zoneId)
    ): Int = daysSince(startDateMillis, zoneId, today) + 1

    /**
     * Milliseconds from [now] until the next local midnight. Computed from the zone's rules, so
     * it is 23h or 25h long on DST-change days rather than a fixed 24h.
     */
    fun millisUntilNextMidnight(now: ZonedDateTime = ZonedDateTime.now()): Long {
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        return Duration.between(now, nextMidnight).toMillis()
    }

    fun toLocalDate(dateMillis: Long): LocalDate =
        Instant.ofEpochMilli(dateMillis).atZone(ZoneOffset.UTC).toLocalDate()

    /** Canonical persisted form of a calendar date. */
    fun startOfDayMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun todayStartOfDayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
        startOfDayMillis(LocalDate.now(zoneId))

    fun formatForDisplay(dateMillis: Long): String =
        toLocalDate(dateMillis).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
}
