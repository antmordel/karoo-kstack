package io.github.antmordel.kstack.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldSettingsTest {

    @Test
    fun `a field with nothing stored gets the default layout`() {
        assertEquals(SecondaryLayout.SIDE_BY_SIDE, fieldSettingsFrom(null, null).secondaryLayout)
    }

    @Test
    fun `a stored choice is read back`() {
        assertEquals(SecondaryLayout.STACKED, fieldSettingsFrom("STACKED", null).secondaryLayout)
    }

    @Test
    fun `a field with nothing stored has zone colouring off`() {
        assertEquals(ZoneColorMode.NONE, fieldSettingsFrom(null, null).zoneColorMode)
    }

    @Test
    fun `a stored colour mode is read back`() {
        assertEquals(ZoneColorMode.ICON, fieldSettingsFrom(null, "ICON").zoneColorMode)
    }

    @Test
    fun `the two settings are independent`() {
        val settings = fieldSettingsFrom("STACKED", "ICON")

        assertEquals(SecondaryLayout.STACKED, settings.secondaryLayout)
        assertEquals(ZoneColorMode.ICON, settings.zoneColorMode)
    }

    @Test
    fun `a value that is no longer a known option falls back to the default`() {
        // A downgrade, or a renamed enum entry, must leave a working field rather than crash.
        assertEquals(SecondaryLayout.SIDE_BY_SIDE, fieldSettingsFrom("DIAGONAL", null).secondaryLayout)
    }
}
