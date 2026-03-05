package debug_login

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
    private const val KEY_USER_ID = "user_id"

    // 获取SP实例
    private fun getSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存登录状态
     * @param context 上下文
     * @param isLogin 是否登录
     * @param userId 用户ID（可选）
     */
    fun saveLoginStatus(context: Context, isLogin: Boolean, userId: String = "") {
        val editor = getSP(context).edit()
        editor.putBoolean(KEY_IS_LOGIN, isLogin)
        editor.putString(KEY_USER_ID, userId)
        editor.apply() // 异步提交，不阻塞主线程
    }

    /**
     * 检查是否已登录
     * @return true=已登录，false=未登录
     */
    fun isLogin(context: Context): Boolean {
        return getSP(context).getBoolean(KEY_IS_LOGIN, false)
    }

    /**
     * 退出登录（清除登录状态）
     */
    fun logout(context: Context) {
        saveLoginStatus(context, false, "")
    }

}