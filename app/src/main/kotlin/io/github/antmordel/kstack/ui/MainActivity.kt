package io.github.antmordel.kstack.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import io.github.antmordel.kstack.R

/**
 * Gives KStack an entry in the Karoo main menu.
 *
 * Karoo's own documentation tells riders to open an extension once after installing it, and the
 * fields are added from the ride profile editor rather than here. This screen exists to satisfy
 * that step and to say so — it is not a settings surface, which is an explicit non-goal.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            TextView(this).apply {
                text = getString(R.string.main_screen_body)
                gravity = Gravity.CENTER
                setPadding(PADDING_PX, PADDING_PX, PADDING_PX, PADDING_PX)
            },
        )
    }

    private companion object {
        const val PADDING_PX = 32
    }
}
