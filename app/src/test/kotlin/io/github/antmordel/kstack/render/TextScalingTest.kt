package io.github.antmordel.kstack.render

import io.hammerhead.karooext.models.ViewConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DENSITY = 2f

private fun config(textSize: Int, heightPx: Int) = ViewConfig(
    gridSize = 30 to 15,
    viewSize = 240 to heightPx,
    textSize = textSize,
)

class TextScalingTest {

    @Test
    fun `primary never exceeds the size Karoo draws its own fields at`() {
        val sizes = stackedTextSizes(config(textSize = 40, heightPx = 4000), secondaryCount = 2, DENSITY)

        assertEquals(40f, sizes.primarySp, 0.01f)
    }

    @Test
    fun `secondaries stay subordinate to the primary`() {
        val sizes = stackedTextSizes(config(textSize = 40, heightPx = 4000), secondaryCount = 2, DENSITY)

        assertTrue("${sizes.secondarySp} should be below ${sizes.primarySp}", sizes.secondarySp < sizes.primarySp)
    }

    @Test
    fun `a short field shrinks the text rather than overflowing`() {
        val roomy = stackedTextSizes(config(textSize = 40, heightPx = 4000), secondaryCount = 2, DENSITY)
        val cramped = stackedTextSizes(config(textSize = 40, heightPx = 120), secondaryCount = 2, DENSITY)

        assertTrue("${cramped.primarySp} should be below ${roomy.primarySp}", cramped.primarySp < roomy.primarySp)
    }

    @Test
    fun `the composed stack fits the height it was given`() {
        val heightPx = 200
        val secondaryCount = 2
        val sizes = stackedTextSizes(config(textSize = 40, heightPx = heightPx), secondaryCount, DENSITY)

        val usedPx = (sizes.primarySp + sizes.secondarySp * secondaryCount) * DENSITY

        assertTrue("used ${usedPx}px of ${heightPx}px", usedPx <= heightPx)
    }

    @Test
    fun `more secondary rows leave the primary smaller`() {
        val two = stackedTextSizes(config(textSize = 40, heightPx = 200), secondaryCount = 2, DENSITY)
        val four = stackedTextSizes(config(textSize = 40, heightPx = 200), secondaryCount = 4, DENSITY)

        assertTrue("${four.primarySp} should be below ${two.primarySp}", four.primarySp < two.primarySp)
    }

    @Test
    fun `text stays legible in the smallest field Karoo can hand us`() {
        val sizes = stackedTextSizes(config(textSize = 12, heightPx = 40), secondaryCount = 2, DENSITY)

        assertTrue("primary ${sizes.primarySp}", sizes.primarySp >= 9f)
        assertTrue("secondary ${sizes.secondarySp}", sizes.secondarySp >= 9f)
    }
}
