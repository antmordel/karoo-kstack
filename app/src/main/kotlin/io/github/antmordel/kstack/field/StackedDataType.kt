package io.github.antmordel.kstack.field

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.github.antmordel.kstack.render.StackedFieldView
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
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
) : DataTypeImpl(extension, definition.fieldId) {

    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val profiles = streams.userProfile()
            .map<UserProfile, UserProfile?> { it }
            .onStart { emit(null) }

        val job = combine(streams.stackedFieldStates(definition), profiles) { state, profile ->
            state to profile
        }.onEach { (state, profile) ->
            // GlanceRemoteViews.compose suspends, so composition lives on this collector.
            val result = glance.compose(context, DpSize.Unspecified) {
                StackedFieldView(definition, state, profile, config)
            }
            emitter.updateView(result.remoteViews)
        }.launchIn(CoroutineScope(Dispatchers.IO))

        emitter.setCancellable { job.cancel() }
    }
}
