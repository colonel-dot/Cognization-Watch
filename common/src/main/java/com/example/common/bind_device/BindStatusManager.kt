package com.example.common.bind_device

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

private const val SP_NAME = "bind_status"
private const val TAG = "BindStatusManager"
object BindStatusManager {
    private var isBound: Boolean = false
    private var boundUsername: String? = null
    private var bindRemark: String? = null
    private const val KEY_IS_BIND = "is_bound"
    private const val KEY_BIND_REMARK = "bind_remark"

    private fun getSP(context: Context): SharedPreferences {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        val sp = getSP(context)
        isBound = sp.getBoolean(KEY_IS_BIND, false)
        boundUsername = sp.getString("bound_username", null)
        bindRemark = sp.getString(KEY_BIND_REMARK, null)
    }

    fun getBindRemark(context: Context): String? {
        return getSP(context).getString(KEY_BIND_REMARK, null)
    }

    fun saveBindRemark(context: Context, remark: String?) {
        getSP(context).edit { putString(KEY_BIND_REMARK, remark) }
        bindRemark = remark
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

    fun saveBindStatus(context: Context, isBound: Boolean, boundUsername: String?, remark: String? = null) {
        val editor = getSP(context).edit()
        editor.putBoolean(KEY_IS_BIND, isBound)
        editor.putString("bound_username", boundUsername)
        editor.putString(KEY_BIND_REMARK, remark)
        editor.apply()
        this.isBound = isBound
        this.boundUsername = boundUsername
        this.bindRemark = remark
    }

    fun unbindDevice() {
        isBound = false
        boundUsername = null
        bindRemark = null
    }

    fun clearBindStatus(context: Context) {
        getSP(context).edit().clear().apply()
        isBound = false
        boundUsername = null
        bindRemark = null
    }

    fun isBound(context: Context): Boolean {
        return getSP(context).getBoolean(KEY_IS_BIND, false)
    }

    fun getBindStatus(): Pair<Boolean, String?> {
        Log.d(TAG, "getBindStatus: BindManager里管理的绑定用户的用户名是$boundUsername")
        return Pair(isBound, boundUsername)
    }
}