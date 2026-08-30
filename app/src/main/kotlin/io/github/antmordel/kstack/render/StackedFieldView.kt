package io.github.antmordel.kstack.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.antmordel.kstack.R
import io.github.antmordel.kstack.field.StackedFieldDefinition
import io.github.antmordel.kstack.field.StackedFieldState
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.ViewConfig
import androidx.compose.ui.unit.dp as composeDp

/** Icon squares scale with the number beside them. */
private const val ICON_RATIO = 0.8f

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
) {
    val density = LocalContext.current.resources.displayMetrics.density
    val sizes = stackedTextSizes(config, state.secondaries.size, density)
    val horizontalAlignment = config.alignment.toGlanceAlignment()

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(definition.iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size((sizes.primarySp * ICON_RATIO).composeDp),
            )
            Spacer(modifier = GlanceModifier.size(4.composeDp))
            Text(
                text = definition.formatter.formatOrDash(state.primary, profile),
                style = TextStyle(fontSize = sizes.primarySp.sp, fontWeight = FontWeight.Bold),
            )
        }

        state.secondaries.forEach { secondary ->
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = secondary.labelRes?.let { LocalContext.current.getString(it) }.orEmpty(),
                    style = TextStyle(fontSize = sizes.secondarySp.sp),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = definition.formatter.formatOrDash(secondary.value, profile),
                    style = TextStyle(fontSize = sizes.secondarySp.sp, fontWeight = FontWeight.Bold),
                )
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
