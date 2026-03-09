package bind_device

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import bind_device.BindActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val TAG = "BindViewModel"

class BindViewModel(application: Application): AndroidViewModel(application) {

    private val _bindResult = MutableSharedFlow<Boolean>()
    // 公开只读的Flow，供UI层收集结果
    val bindResult: SharedFlow<Boolean> = _bindResult

    fun bind(bindname: String) {
        viewModelScope.launch {
            // 使用Application上下文获取SharedPreferences，避免内存泄漏
            val mname = getApplication<Application>()
                .getSharedPreferences("login_status", Context.MODE_PRIVATE)
                .getString("username", "") ?: ""

            try {
                BindRepository().bind(BindRequest(mname, bindname)).collect { result ->
                    result.fold(
                        onSuccess = { bindResponse ->
                            if (bindResponse.code == 200) {
                                // 保存状态仍用Application上下文
                                BindStatusManager.saveBindStatus(
                                    getApplication(),
                                    true,
                                    bindname
                                )
                                Log.d(TAG, "bind: 绑定成功了")
                                // 发送成功结果给UI层
                                _bindResult.emit(true)
                            } else {
                                // 处理接口返回非200的情况
                                _bindResult.emit(false)
                            }
                        },
                        onFailure = { e ->
                            Log.d(TAG, "bind:${e.message}")
                            // 发送失败结果给UI层
                            _bindResult.emit(false)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "bind: $e")
                // 发送异常失败结果给UI层
                _bindResult.emit(false)
            }
        }
    }
}