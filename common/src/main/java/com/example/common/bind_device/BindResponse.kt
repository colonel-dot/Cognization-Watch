package com.example.common.bind_device

class BindResponse {
    val code: Int = 0 // 后端约定的状态码：200 成功，400 失败，403 重复绑定
}