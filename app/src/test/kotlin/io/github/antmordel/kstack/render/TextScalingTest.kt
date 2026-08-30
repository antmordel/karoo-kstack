package io.github.antmordel.kstack.render

import io.hammerhead.karooext.models.ViewConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DENSITY = 2f

private fun config(textSize: Int, heightPx: Int, widthPx: Int = 240) = ViewConfig(
    gridSize = 30 to 15,
    viewSize = widthPx to heightPx,
    textSize = textSize,
)

/** `avg 128 max 176` — a heart rate stack, the row these sizes were originally tuned against. */
private const val TYPICAL_ROW = 16f

/** `142` — three digits, no suffix. */
private const val TYPICAL_PRIMARY = 3f

private fun sizes(
    textSize: Int,
    heightPx: Int,
    rows: Int,
    widestSecondaryRow: Float = TYPICAL_ROW,
    primaryWidth: Float = TYPICAL_PRIMARY,
    widthPx: Int = 240,
) = stackedTextSizes(
    config = config(textSize = textSize, heightPx = heightPx, widthPx = widthPx),
    secondaryRowCount = rows,
    primaryWidth = primaryWidth,
    widestSecondaryRow = widestSecondaryRow,
    density = DENSITY,
)

class TextScalingTest {

    @Test
    fun `primary never exceeds the size Karoo draws its own fields at`() {
        // Wide as well as tall, so the ceiling is what binds rather than the width.
        val sizes = sizes(textSize = 40, heightPx = 4000, rows = 1, widthPx = 2000)

        assertEquals(40f, sizes.primarySp, 0.01f)
    }

    @Test
    fun `secondaries stay subordinate to the primary`() {
        val sizes = sizes(
            textSize = 40,
            heightPx = 4000,
            rows = 1,
        )

        assertTrue("${sizes.secondarySp} should be below ${sizes.primarySp}", sizes.secondarySp < sizes.primarySp)
    }

    @Test
    fun `a short field shrinks the text rather than overflowing`() {
        val roomy = sizes(
            textSize = 40,
            heightPx = 4000,
            rows = 1,
        )
        val cramped = sizes(
            textSize = 40,
            heightPx = 120,
            rows = 1,
        )

        assertTrue("${cramped.primarySp} should be below ${roomy.primarySp}", cramped.primarySp < roomy.primarySp)
    }

    @Test
    fun `the composed stack fits the height it was given`() {
        val heightPx = 200
        val secondaryRowCount = 1
        val sizes = sizes(textSize = 40, heightPx = heightPx, rows = secondaryRowCount)

        // A rendered line is taller than its font size; the field must fit the real thing.
        val lineHeight = 1.45f
        val usedPx = (sizes.primarySp + sizes.secondarySp * secondaryRowCount) * lineHeight * DENSITY

        assertTrue("used ${usedPx}px of ${heightPx}px", usedPx <= heightPx)
    }

    @Test
    fun `more secondary rows leave the primary smaller`() {
        val two = sizes(
            textSize = 40,
            heightPx = 200,
            rows = 1,
        )
        val four = sizes(
            textSize = 40,
            heightPx = 200,
            rows = 2,
        )

        assertTrue("${four.primarySp} should be below ${two.primarySp}", four.primarySp < two.primarySp)
    }

    @Test
    fun `every character counts the same`() {
        // Discounting punctuation double-counts the spacing CHAR_ADVANCE already absorbs, which
        // wrapped `12:34` onto two lines on a real Karoo.
        assertEquals(5f, textWidthUnits("12:34"), 0.01f)
        assertEquals(3f, textWidthUnits("142"), 0.01f)
    }

    @Test
    fun `a wider secondary row is drawn smaller so it fits the width`() {
        // `avg 27.0 max 54.0` against `avg 128 max 176`: the decimals are what overflowed a real
        // Karoo, and height alone cannot see the difference.
        val heartRate = sizes(textSize = 40, heightPx = 4000, rows = 1, widestSecondaryRow = 16f)
        val speed = sizes(textSize = 40, heightPx = 4000, rows = 1, widestSecondaryRow = 20f)

        assertTrue("${speed.secondarySp} should be below ${heartRate.secondarySp}",
            speed.secondarySp < heartRate.secondarySp)
    }

    @Test
    fun `the widest secondary row fits the width it was given`() {
        val widthPx = 600
        val row = 20f
        val sizes = sizes(
            textSize = 40,
            heightPx = 4000,
            rows = 1,
            widestSecondaryRow = row,
            widthPx = widthPx,
        )

        val usedPx = (row * sizes.secondarySp * 0.55f + 38f) * DENSITY

        assertTrue("used ${usedPx}px of ${widthPx}px", usedPx <= widthPx)
    }

    @Test
    fun `legibility wins over fitting in a field too narrow for either`() {
        // A row that cannot fit above the legibility floor overflows rather than shrinking into
        // something unreadable at arm's length. Deliberate, and the only case where width loses.
        val sizes = sizes(textSize = 40, heightPx = 4000, rows = 1, widestSecondaryRow = 20f, widthPx = 240)

        assertEquals(9f, sizes.secondarySp, 0.01f)
    }

    @Test
    fun `a field with no secondaries is not squeezed by a width of zero`() {
        val none = sizes(
            textSize = 40,
            heightPx = 4000,
            rows = 0,
            widestSecondaryRow = 0f,
            widthPx = 2000,
        )

        assertEquals(40f, none.primarySp, 0.01f)
    }

    @Test
    fun `a long primary value is drawn smaller rather than truncated`() {
        // Karoo clips a value that does not fit instead of shrinking it, so `142` came out as
        // `14` on a real device. The icon beside the number is what makes width run out first.
        val short = sizes(textSize = 60, heightPx = 4000, rows = 1, primaryWidth = 3f)
        val long = sizes(textSize = 60, heightPx = 4000, rows = 1, primaryWidth = 6f)

        assertTrue("${long.primarySp} should be below ${short.primarySp}",
            long.primarySp < short.primarySp)
    }

    @Test
    fun `the primary and its icon fit the width they were given`() {
        val widthPx = 240
        val primaryWidth = 6f
        val sizes = sizes(
            textSize = 60,
            heightPx = 4000,
            rows = 1,
            primaryWidth = primaryWidth,
            widthPx = widthPx,
        )

        val usedPx = (0.8f + primaryWidth * 0.55f) * sizes.primarySp * DENSITY + 24f * DENSITY

        assertTrue("used ${usedPx}px of ${widthPx}px", usedPx <= widthPx)
    }

    @Test
    fun `text stays legible in the smallest field Karoo can hand us`() {
        val sizes = sizes(
            textSize = 12,
            heightPx = 40,
            rows = 1,
        )

        assertTrue("primary ${sizes.primarySp}", sizes.primarySp >= 9f)
        assertTrue("secondary ${sizes.secondarySp}", sizes.secondarySp >= 9f)
    }
}
