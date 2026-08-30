package io.github.antmordel.kstack.extension

import io.github.antmordel.kstack.field.Definitions
import io.github.antmordel.kstack.field.KarooStreamSource
import io.github.antmordel.kstack.field.StackedDataType
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.github.antmordel.kstack.BuildConfig
import io.hammerhead.karooext.extension.KarooExtension
import timber.log.Timber

/**
 * Entry point Karoo OS binds to. Owns the connection to [KarooSystemService] and the list of
 * data types the extension publishes.
 *
 * [types] must stay in sync with the `DataType` entries in `res/xml/extension_info.xml`: a type
 * listed in one and not the other never reaches the field picker.
 */
class KStackExtension : KarooExtension(EXTENSION_ID, EXTENSION_VERSION) {

    private lateinit var karooSystem: KarooSystemService

    override val types: List<DataTypeImpl> by lazy {
        val streams = KarooStreamSource(karooSystem)
        Definitions.all.map { StackedDataType(extension, it, streams) }
    }

    override fun onCreate() {
        super.onCreate()
        // karoo-ext logs through Timber, and Timber discards everything until a tree is planted.
        // Without this the extension is silent in logcat, which makes on-device debugging blind.
        if (BuildConfig.DEBUG && Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
        karooSystem = KarooSystemService(this)
        karooSystem.connect { connected ->
            Timber.i("Karoo system connected: %b", connected)
        }
    }

    override fun onDestroy() {
        karooSystem.disconnect()
        super.onDestroy()
    }

    private companion object {
        /** Must match the `id` attribute in `extension_info.xml` and contain no '.'. */
        const val EXTENSION_ID = "kstack"
        const val EXTENSION_VERSION = "1.0"
    }
}
