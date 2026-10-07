package com.debroglie.mimore.haptics

import android.view.HapticFeedbackConstants
import android.view.View

class HapticManager {
    fun tap(view: View, enabled: Boolean, level: Int) {
        if (!enabled) return
        val constant = when (level.coerceIn(1, 3)) {
            1 -> HapticFeedbackConstants.KEYBOARD_TAP
            2 -> HapticFeedbackConstants.KEYBOARD_TAP
            else -> HapticFeedbackConstants.CONTEXT_CLICK
        }
        view.performHapticFeedback(constant)
    }

    fun confirm(view: View, enabled: Boolean, level: Int) {
        if (!enabled) return
        view.performHapticFeedback(if (level >= 2) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun delete(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun clear(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun error(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    fun selection(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}
