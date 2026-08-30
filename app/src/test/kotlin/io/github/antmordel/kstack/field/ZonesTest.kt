package io.github.antmordel.kstack.field

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ZonesTest {

    @Test
    fun `karoo's first zone is the first of the rider's zones`() {
        assertEquals(0, zoneIndex(reported = 1, zoneCount = 5))
    }

    @Test
    fun `the last zone maps to the last of the rider's zones`() {
        assertEquals(4, zoneIndex(reported = 5, zoneCount = 5))
    }

    @Test
    fun `a rider with no zones configured gets no zone`() {
        // Same outcome as colouring switched off, rather than a colour derived from nothing.
        assertNull(zoneIndex(reported = 3, zoneCount = 0))
    }

    @Test
    fun `a zone above the configured range saturates instead of dropping the colour`() {
        // Losing the colour at the top of an effort is worse than saturating at the hardest zone.
        assertEquals(4, zoneIndex(reported = 9, zoneCount = 5))
    }

    @Test
    fun `a zone below the configured range saturates at the easiest zone`() {
        assertEquals(0, zoneIndex(reported = 0, zoneCount = 5))
    }
}
