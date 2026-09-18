package com.console.streakwall.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.WindowManager
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.console.streakwall.data.WallpaperTheme

/**
 * Renders the "Day N" wallpaper bitmap.
 *
 * The trickiest part here is sizing: we must draw at the device's *actual* screen
 * resolution, not a hardcoded constant, or the wallpaper will be upscaled/cropped oddly by
 * WallpaperManager on devices that differ from whatever we assumed. There is no Activity
 * available when this runs from a background Worker, so we can't use
 * Activity#windowManager — instead we go through DisplayManager to get the default
 * display, with an API-level-appropriate way to query its real (full, not just the app's
 * visible) pixel size.
 */
object WallpaperGenerator {

    private val MILESTONES = intArrayOf(7, 30, 90, 365)

    fun isMilestoneDay(dayNumber: Int): Boolean = dayNumber in MILESTONES

    fun generate(context: Context, dayNumber: Int, habitName: String, theme: WallpaperTheme): Bitmap {
        val (width, height) = screenSizePx(context)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val isMilestone = isMilestoneDay(dayNumber)

        drawBackground(canvas, width, height, theme)
        drawContent(canvas, width, height, dayNumber, habitName, theme, isMilestone)

        return bitmap
    }

    /**
     * Real display pixel size, independent of the current app window (which may be smaller
     * than the screen, e.g. in split-screen). [WindowManager.getCurrentWindowMetrics] is the
     * modern (API 30+) way to get this; below that we fall back to the deprecated but still
     * functional [android.view.Display.getRealMetrics].
     */
    private fun screenSizePx(context: Context): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val bounds = windowManager.maximumWindowMetrics.bounds
            return bounds.width() to bounds.height()
        }

        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(android.view.Display.DEFAULT_DISPLAY)
        val metrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)
        return metrics.widthPixels to metrics.heightPixels
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int, theme: WallpaperTheme) {
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                theme.background.toArgb(),
                theme.backgroundSecondary.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawContent(
        canvas: Canvas,
        width: Int,
        height: Int,
        dayNumber: Int,
        habitName: String,
        theme: WallpaperTheme,
        isMilestone: Boolean
    ) {
        val centerX = width / 2f
        val centerY = height / 2f

        // Scale text relative to screen width so this looks right across phone/tablet sizes.
        val dayNumberSizePx = width * 0.28f
        val dayLabelSizePx = width * 0.055f
        val habitNameSizePx = width * 0.065f
        val badgeSizePx = width * 0.045f

        val accentColor = theme.accent.toArgb()
        val onBackgroundColor = theme.onBackground.toArgb()

        val numberText = dayNumber.toString()
        val dayNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isMilestone) accentColor else onBackgroundColor
            textSize = dayNumberSizePx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        // Lay everything out from the number's real glyph bounds (not fixed multiples of the
        // font size) so the "DAY" label and badge can never overlap the digits.
        val numberBounds = Rect()
        dayNumberPaint.getTextBounds(numberText, 0, numberText.length, numberBounds)
        val numberBaseline = centerY + numberBounds.height() / 2f
        val numberTop = numberBaseline + numberBounds.top
        val gap = width * 0.05f

        canvas.drawText(numberText, centerX, numberBaseline, dayNumberPaint)

        val dayLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColorUtils.setAlphaComponent(onBackgroundColor, 200)
            textSize = dayLabelSizePx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            letterSpacing = 0.25f
        }
        val dayLabelBaseline = numberTop - gap
        canvas.drawText("DAY", centerX, dayLabelBaseline, dayLabelPaint)

        if (isMilestone) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = badgeSizePx
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                letterSpacing = 0.15f
            }
            val badgeBaseline = dayLabelBaseline - dayLabelSizePx - gap
            canvas.drawText(milestoneBadgeText(dayNumber), centerX, badgeBaseline, badgePaint)
        }

        val habitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onBackgroundColor
            textSize = habitNameSizePx
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
        val habitBaseline = numberBaseline + gap + habitNameSizePx
        drawWrappedText(canvas, habitName.uppercase(), centerX, habitBaseline, width * 0.82f, habitPaint)
    }

    private fun milestoneBadgeText(dayNumber: Int): String = when (dayNumber) {
        7 -> "★ 1 WEEK STRONG ★"
        30 -> "★ 1 MONTH STRONG ★"
        90 -> "★ 90 DAYS STRONG ★"
        365 -> "★ 1 YEAR STRONG ★"
        else -> "★ MILESTONE ★"
    }

    /** Wraps [text] across multiple centered lines if it's too wide for [maxWidth]. */
    private fun drawWrappedText(canvas: Canvas, text: String, centerX: Float, startY: Float, maxWidth: Float, paint: Paint) {
        if (text.isBlank()) return
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth || currentLine.isEmpty()) {
                currentLine = StringBuilder(candidate)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())

        val lineHeight = paint.textSize * 1.25f
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, centerX, startY + index * lineHeight, paint)
        }
    }
}
