package io.github.antmordel.kstack.render

import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.max
import kotlin.math.min

/** Text sizes, in sp, for one rendering of a stacked field. */
data class StackedTextSizes(
    val primarySp: Float,
    val secondarySp: Float,
)

/** Secondary values as a fraction of the primary. Set by eye on a Karoo. */
private const val SECONDARY_RATIO = 0.48f

/** Below this the numbers stop being readable at arm's length on a bike. */
private const val MINIMUM_SP = 9f

/**
 * A line of text occupies more vertical space than its font size — roughly this much, once
 * ascent, descent and leading are counted. Measured against the Karoo: at 1.0 the bottom two rows
 * vanished, at 1.3 the last row was still clipped in half.
 */
private const val LINE_HEIGHT = 1.45f

/** Breathing room at the top and bottom, in sp, so text does not touch the field boundary. */
private const val VERTICAL_PADDING_SP = 4f

/**
 * Derives text sizes from the configuration Karoo supplies when the view starts.
 *
 * [ViewConfig.textSize] is the size Karoo draws its own numeric field at for this grid size, so it
 * is the ceiling: a KStack field then matches the stock fields beside it. The stack shrinks below
 * that ceiling only when the rows would not otherwise fit the height Karoo has given it.
 *
 * Pure by design — no view is measured, and nothing here needs a laid-out hierarchy.
 *
 * @param secondaryRowCount rows of secondary values, not values: secondaries sit side by side, so
 * two of them cost one row and leave the primary the height that buys.
 * @param density pixels per dp, from the display metrics.
 */
fun stackedTextSizes(
    config: ViewConfig,
    secondaryRowCount: Int,
    density: Float,
): StackedTextSizes {
    val totalWeight = LINE_HEIGHT * (1f + SECONDARY_RATIO * secondaryRowCount)
    val availableSp = config.viewSize.second / density - VERTICAL_PADDING_SP

    val primarySp = max(MINIMUM_SP, min(config.textSize.toFloat(), availableSp / totalWeight))
    val secondarySp = max(MINIMUM_SP, primarySp * SECONDARY_RATIO)

    return StackedTextSizes(primarySp = primarySp, secondarySp = secondarySp)
}
