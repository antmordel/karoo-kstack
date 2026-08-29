package io.github.antmordel.kstack.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.hammerhead.karooext.models.ViewConfig

/**
 * Minimal field content. Sizes off [ViewConfig.textSize] so it is legible at every grid size
 * without measuring the laid-out view — the same input the real stacked renderer will use.
 */
@Composable
fun PlaceholderView(config: ViewConfig) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "KStack",
            style = TextStyle(fontSize = config.textSize.sp, fontWeight = FontWeight.Bold),
        )
    }
}
