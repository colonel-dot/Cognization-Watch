package user

import android.content.Context
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.remote.LoginStatusManager

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