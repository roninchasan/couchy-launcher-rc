package com.conreo.couchytv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val ACCENTS = listOf(
    Color(0xFF8AB4F8),
    Color(0xFF81C995),
    Color(0xFFFDD663),
    Color(0xFFF28B82),
    Color(0xFFD7AEFB),
    Color(0xFF78D9EC),
)

data class WallpaperPreset(val name: String, val colors: List<Color>)

val WALLPAPERS = listOf(
    WallpaperPreset("Midnight", listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))),
    WallpaperPreset("Aurora", listOf(Color(0xFF13547A), Color(0xFF2B825B), Color(0xFF80D0C7))),
    WallpaperPreset("Sunset", listOf(Color(0xFF41295A), Color(0xFF8F4A3E), Color(0xFFD76D77))),
    WallpaperPreset("Deep", listOf(Color(0xFF000428), Color(0xFF004683))),
    WallpaperPreset("Charcoal", listOf(Color(0xFF16161A), Color(0xFF232526), Color(0xFF2F3437))),
)

fun WallpaperPreset.brush(): Brush = Brush.linearGradient(colors)

private class DayStop(val hour: Float, val top: Color, val bottom: Color)

// Keyframes across the day; the last repeats the first so midnight wraps cleanly.
private val DAY_STOPS = listOf(
    DayStop(0f, Color(0xFF070B1E), Color(0xFF1A2347)),   // night
    DayStop(5f, Color(0xFF1B2250), Color(0xFF5B4B8A)),   // pre-dawn
    DayStop(7f, Color(0xFF3B5B9A), Color(0xFFE59A78)),   // sunrise
    DayStop(10f, Color(0xFF2A6FB0), Color(0xFF7CC4E8)),  // morning
    DayStop(14f, Color(0xFF1F5FA5), Color(0xFF5FB3D6)),  // afternoon
    DayStop(17.5f, Color(0xFF2D4F8E), Color(0xFFE5A36B)), // golden hour
    DayStop(19.5f, Color(0xFF3A2A66), Color(0xFFD9667A)), // sunset
    DayStop(21.5f, Color(0xFF141B3F), Color(0xFF3B3B7A)), // dusk
    DayStop(24f, Color(0xFF070B1E), Color(0xFF1A2347)),
)

/** Vertical sky gradient for [hour] (0..24, fractional), blended between keyframes. */
fun timeOfDayBrush(hour: Float): Brush {
    val h = hour.coerceIn(0f, 24f)
    val i = DAY_STOPS.indexOfLast { it.hour <= h }.coerceIn(0, DAY_STOPS.size - 2)
    val a = DAY_STOPS[i]
    val b = DAY_STOPS[i + 1]
    val t = ((h - a.hour) / (b.hour - a.hour)).coerceIn(0f, 1f)
    return Brush.verticalGradient(listOf(lerp(a.top, b.top, t), lerp(a.bottom, b.bottom, t)))
}

/** Deterministic tile color for apps that ship no banner artwork. */
fun tileColor(pkg: String): Color {
    val palette = listOf(
        Color(0xFF37474F), Color(0xFF4E342E), Color(0xFF1B5E20),
        Color(0xFF0D47A1), Color(0xFF4A148C), Color(0xFF880E4F),
        Color(0xFF3E2723), Color(0xFF263238), Color(0xFF33691E),
    )
    var h = 0
    for (c in pkg) h = h * 31 + c.code
    return palette[((h % palette.size) + palette.size) % palette.size]
}

@Composable
fun LiteTvTheme(accent: Color, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accent,
            surface = Color(0xEE1C1E24),
            onSurface = Color(0xFFE8EAED),
            surfaceVariant = Color(0xFF303440),
            onSurfaceVariant = Color(0xFF9AA0A6),
        ),
        content = content,
    )
}
