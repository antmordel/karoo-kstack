package io.github.antmordel.kstack.field

/**
 * Which of Karoo's two zone scales a metric is on.
 *
 * They are different lengths and different colours above the fourth zone, so a definition has to
 * say which one it means. [zoneCount] is how many zones Karoo defines for the metric, used to
 * preview a coloured field across its whole scale.
 */
enum class ZonePalette(val zoneCount: Int) {
    HEART_RATE(5),
    POWER(7),
}

/**
 * Turns the zone number Karoo reports into a 0-based index into the rider's zones.
 *
 * Karoo reports zones the way its own screens label them, so Z1 arrives as 1. A number outside the
 * rider's configured range is clamped to the nearest zone rather than dropping the colour, because
 * a field that loses its colour at the top of an effort is worse than one that saturates.
 *
 * Returns `null` when the rider has no zones configured for the metric, which renders exactly as
 * colouring switched off.
 */
fun zoneIndex(reported: Int, zoneCount: Int): Int? =
    if (zoneCount <= 0) null else (reported - 1).coerceIn(0, zoneCount - 1)
