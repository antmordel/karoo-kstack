package io.github.antmordel.kstack.field

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/** Long enough to cover any sweep the definitions use, so a test never depends on its length. */
private val SWEEP = 0..119

class PreviewSweepTest {

    @Test
    fun `the number a rider sees in the editor changes as they look at it`() {
        // Every field, formatted the way it is drawn: an amplitude that moves the raw value but
        // rounds to the same text would still look frozen on the device.
        Definitions.all.forEach { definition ->
            val drawn = SWEEP.map { step ->
                definition.formatter.format(definition.previewState(step).primary!!, null)
            }

            assertTrue("${definition.fieldId} previews as ${drawn.first()} throughout",
                drawn.toSet().size > 1)
        }
    }

    @Test
    fun `the sweep comes back to where it started rather than running away`() {
        Definitions.all.forEach { definition ->
            val first = definition.previewState(0).primary!!
            val afterSeveralSweeps = definition.previewState(SWEEP.last + 1).primary!!

            assertEquals(definition.fieldId, first, afterSeveralSweeps, 0.001)
        }
    }

    @Test
    fun `a preview stays a plausible reading for its metric`() {
        // A speed that previews at 90 km/h or a cadence at 200 advertises the field as broken.
        Definitions.all.forEach { definition ->
            val highest = SWEEP.maxOf { definition.previewState(it).primary!! }

            assertTrue("${definition.fieldId} reaches $highest",
                highest < definition.primary.previewValue * 1.2)
        }
    }

    @Test
    fun `averages and maxima hold still while the current value moves`() {
        val moving = SWEEP.map { Definitions.HeartRate.previewState(it) }

        assertEquals(1, moving.map { it.secondaries.map(SecondaryState::value) }.toSet().size)
        assertTrue(moving.map { it.primary }.toSet().size > 1)
    }

    @Test
    fun `a coloured field previews every colour it can show`() {
        val zones = SWEEP.map { Definitions.HeartRate.previewState(it).zone }.toSet()
        val zoneCount = Definitions.HeartRate.previewState(0).zoneCount

        assertEquals((0 until zoneCount).toSet(), zones)
    }

    @Test
    fun `a field with no zones previews none`() {
        val state = Definitions.Speed.previewState(7)

        assertNull(state.zone)
        assertEquals(0, state.zoneCount)
    }

    @Test
    fun `a row that is really streaming is never overwritten by the sweep`() {
        val streaming = StackedFieldState(
            primary = 151.0,
            secondaries = Definitions.HeartRate.secondaries.map { SecondaryState(it.labelRes, null) },
        )

        val shown = SWEEP.map { Definitions.HeartRate.withPreviewFallback(streaming, it) }

        assertEquals(setOf(151.0), shown.map { it.primary }.toSet())
        assertTrue("the empty rows should still be filled in",
            shown.all { row -> row.secondaries.all { it.value != null } })
    }

    @Test
    fun `a real zone wins over the swept one`() {
        val streaming = StackedFieldState(
            primary = null,
            secondaries = Definitions.HeartRate.secondaries.map { SecondaryState(it.labelRes, null) },
            zone = 1,
            zoneCount = 7,
        )

        val shown = SWEEP.map { Definitions.HeartRate.withPreviewFallback(streaming, it) }

        assertEquals(setOf(1), shown.map { it.zone }.toSet())
        assertEquals(setOf(7), shown.map { it.zoneCount }.toSet())
    }

    @Test
    fun `the lap time in the editor counts forwards`() {
        val formatter = durationFormatter(Locale.UK)
        val early = formatter.format(Definitions.Time.previewState(1).primary!!, null)
        val later = formatter.format(Definitions.Time.previewState(9).primary!!, null)

        assertNotEquals(early, later)
        assertTrue("$early should precede $later",
            Definitions.Time.previewState(1).primary!! < Definitions.Time.previewState(9).primary!!)
    }
}
