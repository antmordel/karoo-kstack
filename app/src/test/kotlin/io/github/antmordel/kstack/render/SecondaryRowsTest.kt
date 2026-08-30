package io.github.antmordel.kstack.render

import io.github.antmordel.kstack.settings.SecondaryLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryRowsTest {

    @Test
    fun `side by side pairs secondaries across a row`() {
        assertEquals(
            listOf(listOf("avg", "max")),
            listOf("avg", "max").inSecondaryRows(SecondaryLayout.SIDE_BY_SIDE),
        )
    }

    @Test
    fun `stacked gives every secondary its own row`() {
        assertEquals(
            listOf(listOf("avg"), listOf("max")),
            listOf("avg", "max").inSecondaryRows(SecondaryLayout.STACKED),
        )
    }

    @Test
    fun `an odd secondary sits alone on the last row rather than being dropped`() {
        // The secondary list is open-ended, so three rows must survive the pairing.
        assertEquals(
            listOf(listOf("avg", "max"), listOf("lap")),
            listOf("avg", "max", "lap").inSecondaryRows(SecondaryLayout.SIDE_BY_SIDE),
        )
    }

    @Test
    fun `a field with no secondaries produces no rows`() {
        assertEquals(emptyList<List<String>>(), emptyList<String>().inSecondaryRows(SecondaryLayout.SIDE_BY_SIDE))
    }
}
