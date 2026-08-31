package io.github.antmordel.kstack.render

import io.github.antmordel.kstack.field.ZonePalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ZoneColorsTest {

    @Test
    fun `each zone on a scale gets its own colour`() {
        ZonePalette.entries.forEach { palette ->
            val colors = (0 until palette.zoneCount).map { zoneColor(palette, it) }

            assertEquals(palette.name, palette.zoneCount, colors.toSet().size)
        }
    }

    @Test
    fun `the two scales agree up to threshold`() {
        // Karoo draws the first four zones the same on both settings screens.
        (0 until 4).forEach { zone ->
            assertEquals(
                "zone $zone",
                zoneColor(ZonePalette.HEART_RATE, zone),
                zoneColor(ZonePalette.POWER, zone),
            )
        }
    }

    @Test
    fun `the scales diverge above threshold`() {
        // Zone 5 is Max on the heart rate scale and VO2 Max on the power scale, and Karoo colours
        // them differently. Resampling one palette across both zone counts would tie them.
        assertNotEquals(
            zoneColor(ZonePalette.HEART_RATE, 4),
            zoneColor(ZonePalette.POWER, 4),
        )
    }

    @Test
    fun `the hardest power zone is a colour the heart rate scale never shows`() {
        val heartRate = (0 until ZonePalette.HEART_RATE.zoneCount)
            .map { zoneColor(ZonePalette.HEART_RATE, it) }

        assertEquals(false, zoneColor(ZonePalette.POWER, 6) in heartRate)
    }

    @Test
    fun `a rider with more zones than Karoo defines saturates at the top colour`() {
        // Dropping the colour at the top of an effort would be worse than repeating one.
        val top = zoneColor(ZonePalette.HEART_RATE, ZonePalette.HEART_RATE.zoneCount - 1)

        assertEquals(top, zoneColor(ZonePalette.HEART_RATE, 9))
    }

    @Test
    fun `a zone below the scale takes the easiest colour`() {
        assertEquals(zoneColor(ZonePalette.POWER, 0), zoneColor(ZonePalette.POWER, -3))
    }
}
