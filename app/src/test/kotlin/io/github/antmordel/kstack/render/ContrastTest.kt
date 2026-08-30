package io.github.antmordel.kstack.render

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG's threshold for large text, which is what every value on a stacked field is. */
private const val LARGE_TEXT_CONTRAST = 3.0

class ContrastTest {

    @Test
    fun `white reads on a dark background`() {
        assertEquals(Color.White, contentColorOn(Color.Black))
    }

    @Test
    fun `black reads on a light background`() {
        assertEquals(Color.Black, contentColorOn(Color.White))
    }

    @Test
    fun `every zone colour gets readable text on it`() {
        // The saturated mid-tones are the ones a brightness threshold picks wrongly, so the whole
        // palette is checked rather than the extremes.
        (0 until 5).forEach { zone ->
            val background = zoneColor(zone, zoneCount = 5)
            val ratio = contrastRatio(contentColorOn(background), background)

            assertTrue("zone $zone contrast $ratio", ratio >= LARGE_TEXT_CONTRAST)
        }
    }

    @Test
    fun `every zone colour of a seven zone rider gets readable text on it`() {
        (0 until 7).forEach { zone ->
            val background = zoneColor(zone, zoneCount = 7)
            val ratio = contrastRatio(contentColorOn(background), background)

            assertTrue("zone $zone contrast $ratio", ratio >= LARGE_TEXT_CONTRAST)
        }
    }

    @Test
    fun `the contrast ratio of a colour with itself is one`() {
        assertEquals(1.0, contrastRatio(Color.White, Color.White), 0.001)
    }

    @Test
    fun `black on white is the maximum ratio`() {
        assertEquals(21.0, contrastRatio(Color.Black, Color.White), 0.01)
    }
}
