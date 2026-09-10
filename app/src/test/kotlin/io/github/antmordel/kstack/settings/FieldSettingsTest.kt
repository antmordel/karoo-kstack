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
    fun `a field with nothing stored colours its icon by zone`() {
        assertEquals(ZoneColorMode.ICON, fieldSettingsFrom(null, null).zoneColorMode)
    }

    @Test
    fun `a stored colour mode is read back`() {
        assertEquals(ZoneColorMode.ICON, fieldSettingsFrom(null, "ICON").zoneColorMode)
    }

    @Test
    fun `the field colouring mode is read back`() {
        assertEquals(ZoneColorMode.FIELD, fieldSettingsFrom(null, "FIELD").zoneColorMode)
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
        assertEquals(ZoneColorMode.ICON, fieldSettingsFrom(null, "BACKGROUND").zoneColorMode)
        assertEquals(ZonePaletteScheme.KAROO, fieldSettingsFrom(null, null, "UNKNOWN").zonePaletteScheme)
        assertEquals(PowerAveraging.INSTANT, fieldSettingsFrom(null, null, null, "UNKNOWN").powerAveraging)
    }

    @Test
    fun `palette choices are read back`() {
        assertEquals(ZonePaletteScheme.KAROO, fieldSettingsFrom(null, null, null).zonePaletteScheme)
        assertEquals(ZonePaletteScheme.GARMIN, fieldSettingsFrom(null, null, "GARMIN").zonePaletteScheme)
        assertEquals(ZonePaletteScheme.ZWIFT, fieldSettingsFrom(null, null, "ZWIFT").zonePaletteScheme)
    }

    @Test
    fun `power averaging choices are read back`() {
        assertEquals(PowerAveraging.INSTANT, fieldSettingsFrom(null, null, null, null).powerAveraging)
        assertEquals(PowerAveraging.SMOOTHED_3S, fieldSettingsFrom(null, null, null, "SMOOTHED_3S").powerAveraging)
        assertEquals(PowerAveraging.SMOOTHED_5S, fieldSettingsFrom(null, null, null, "SMOOTHED_5S").powerAveraging)
        assertEquals(PowerAveraging.SMOOTHED_10S, fieldSettingsFrom(null, null, null, "SMOOTHED_10S").powerAveraging)
        assertEquals(PowerAveraging.SMOOTHED_30S, fieldSettingsFrom(null, null, null, "SMOOTHED_30S").powerAveraging)
    }

    @Test
    fun `power averaging cycles in order`() {
        assertEquals(PowerAveraging.SMOOTHED_3S, PowerAveraging.INSTANT.next())
        assertEquals(PowerAveraging.SMOOTHED_5S, PowerAveraging.SMOOTHED_3S.next())
        assertEquals(PowerAveraging.SMOOTHED_10S, PowerAveraging.SMOOTHED_5S.next())
        assertEquals(PowerAveraging.SMOOTHED_30S, PowerAveraging.SMOOTHED_10S.next())
        assertEquals(PowerAveraging.INSTANT, PowerAveraging.SMOOTHED_30S.next())
    }
}

