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

// 用于健康数据的请求
data class UpdateDailyHealthRequest(
    val elder_account: String,
    val date: String,  // 需要转为字符串格式
    val data: Map<String, Any?>  // 或者直接传 DailyBehaviorEntity 并序列化
)

// 用于风险数据的请求
data class UpdateDailyRiskRequest(
    val elder_account: String,
    val date: String,
    val data: Map<String, Any?>
)

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

    @GET("daily/all")
    suspend fun getAllDailyBehavior(account: String): List<DailyBehaviorEntity>

    @GET("daily/allrisk")
    suspend fun getAllDailyRisk(account: String): List<DailyRiskEntity>

}