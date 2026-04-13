package com.example.common.login

import android.content.Context
import android.content.SharedPreferences

object GuestStateHolder {
    private const val PREFS_NAME = "guest_state"
    private const val KEY_IS_GUEST = "is_guest"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setGuest(value: Boolean) {
        prefs?.edit()?.putBoolean(KEY_IS_GUEST, value)?.apply()
    }

    fun isGuest(): Boolean = prefs?.getBoolean(KEY_IS_GUEST, false) ?: false
}
