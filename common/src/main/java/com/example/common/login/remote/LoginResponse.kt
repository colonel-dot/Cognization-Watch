package com.example.common.login.remote

class LoginResponse {
    // 仅需 Getter（Setter 可选，因为是解析后端返回数据）
    val code: Int = 0 // 后端约定的状态码：如 200 成功，400 失败
        get() = field
}