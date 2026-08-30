package io.github.antmordel.kstack.field

import io.hammerhead.karooext.models.UserProfile
import java.util.Locale

/** Karoo streams speed in metres per second, whatever the rider's profile says. */
private const val KMH_PER_MS = 3.6
private const val MPH_PER_MS = 2.2369362920544

/**
 * Speed in the rider's configured unit system, to one decimal.
 *
 * Formatting lives on the definition rather than in the renderer, so a field that carries units
 * converts here and one that does not uses [ValueFormatter.Plain].
 *
 * Falls back to metric while the profile is still in flight. It arrives within moments of the
 * field starting, and a number in the wrong system for that moment beats a blank field.
 *
 * @param locale decides the decimal separator. Injectable so tests do not depend on the machine.
 */
fun speedFormatter(locale: Locale = Locale.getDefault()): ValueFormatter =
    ValueFormatter { metresPerSecond, profile ->
        val factor = if (profile.isImperialDistance()) MPH_PER_MS else KMH_PER_MS
        String.format(locale, "%.1f", metresPerSecond * factor)
    }

private fun UserProfile?.isImperialDistance() =
    this?.preferredUnit?.distance == UserProfile.PreferredUnit.UnitType.IMPERIAL
