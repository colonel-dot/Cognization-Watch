package debug_login

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 基础 URL（替换为你的后端地址，必须以 / 结尾）
    private const val BASE_URL = "http://192.168.1.70:8080/"

    // 单例 Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON 转实体
            .build()
    }

    // 获取 ApiService 实例
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}