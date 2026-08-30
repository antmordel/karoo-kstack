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
private const val SECONDARY_RATIO = 0.56f

/** Below this the numbers stop being readable at arm's length on a bike. */
private const val MINIMUM_SP = 9f

/**
 * A line of text occupies more vertical space than its font size — roughly this much, once
 * ascent, descent and leading are counted. Measured against the Karoo: at 1.0 the bottom two rows
 * vanished. 1.45 was the first value that clearly worked; 1.22 was too
 * little — the secondary row fell out of the bottom of the field. This sits between them.
 */
private const val LINE_HEIGHT = 1.35f

/** Breathing room at the top and bottom, in sp, so text does not touch the field boundary. */
private const val VERTICAL_PADDING_SP = 4f

/**
 * Roughly the width of one bold digit as a fraction of its font size.
 *
 * Nothing is measured here, so the widest row is estimated from its character count. Digits and
 * lowercase label letters are close enough in this typeface for one ratio to cover both, and the
 * decimal point is narrower than either — this was tuned down from a first guess that left the
 * heart rate row two points smaller than the width it actually had.
 */
private const val CHAR_ADVANCE = 0.52f

/** Left and right padding plus the gap between the two secondary pairs, in sp. */
private const val HORIZONTAL_PADDING_SP = 26f

/** The icon sits beside the primary value and takes width from it, as a fraction of its size. */
private const val ICON_WIDTH_RATIO = 0.9f

/** Punctuation inside a value is far narrower than a digit: `12:34` is not five digits wide. */
private const val NARROW_CHARS = ".:, "
private const val NARROW_ADVANCE = 0.35f

/**
 * How many digit-widths a string occupies.
 *
 * Counting every character as a digit made `12:34` look five wide and drew Time Stack seven points
 * smaller than the fields beside it.
 */
fun textWidthUnits(text: String): Float =
    text.sumOf { if (it in NARROW_CHARS) NARROW_ADVANCE.toDouble() else 1.0 }.toFloat()

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
 * @param primaryWidth digit-widths in the primary value, its suffix included. Karoo's own fields
 * give the number the whole box; a stacked field puts an icon beside it, so the primary runs out
 * of width sooner than the stock size suggests — at which point Karoo truncates it silently.
 * @param widestSecondaryRow digit-widths in the widest secondary row, labels included. Height alone
 * is not enough: a speed carries a decimal point, so `avg 27.0 max 54.0` overflows a width that
 * `avg 128 max 176` fits.
 * @param density pixels per dp, from the display metrics.
 */
fun stackedTextSizes(
    config: ViewConfig,
    secondaryRowCount: Int,
    primaryWidth: Float,
    widestSecondaryRow: Float,
    density: Float,
): StackedTextSizes {
    val totalWeight = LINE_HEIGHT * (1f + SECONDARY_RATIO * secondaryRowCount)
    val availableHeightSp = config.viewSize.second / density - VERTICAL_PADDING_SP
    val availableWidthSp = config.viewSize.first / density - HORIZONTAL_PADDING_SP

    val fromHeight = availableHeightSp / totalWeight
    val primaryFromWidth = if (primaryWidth > 0f) {
        availableWidthSp / (ICON_WIDTH_RATIO + primaryWidth * CHAR_ADVANCE)
    } else {
        Float.MAX_VALUE
    }
    val primarySp = max(
        MINIMUM_SP,
        minOf(config.textSize.toFloat(), fromHeight, primaryFromWidth),
    )

    val fromWidth = if (widestSecondaryRow > 0f) {
        availableWidthSp / (widestSecondaryRow * CHAR_ADVANCE)
    } else {
        Float.MAX_VALUE
    }
    val secondarySp = max(MINIMUM_SP, min(primarySp * SECONDARY_RATIO, fromWidth))

    return StackedTextSizes(primarySp = primarySp, secondarySp = secondarySp)
}
