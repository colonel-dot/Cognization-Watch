package com.example.common.login.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * 登录状态管理工具类
 */
object LoginStatusManager {
    // SP文件名
    private const val SP_NAME = "login_status"
    // 存储的Key
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
        editor.apply() // 异步提交，不阻塞主线程
    }

    /**
     * 检查是否已登录
     * @return true=已登录，false=未登录
     */
    fun isLogin(context: Context): Boolean {
        return getLoginSP(context).getBoolean(KEY_IS_LOGIN, false)
    }

    /**
     * 退出登录（清除登录状态） 
     */
    fun logout(context: Context) {
        saveLoginStatus(context, false, "")
    }

    fun isElder(context: Context): Boolean {
        if (!isLogin(context)) return false;
        return getLoginSP(context).getString(KEY_IDENTITY, "") == "elder"
    }

}