package io.github.antmordel.kstack.render

import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.max
import kotlin.math.min

/** Text sizes, in sp, for one rendering of a stacked field. */
data class StackedTextSizes(
    val primarySp: Float,
    val secondarySp: Float,
)

/** Secondary rows read as subordinate at this fraction of the primary. */
private const val SECONDARY_RATIO = 0.45f

/** Below this the numbers stop being readable at arm's length on a bike. */
private const val MINIMUM_SP = 9f

/** Rough allowance for row spacing and the field's own padding. */
private const val VERTICAL_OVERHEAD = 0.88f

/**
 * Derives text sizes from the configuration Karoo supplies when the view starts.
 *
 * [ViewConfig.textSize] is the size Karoo draws its own numeric field at for this grid size, so it
 * is the ceiling: a KStack field then matches the stock fields beside it. The stack shrinks below
 * that ceiling only when the rows would not otherwise fit the height Karoo has given it.
 *
 * Pure by design — no view is measured, and nothing here needs a laid-out hierarchy.
 *
 * @param density pixels per dp, from the display metrics.
 */
fun stackedTextSizes(
    config: ViewConfig,
    secondaryCount: Int,
    density: Float,
): StackedTextSizes {
    val totalWeight = 1f + SECONDARY_RATIO * secondaryCount
    val availableSp = config.viewSize.second / density * VERTICAL_OVERHEAD

    val primarySp = max(MINIMUM_SP, min(config.textSize.toFloat(), availableSp / totalWeight))
    val secondarySp = max(MINIMUM_SP, primarySp * SECONDARY_RATIO)

    return StackedTextSizes(primarySp = primarySp, secondarySp = secondarySp)
}
