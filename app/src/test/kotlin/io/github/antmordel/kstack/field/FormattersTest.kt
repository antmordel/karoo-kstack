package io.github.antmordel.kstack.field

import io.hammerhead.karooext.models.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

private fun profile(distance: UserProfile.PreferredUnit.UnitType) = UserProfile(
    weight = 70f,
    preferredUnit = UserProfile.PreferredUnit(
        distance = distance,
        elevation = distance,
        temperature = distance,
        weight = distance,
    ),
    maxHr = 190,
    restingHr = 50,
    heartRateZones = emptyList(),
    ftp = 250,
    powerZones = emptyList(),
)

class SpeedFormatterTest {

    private val formatter = speedFormatter(Locale.US)

    @Test
    fun `metric profile reads metres per second as kilometres per hour`() {
        val text = formatter.format(10.0, profile(UserProfile.PreferredUnit.UnitType.METRIC))

        assertEquals("36.0", text)
    }

    @Test
    fun `imperial profile reads metres per second as miles per hour`() {
        val text = formatter.format(10.0, profile(UserProfile.PreferredUnit.UnitType.IMPERIAL))

        assertEquals("22.4", text)
    }

    @Test
    fun `an unknown profile falls back to metric rather than blanking the field`() {
        val text = formatter.format(10.0, profile = null)

        assertEquals("36.0", text)
    }

    @Test
    fun `a stationary rider reads zero, not a dash`() {
        val text = formatter.format(0.0, profile(UserProfile.PreferredUnit.UnitType.METRIC))

        assertEquals("0.0", text)
    }

    @Test
    fun `the locale decides the decimal separator`() {
        val spanish = speedFormatter(Locale.forLanguageTag("es-ES"))

        val text = spanish.format(10.0, profile(UserProfile.PreferredUnit.UnitType.METRIC))

        assertEquals("36,0", text)
    }

    @Test
    fun `unitless fields are unaffected by the profile`() {
        val metric = ValueFormatter.Plain.format(142.0, profile(UserProfile.PreferredUnit.UnitType.METRIC))
        val imperial = ValueFormatter.Plain.format(142.0, profile(UserProfile.PreferredUnit.UnitType.IMPERIAL))

        assertEquals(metric, imperial)
        assertEquals("142", metric)
    }
}
