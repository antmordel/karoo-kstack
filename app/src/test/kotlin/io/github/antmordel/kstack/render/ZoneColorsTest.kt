package io.github.antmordel.kstack.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ZoneColorsTest {

    @Test
    fun `five zones each get their own colour`() {
        val colors = (0 until 5).map { zoneColor(it, zoneCount = 5) }

        assertEquals(5, colors.toSet().size)
    }

    @Test
    fun `the hardest zone is the same colour whatever the zone count`() {
        // A rider with seven power zones and one with five heart rate zones should both see the
        // top zone in the top colour, which indexing the palette directly would not give.
        assertEquals(zoneColor(4, zoneCount = 5), zoneColor(6, zoneCount = 7))
    }

    @Test
    fun `the easiest zone is the same colour whatever the zone count`() {
        assertEquals(zoneColor(0, zoneCount = 5), zoneColor(0, zoneCount = 7))
    }

    @Test
    fun `the middle zone of seven matches the middle zone of five`() {
        // Indexing the palette directly would give a seven-zone rider the fourth colour here.
        assertEquals(zoneColor(2, zoneCount = 5), zoneColor(3, zoneCount = 7))
    }

    @Test
    fun `colour rises with the zone`() {
        assertNotEquals(zoneColor(0, zoneCount = 5), zoneColor(4, zoneCount = 5))
    }

    @Test
    fun `a single configured zone still gets a colour`() {
        // The division by zoneCount - 1 would otherwise be a divide by zero.
        assertEquals(zoneColor(4, zoneCount = 5), zoneColor(0, zoneCount = 1))
    }
}
