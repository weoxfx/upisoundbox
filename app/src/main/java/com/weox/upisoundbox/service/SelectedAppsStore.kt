package com.weox.upisoundbox.service

import android.content.Context

/**
 * Tracks which UPI apps the user selected during onboarding
 * (e.g. they only use PhonePe, so we should ignore GPay/Paytm entirely
 * even if those apps happen to be installed on the device).
 */
object SelectedAppsStore {
    private const val PREFS_NAME = "upi_sound_box_prefs"
    private const val KEY_SELECTED = "selected_packages"

    fun getSelected(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SELECTED, emptySet()) ?: emptySet()
    }

    fun setSelected(context: Context, packages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SELECTED, packages).apply()
    }
}
