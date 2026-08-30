package io.github.antmordel.kstack.render

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ColorFilter
import androidx.glance.background
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import io.github.antmordel.kstack.R
import io.github.antmordel.kstack.field.StackedFieldDefinition
import io.github.antmordel.kstack.field.StackedFieldState
import io.github.antmordel.kstack.settings.FieldSettings
import io.github.antmordel.kstack.settings.SecondaryLayout
import io.github.antmordel.kstack.settings.ZoneColorMode
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.ViewConfig
import androidx.compose.ui.unit.dp as composeDp

/** Icon squares scale with the number beside them. */
private const val ICON_RATIO = 0.8f

/**
 * Labels stay small and light while their value grows: the number is what a rider reads at a
 * glance, and `avg` only has to be identifiable. Lowered whenever the values grow, so the label
 * keeps the absolute size it had rather than growing with them.
 */
private const val LABEL_RATIO = 0.53f

/** A suffix is punctuation on the number, not a second number: it stays clearly subordinate. */
private const val SUFFIX_RATIO = 0.55f

/** The two pairs on a row never touch, however wide their numbers get. */
private const val MINIMUM_PAIR_GAP = 8

/** Side by side, two secondaries cost one row instead of two — height the primary gets to keep. */
private const val SECONDARIES_PER_ROW = 2

/**
 * One secondary as it will actually be drawn, formatted.
 *
 * Sizing needs the finished text: a value's width is a property of the string, not of the number.
 */
internal data class DrawnValue(val label: String, val value: String, val suffix: String) {
    /**
     * Character widths for this pair, with the smaller label and suffix counted at their own
     * scale, plus a couple for the gap that follows the label.
     */
    fun widthUnits(): Int =
        (label.length * LABEL_RATIO + value.length + suffix.length * SUFFIX_RATIO + 1f).toInt() + 1
}

/**
 * Groups secondaries into the rows the rider asked for. Pure, so the arrangement is testable
 * without composing a view.
 */
internal fun <T> List<T>.inSecondaryRows(layout: SecondaryLayout): List<List<T>> =
    chunked(if (layout == SecondaryLayout.SIDE_BY_SIDE) SECONDARIES_PER_ROW else 1)

/**
 * Content colour follows the device's day/night setting.
 *
 * Glance defaults text to black. A Karoo in night mode draws fields on black, so the default makes
 * every number invisible — which is exactly what the first on-device run showed.
 */
@Composable
private fun contentColor(): androidx.glance.unit.ColorProvider {
    val uiMode = LocalContext.current.resources.configuration.uiMode
    val night = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    return ColorProvider(if (night) Color.White else Color.Black)
}

/**
 * Draws any stacked field: the primary value large with the metric icon, then one small labeled
 * row per secondary.
 *
 * Nothing here knows which metric it is drawing. Everything metric-specific — which streams, which
 * labels, which icon, how to format — arrives on [definition].
 */
@Composable
fun StackedFieldView(
    definition: StackedFieldDefinition,
    state: StackedFieldState,
    profile: UserProfile?,
    config: ViewConfig,
    settings: FieldSettings,
) {
    val density = LocalContext.current.resources.displayMetrics.density
    val suffix = definition.suffixRes?.let { LocalContext.current.getString(it) }.orEmpty()
    // Formatted before sizing, because how wide a row is depends on the text it ended up with.
    val secondaryRows = state.secondaries
        .map { secondary ->
            DrawnValue(
                label = secondary.labelRes?.let { LocalContext.current.getString(it) }.orEmpty(),
                value = definition.formatter.formatOrDash(secondary.value, profile),
                suffix = suffix,
            )
        }
        .inSecondaryRows(settings.secondaryLayout)
    val primaryText = definition.formatter.formatOrDash(state.primary, profile)
    val sizes = stackedTextSizes(
        config = config,
        secondaryRowCount = secondaryRows.size,
        primaryWidth = primaryText.length + (suffix.length * SUFFIX_RATIO).toInt(),
        widestSecondaryRow = secondaryRows.maxOfOrNull { row -> row.sumOf { it.widthUnits() } } ?: 0,
        density = density,
    )
    val horizontalAlignment = config.alignment.toGlanceAlignment()
    // Null whenever there is no zone to colour by, which is also what a field with colouring off
    // and a field on a metric without zones both look like.
    val zoneColor = state.zone?.let { zoneColor(it, state.zoneCount) }
    val background = zoneColor.takeIf { settings.zoneColorMode == ZoneColorMode.FIELD }
    val contentColor = background?.let { ColorProvider(contentColorOn(it)) } ?: contentColor()
    val iconColor = when (settings.zoneColorMode) {
        ZoneColorMode.NONE, ZoneColorMode.FIELD -> contentColor
        ZoneColorMode.ICON -> zoneColor?.let { ColorProvider(it) } ?: contentColor
    }
    // Verbose, and only reachable in debug builds where a Timber tree is planted. The layout
    // constants here were set against a real Karoo, and this is how they were read back.
    timber.log.Timber.v(
        "render %s view=%s grid=%s textSize=%s density=%s sizes=%s primary=%s secondaries=%s zone=%s/%s mode=%s",
        definition.fieldId,
        config.viewSize,
        config.gridSize,
        config.textSize,
        density,
        sizes,
        primaryText,
        secondaryRows.flatten().map { it.value },
        state.zone,
        state.zoneCount,
        settings.zoneColorMode,
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .let { if (background != null) it.background(ColorProvider(background)) else it }
            // More room on the left than the right: the small labels start there, and hard against
            // the boundary they read as clipped.
            .padding(
                start = 14.composeDp,
                top = 2.composeDp,
                end = 10.composeDp,
                bottom = 2.composeDp,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(definition.iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconColor),
                modifier = GlanceModifier.size((sizes.primarySp * ICON_RATIO).composeDp),
            )
            Spacer(modifier = GlanceModifier.size(4.composeDp))
            Text(
                text = primaryText,
                style = TextStyle(
                    fontSize = sizes.primarySp.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                ),
            )
            Suffix(definition.suffixRes, sizes.primarySp, contentColor)
        }

        secondaryRows.forEach { row ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEachIndexed { index, drawn ->
                    if (index > 0) {
                        // A weighted spacer alone collapses to nothing once the row is full, which
                        // is how `avg 27.0max 54.0` ran together on a Karoo.
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Spacer(modifier = GlanceModifier.size(MINIMUM_PAIR_GAP.composeDp))
                    }
                    Text(
                        text = drawn.label,
                        style = TextStyle(
                            fontSize = (sizes.secondarySp * LABEL_RATIO).sp,
                            color = contentColor,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.size(3.composeDp))
                    Text(
                        text = drawn.value,
                        style = TextStyle(
                            fontSize = sizes.secondarySp.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        ),
                    )
                    Suffix(definition.suffixRes, sizes.secondarySp, contentColor)
                }
            }
        }
    }
}

/** Nothing at all for a definition that names no suffix, which is most of them. */
@Composable
private fun Suffix(@StringRes suffixRes: Int?, valueSp: Float, color: ColorProvider) {
    if (suffixRes == null) return
    Text(
        text = LocalContext.current.getString(suffixRes),
        style = TextStyle(
            fontSize = (valueSp * SUFFIX_RATIO).sp,
            fontWeight = FontWeight.Bold,
            color = color,
        ),
    )
}

@Composable
private fun io.github.antmordel.kstack.field.ValueFormatter.formatOrDash(
    value: Double?,
    profile: UserProfile?,
): String = value?.let { format(it, profile) } ?: LocalContext.current.getString(R.string.value_missing)

private fun ViewConfig.Alignment.toGlanceAlignment(): Alignment.Horizontal = when (this) {
    ViewConfig.Alignment.LEFT -> Alignment.Start
    ViewConfig.Alignment.CENTER -> Alignment.CenterHorizontally
    ViewConfig.Alignment.RIGHT -> Alignment.End
}
