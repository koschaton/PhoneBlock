package com.prefixblocker.app.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefix_blocker_prefs", Context.MODE_PRIVATE)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var blockPrivateNumbers: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_PRIVATE, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_PRIVATE, value).apply()

    var hideFromSystemCallLog: Boolean
        get() = prefs.getBoolean(KEY_HIDE_SYSTEM_LOG, false)
        set(value) = prefs.edit().putBoolean(KEY_HIDE_SYSTEM_LOG, value).apply()

    companion object {
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_BLOCK_PRIVATE = "block_private_numbers"
        private const val KEY_HIDE_SYSTEM_LOG = "hide_from_system_call_log"
    }
}
