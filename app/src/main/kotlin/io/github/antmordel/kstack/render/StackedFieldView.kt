package io.github.antmordel.kstack.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ColorFilter
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
 * glance, and `avg` only has to be identifiable. Chosen so the label keeps roughly the size it had
 * when the secondary values were smaller.
 */
private const val LABEL_RATIO = 0.62f

/** Side by side, two secondaries cost one row instead of two — height the primary gets to keep. */
private const val SECONDARIES_PER_ROW = 2

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
    val secondaryRows = state.secondaries.inSecondaryRows(settings.secondaryLayout)
    val sizes = stackedTextSizes(config, secondaryRows.size, density)
    val horizontalAlignment = config.alignment.toGlanceAlignment()
    val contentColor = contentColor()
    val iconColor = when (settings.zoneColorMode) {
        ZoneColorMode.NONE -> contentColor
        ZoneColorMode.ICON -> state.zone
            ?.let { ColorProvider(zoneColor(it, state.zoneCount)) }
            ?: contentColor
    }
    // Verbose, and only reachable in debug builds where a Timber tree is planted. The layout
    // constants here were set against a real Karoo, and this is how they were read back.
    timber.log.Timber.v(
        "render %s density=%s sizes=%s primary=%s secondaries=%s",
        definition.fieldId,
        density,
        sizes,
        definition.formatter.formatOrDash(state.primary, profile),
        state.secondaries.map { definition.formatter.formatOrDash(it.value, profile) },
    )

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(horizontal = 10.composeDp, vertical = 2.composeDp),
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
                text = definition.formatter.formatOrDash(state.primary, profile),
                style = TextStyle(
                    fontSize = sizes.primarySp.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                ),
            )
        }

        secondaryRows.forEach { row ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEachIndexed { index, secondary ->
                    if (index > 0) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                    Text(
                        text = secondary.labelRes?.let { LocalContext.current.getString(it) }
                            .orEmpty(),
                        style = TextStyle(
                            fontSize = (sizes.secondarySp * LABEL_RATIO).sp,
                            color = contentColor,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.size(3.composeDp))
                    Text(
                        text = definition.formatter.formatOrDash(secondary.value, profile),
                        style = TextStyle(
                            fontSize = sizes.secondarySp.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        ),
                    )
                }
            }
        }
    }
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
