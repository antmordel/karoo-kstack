package io.github.antmordel.kstack.render

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * The colours a zone can take, easiest effort first.
 *
 * karoo-ext exposes no palette: `UserProfile.Zone` carries `(min, max)` and nothing else, and
 * Karoo's own colours are not published to extensions. These are matched by eye against the stock
 * heart rate field, so a Karoo OS change can drift from them.
 */
private val ZONE_STOPS = listOf(
    Color(0xFF9E9E9E), // grey - recovery
    Color(0xFF3B82F6), // blue - endurance
    Color(0xFF22A559), // green - tempo
    Color(0xFFF59E0B), // amber - threshold
    Color(0xFFDC2626), // red - anaerobic
)

/**
 * The colour for a zone, spread across [ZONE_STOPS] by position rather than by index.
 *
 * A rider with five heart rate zones lands on the five stops exactly. A rider with seven power
 * zones gets the same progression resampled, so the hardest zone is red either way — which
 * indexing the palette directly would not give.
 */
fun zoneColor(zoneIndex: Int, zoneCount: Int): Color {
    if (zoneCount <= 1) return ZONE_STOPS.last()
    val position = zoneIndex.toDouble() / (zoneCount - 1)
    val stop = (position * (ZONE_STOPS.size - 1)).roundToInt().coerceIn(ZONE_STOPS.indices)
    return ZONE_STOPS[stop]
}
