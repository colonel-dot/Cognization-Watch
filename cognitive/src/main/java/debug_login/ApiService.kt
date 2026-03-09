package debug_login

import bind_device.BindRequest
import bind_device.BindResponse
import persistense.DailyBehaviorEntity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import risk.model.DailyRiskResult
import risk.persistence.DailyRiskEntity
import java.time.LocalDate

interface ApiService {
    @POST("login") // 替换为你的实际接口路径
    suspend fun login(@Body request: LoginRequest): LoginResponse // 关键：suspend + 直接返回 LoginResponse

    @POST("bind")
    suspend fun bind(@Body request: BindRequest): BindResponse

    @POST("elder/updatedailyrisk")
    suspend fun postDailyRisk(account: String, date: LocalDate, risk: DailyRiskResult)

    @GET("elder/dailyrisk")
    suspend fun  getDailyRisk(account: String, date: LocalDate): DailyRiskEntity

    @POST("elder/updatedailyhealthrecord")
    suspend fun postDailyBehavior(account: String, date: LocalDate, record: DailyBehaviorEntity)

    @GET("elder/daily")
    suspend fun getDailyBehavior(account: String?, date: LocalDate): DailyBehaviorEntity

}