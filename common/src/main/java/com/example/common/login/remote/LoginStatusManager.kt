package com.example.common.login.remote

import android.content.Context
import android.content.SharedPreferences

object LoginStatusManager {
    private const val SP_NAME = "login_status"

    private const val KEY_IS_LOGIN = "is_login"
    private const val KEY_USER_ID = "username"
    private const val KEY_IDENTITY = "identity"

    // 获取SP实例
    private fun getLoginSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    fun getLoggedInUserId(context: Context): String {
        return getLoginSP(context).getString(KEY_USER_ID, "") ?: ""
    }

    fun saveLoginStatus(context: Context, isLogin: Boolean, userId: String = "", identity: String = "") {
        val editor = getLoginSP(context).edit()
        editor.putBoolean(KEY_IS_LOGIN, isLogin)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_IDENTITY, identity)
        editor.apply()
    }

    fun isLogin(context: Context): Boolean {
        return getLoginSP(context).getBoolean(KEY_IS_LOGIN, false)
    }

    fun logout(context: Context) {
        saveLoginStatus(context, false, "")
    }

    fun isElder(context: Context): Boolean {
        if (!isLogin(context)) return false
        return getLoginSP(context).getString(KEY_IDENTITY, "") == "elder"
    }

}