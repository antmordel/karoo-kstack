package io.github.antmordel.kstack.field

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.github.antmordel.kstack.render.StackedFieldView
import io.github.antmordel.kstack.settings.SettingsStore
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * The one graphical data type. Every KStack field is an instance of this class holding a different
 * [definition] — there is no per-metric subclass, and adding a metric never reaches this file.
 *
 * Publishes no numeric stream of its own: a stacked field re-presents data types Karoo already
 * owns, so [DataTypeImpl.startStream] keeps its default.
 */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class StackedDataType(
    extension: String,
    private val definition: StackedFieldDefinition,
    private val streams: StreamSource,
    private val settingsStore: SettingsStore,
) : DataTypeImpl(extension, definition.fieldId) {

    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        // Karoo draws its own icon-and-name header above a graphical field by default. A stacked
        // field carries its own icon and needs the whole box: with the header on, the rows are
        // sized for height the field does not actually have and get clipped.
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        val profiles = streams.userProfile()
            .map<UserProfile, UserProfile?> { it }
            .onStart { emit(null) }

        val settings = settingsStore.settings(definition.fieldId)

        // The editor feeds the real data types, so a preview follows them and falls back to the
        // definition's own numbers only for rows that have nothing yet. Those fall back to a value
        // that sweeps, so an unpaired sensor previews as a field that is working rather than one
        // that is stuck.
        val states = if (config.preview) {
            combine(streams.stackedFieldStates(definition), previewSweep()) { state, step ->
                definition.withPreviewFallback(state, step)
            }
        } else {
            streams.stackedFieldStates(definition)
        }

        val job = combine(
            states,
            profiles,
            settings,
        ) { state, profile, fieldSettings ->
            Triple(state, profile, fieldSettings)
        }.onEach { (state, profile, fieldSettings) ->
            // GlanceRemoteViews.compose suspends, so composition lives on this collector.
            val result = glance.compose(context, DpSize.Unspecified) {
                StackedFieldView(definition, state, profile, config, fieldSettings)
            }
            emitter.updateView(result.remoteViews)
        }.launchIn(CoroutineScope(Dispatchers.IO))

        emitter.setCancellable { job.cancel() }
    }

}

/** Karoo redraws its own fields about once a second; a preview that moves faster looks frantic. */
private const val PREVIEW_TICK_MS = 1_000L

/** Counts the seconds a data page has been open, which is all a preview needs to animate. */
private fun previewSweep(): Flow<Int> = flow {
    var step = 0
    while (true) {
        emit(step++)
        delay(PREVIEW_TICK_MS)
    }
}
