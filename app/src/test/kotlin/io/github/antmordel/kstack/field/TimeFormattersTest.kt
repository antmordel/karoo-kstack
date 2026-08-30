package io.github.antmordel.kstack.field

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TimeFormattersTest {

    private val duration = durationFormatter(Locale.UK)

    @Test
    fun `under an hour the hour is dropped`() {
        // A stacked field cannot spare the width for a leading zero hour that means nothing.
        assertEquals("12:34", duration.format(754.0, null))
    }

    @Test
    fun `over an hour the hour appears`() {
        assertEquals("1:15:21", duration.format(4521.0, null))
    }

    @Test
    fun `seconds are always two digits`() {
        assertEquals("0:05", duration.format(5.0, null))
    }

    @Test
    fun `a duration of zero reads as zero rather than empty`() {
        assertEquals("0:00", duration.format(0.0, null))
    }

    @Test
    fun `a negative duration is floored rather than rendered with a sign`() {
        assertEquals("0:00", duration.format(-4.0, null))
    }
}
