package com.conreo.couchytv

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * The remote's Google Play button (KEYCODE_BUTTON_3) is claimed by the system
 * before any app sees it, so a plain key handler can't remap it. An
 * accessibility key filter runs first and may swallow it.
 */
class KeyRemapService : AccessibilityService() {

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_BUTTON_3) return false
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            Actions.launchApp(this, YOUTUBE_PKG)
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    private companion object {
        const val YOUTUBE_PKG = "com.google.android.youtube.tv"
    }
}
