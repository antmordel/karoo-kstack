package io.github.antmordel.kstack.render

import androidx.compose.ui.graphics.Color
import io.github.antmordel.kstack.field.ZonePalette
import io.github.antmordel.kstack.settings.ZonePaletteScheme

/**
 * Karoo's own zone colours, as its Heart Rate Zones and Power Zones settings screens draw them.
 *
 * karoo-ext exposes no palette: `UserProfile.Zone` carries `(min, max)` and nothing else. Matching
 * the device means copying the values, so a Karoo OS change can drift from them.
 *
 * The two scales share their first four colours and diverge above threshold, where power has three
 * zones heart rate does not. That is why a definition names which scale it is on instead of one
 * palette being stretched to fit both.
 */
private val HEART_RATE_STOPS = listOf(
    Color(0xFF60EEB2), // Z1 Active Recovery
    Color(0xFF00B988), // Z2 Endurance
    Color(0xFFFFF500), // Z3 Tempo
    Color(0xFFFB8C65), // Z4 Lactate Threshold
    Color(0xFFD60404), // Z5 Max
)

private val POWER_STOPS = listOf(
    Color(0xFF60EEB2), // Z1 Active Recovery
    Color(0xFF00B988), // Z2 Endurance
    Color(0xFFFFF500), // Z3 Tempo
    Color(0xFFFB8C65), // Z4 Lactate Threshold
    Color(0xFFFE581F), // Z5 VO2 Max
    Color(0xFFD60404), // Z6 Anaerobic Capacity
    Color(0xFFB700A2), // Z7 Neuromuscular
)

private val GARMIN_HEART_RATE_STOPS = listOf(
    Color(0xFF808080), // Z1 Warm Up
    Color(0xFF2196F3), // Z2 Easy
    Color(0xFF4CAF50), // Z3 Aerobic
    Color(0xFFFF9800), // Z4 Threshold
    Color(0xFFF44336), // Z5 Maximum
)

private val GARMIN_POWER_STOPS = listOf(
    Color(0xFF808080), // Z1 Active Recovery
    Color(0xFF2196F3), // Z2 Endurance
    Color(0xFF4CAF50), // Z3 Tempo
    Color(0xFFFFEB3B), // Z4 Lactate Threshold
    Color(0xFFFF9800), // Z5 VO2 Max
    Color(0xFFF44336), // Z6 Anaerobic Capacity
    Color(0xFF9C27B0), // Z7 Neuromuscular
)

private val ZWIFT_HEART_RATE_STOPS = listOf(
    Color(0xFF8A8A8A), // Z1
    Color(0xFF3A9EFD), // Z2
    Color(0xFF5BC236), // Z3
    Color(0xFFF9C300), // Z4
    Color(0xFFE03030), // Z5
)

private val ZWIFT_POWER_STOPS = listOf(
    Color(0xFF7B7B7B), // Z1
    Color(0xFF3A9EFD), // Z2
    Color(0xFF5BC236), // Z3
    Color(0xFFFBCB23), // Z4
    Color(0xFFFF7518), // Z5
    Color(0xFFE82B2B), // Z6
    Color(0xFF9C27B0), // Z7
)

private fun stopsFor(palette: ZonePalette, scheme: ZonePaletteScheme): List<Color> = when (scheme) {
    ZonePaletteScheme.KAROO -> when (palette) {
        ZonePalette.HEART_RATE -> HEART_RATE_STOPS
        ZonePalette.POWER -> POWER_STOPS
    }
    ZonePaletteScheme.GARMIN -> when (palette) {
        ZonePalette.HEART_RATE -> GARMIN_HEART_RATE_STOPS
        ZonePalette.POWER -> GARMIN_POWER_STOPS
    }
    ZonePaletteScheme.ZWIFT -> when (palette) {
        ZonePalette.HEART_RATE -> ZWIFT_HEART_RATE_STOPS
        ZonePalette.POWER -> ZWIFT_POWER_STOPS
    }
}

/**
 * The colour drawn for [zoneIndex] on [palette] under [scheme].
 *
 * A rider who has configured more zones than a scale defines saturates at the top colour,
 * which keeps a hard effort coloured rather than dropping it.
 */
fun zoneColor(
    palette: ZonePalette,
    zoneIndex: Int,
    scheme: ZonePaletteScheme = ZonePaletteScheme.KAROO,
): Color {
    val stops = stopsFor(palette, scheme)
    return stops[zoneIndex.coerceIn(stops.indices)]
}
