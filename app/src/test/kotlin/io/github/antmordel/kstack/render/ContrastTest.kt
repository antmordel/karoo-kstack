package io.github.antmordel.kstack.render

import androidx.compose.ui.graphics.Color
import io.github.antmordel.kstack.field.ZonePalette
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
    fun `every zone colour on every scale gets readable text on it`() {
        // The saturated mid-tones are the ones a brightness threshold picks wrongly, so every stop
        // is checked rather than the extremes. A colour that cannot be read on cannot be added.
        ZonePalette.entries.forEach { palette ->
            (0 until palette.zoneCount).forEach { zone ->
                val background = zoneColor(palette, zone)
                val ratio = contrastRatio(contentColorOn(background), background)

                assertTrue("$palette zone $zone contrast $ratio", ratio >= LARGE_TEXT_CONTRAST)
            }
        }
    }

    @Test
    fun `the top of each scale is dark enough to need white text`() {
        // Karoo's own hardest zones are deep red and magenta. Black on them is the pick a
        // brightness threshold makes, and it is unreadable.
        assertEquals(Color.White, contentColorOn(zoneColor(ZonePalette.HEART_RATE, 4)))
        assertEquals(Color.White, contentColorOn(zoneColor(ZonePalette.POWER, 6)))
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
