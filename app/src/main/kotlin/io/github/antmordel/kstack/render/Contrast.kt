package io.github.antmordel.kstack.render

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Black or white, whichever reads better on [background].
 *
 * Text colour normally follows the device's day/night setting, but on a zone-coloured field the
 * background is no longer the device's — amber needs black text in either mode, and the darkest
 * zone needs white in both.
 *
 * Uses the WCAG relative luminance and contrast ratio rather than a brightness threshold, because
 * a threshold picks wrongly for saturated mid-tones like the amber and green stops.
 */
fun contentColorOn(background: Color): Color =
    if (contrastRatio(Color.White, background) >= contrastRatio(Color.Black, background)) {
        Color.White
    } else {
        Color.Black
    }

/** WCAG 2.1 contrast ratio: 1.0 for identical colours, 21.0 for black against white. */
fun contrastRatio(foreground: Color, background: Color): Double {
    val lighter = max(relativeLuminance(foreground), relativeLuminance(background))
    val darker = min(relativeLuminance(foreground), relativeLuminance(background))
    return (lighter + 0.05) / (darker + 0.05)
}

private fun relativeLuminance(color: Color): Double =
    0.2126 * linearize(color.red) + 0.7152 * linearize(color.green) + 0.0722 * linearize(color.blue)

private fun linearize(channel: Float): Double {
    val value = channel.toDouble()
    return if (value <= 0.03928) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
}
