package io.github.antmordel.kstack.ui

import android.app.Activity
import android.os.Bundle
import io.github.antmordel.kstack.R

/**
 * Gives KStack an entry in the Karoo main menu.
 *
 * Karoo tells riders to open an extension once after installing it, and the fields themselves are
 * added from the ride profile editor. This screen says so and gets out of the way — it is not a
 * settings surface, which is an explicit non-goal.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Both the button and the background dismiss: on a small gloved touchscreen a rider
        // should not have to find a particular target to get out.
        findViewById<android.view.View>(R.id.close).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.root).setOnClickListener { finish() }
    }
}
