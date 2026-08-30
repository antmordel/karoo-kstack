package io.github.antmordel.kstack.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import io.github.antmordel.kstack.R
import io.github.antmordel.kstack.field.Definitions
import io.github.antmordel.kstack.field.StackedFieldDefinition
import io.github.antmordel.kstack.settings.SecondaryLayout
import io.github.antmordel.kstack.settings.SettingsStore
import io.github.antmordel.kstack.settings.SharedPreferencesSettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * KStack's entry in the Karoo main menu, and its settings screen.
 *
 * Builds one section per entry in [Definitions.all]. Nothing here knows which metric a section is
 * for: the name comes from the definition, and so will every setting added later.
 */
class MainActivity : Activity() {

    private lateinit var settings: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SharedPreferencesSettingsStore(this)
        setContentView(R.layout.activity_main)

        val container = findViewById<ViewGroup>(R.id.fields)
        Definitions.all.forEach { definition ->
            container.addView(fieldSection(definition, container))
        }

        findViewById<Button>(R.id.close).setOnClickListener { finish() }
    }

    private fun fieldSection(definition: StackedFieldDefinition, parent: ViewGroup): View {
        val section = LayoutInflater.from(this).inflate(R.layout.settings_field, parent, false)
        section.findViewById<TextView>(R.id.field_name).setText(definition.nameRes)

        val choice = section.findViewById<RadioGroup>(R.id.layout_choice)
        // One read of a SharedPreferences-backed flow on an already-loaded file. The screen has no
        // other state to restore, so an async load would only flash the wrong radio button first.
        val current = runBlocking { settings.settings(definition.fieldId).first() }
        choice.check(current.secondaryLayout.radioId())
        choice.setOnCheckedChangeListener { _, checkedId ->
            settings.setSecondaryLayout(definition.fieldId, checkedId.toLayout())
        }
        return section
    }

    private fun SecondaryLayout.radioId() = when (this) {
        SecondaryLayout.SIDE_BY_SIDE -> R.id.layout_side_by_side
        SecondaryLayout.STACKED -> R.id.layout_stacked
    }

    private fun Int.toLayout() = when (this) {
        R.id.layout_stacked -> SecondaryLayout.STACKED
        else -> SecondaryLayout.SIDE_BY_SIDE
    }
}
