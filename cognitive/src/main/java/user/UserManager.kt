package user

import android.content.Context
import bind_device.BindStatusManager
import debug_login.LoginStatusManager

object UserManager {

    private var userId: String = ""
    private var otherId: String? = null

    fun init(context: Context) {
        userId = LoginStatusManager.getLoggedInUserId(context)
        otherId = BindStatusManager.getBindStatus().second
    }

    fun getUserId(): String {
        return userId
    }

    fun getOtherId(): String? {
        return otherId
    }

}