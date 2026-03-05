package bind_device

import android.content.Context
import android.content.SharedPreferences

private const val SP_NAME = "login_status"

object BindStatusManager {
    private var isBound: Boolean = false
    private var boundUsername: String? = null
    private const val KEY_IS_BIND = "is_bound"

    private fun getSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }


    fun bindDevice(context: Context, musername: String, otherusername: String) {
        val editor = getSP(context).edit()
        isBound = true
        editor.let {
            it.putBoolean(KEY_IS_BIND, true)
            it.putString("bound_username", otherusername)
            it.apply()
        }
        boundUsername = otherusername
    }

    fun unbindDevice() {
        isBound = false
        boundUsername = null
    }

    fun isBound(context: Context): Boolean {
        return getSP(context).getBoolean(KEY_IS_BIND, false)
    }

    fun getBindStatus(): Pair<Boolean, String?> {
        return Pair(isBound, boundUsername)
    }
}