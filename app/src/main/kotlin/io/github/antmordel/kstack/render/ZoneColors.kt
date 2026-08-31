package io.github.antmordel.kstack.render

import androidx.compose.ui.graphics.Color
import io.github.antmordel.kstack.field.ZonePalette

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

private val ZonePalette.stops: List<Color>
    get() = when (this) {
        ZonePalette.HEART_RATE -> HEART_RATE_STOPS
        ZonePalette.POWER -> POWER_STOPS
    }

/**
 * The colour Karoo would draw for [zoneIndex] on [palette].
 *
 * A rider who has configured more zones than Karoo's scale defines saturates at the top colour,
 * which keeps a hard effort coloured rather than dropping it.
 */
fun zoneColor(palette: ZonePalette, zoneIndex: Int): Color =
    palette.stops[zoneIndex.coerceIn(palette.stops.indices)]
