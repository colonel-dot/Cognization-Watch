package debug_login

import bind_device.BindRequest
import bind_device.BindResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @POST("login") // 替换为你的实际接口路径
    suspend fun login(@Body request: LoginRequest): LoginResponse // 关键：suspend + 直接返回 LoginResponse

    @POST("bind")
    suspend fun bind(@Body request: BindRequest): BindResponse

    /*@POST("elder/schulte")

    @POST("elder/daily")

    @POST("elder/steps")*/

}