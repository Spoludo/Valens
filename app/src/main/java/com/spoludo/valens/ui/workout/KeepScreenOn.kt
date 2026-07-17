package com.spoludo.valens.ui.workout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current

    DisposableEffect(view, enabled) {
        val previous = view.keepScreenOn
        view.keepScreenOn = enabled || previous

        onDispose {
            view.keepScreenOn = previous
        }
    }
}
