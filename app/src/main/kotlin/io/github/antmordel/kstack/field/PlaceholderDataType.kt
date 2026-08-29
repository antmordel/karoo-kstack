package io.github.antmordel.kstack.field

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.github.antmordel.kstack.render.PlaceholderView
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ViewConfig

/**
 * Proves the extension is installed and that Karoo can render one of its graphical fields.
 *
 * Scaffolding only: the real definition-driven data type replaces this once the field catalog
 * lands. It streams nothing, so [DataTypeImpl.startStream] is left at its default.
 */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class PlaceholderDataType(extension: String) : DataTypeImpl(extension, TYPE_ID) {

    private val glance = GlanceRemoteViews()

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val result = glance.compose(context, DpSize.Unspecified) {
            PlaceholderView(config)
        }
        emitter.updateView(result.remoteViews)
    }

    companion object {
        /** Must match a `DataType typeId` in `extension_info.xml`. */
        const val TYPE_ID = "placeholder-stack"
    }
}
