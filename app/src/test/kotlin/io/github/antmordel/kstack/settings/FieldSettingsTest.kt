package io.github.antmordel.kstack.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldSettingsTest {

    @Test
    fun `a field with nothing stored gets the default layout`() {
        assertEquals(SecondaryLayout.SIDE_BY_SIDE, fieldSettingsFrom(null).secondaryLayout)
    }

    @Test
    fun `a stored choice is read back`() {
        assertEquals(SecondaryLayout.STACKED, fieldSettingsFrom("STACKED").secondaryLayout)
    }

    @Test
    fun `a value that is no longer a known option falls back to the default`() {
        // A downgrade, or a renamed enum entry, must leave a working field rather than crash.
        assertEquals(SecondaryLayout.SIDE_BY_SIDE, fieldSettingsFrom("DIAGONAL").secondaryLayout)
    }
}
