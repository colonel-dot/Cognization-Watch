package debug_login

import com.example.common.persistense.behavior.DailyBehaviorEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.example.common.persistense.risk.DailyRiskEntity
import com.example.common.login.remote.LoginRequest
import com.example.common.login.remote.LoginResponse

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

/*    @POST("elder/updatedailyrisk")
    suspend fun postDailyRisk(account: String, date: LocalDate, risk: DailyRiskResult)*/

/*    @POST("elder/updatedailyhealthrecord")
    suspend fun postDailyBehavior(account: String, date: LocalDate, record: DailyBehaviorEntity)*/

    @POST("elder/updatedailyhealthrecord")
    suspend fun postDailyBehavior(
        @Body request: UpdateDailyHealthRequest
    ): Response<Unit>

    @POST("elder/updatedailyrisk")
    suspend fun postDailyRisk(
        @Body request: UpdateDailyRiskRequest
    ): Response<Unit>

    @GET("elder/daily")
    suspend fun getDailyBehavior(
        @Query("username") child_account: String?, // 关键：account → child_account
        @Query("elder_account") account: String?, // 关键：account → elder_account
        @Query("date") date: String
    ): DailyBehaviorEntity

    // 3. 对应 /elder/dailyrisk 接口（GetDailyRiskHandler）
    @GET("elder/dailyrisk")
    suspend fun getDailyRisk(
        @Query("username") childAccount: String?, // 关键：account → child_account
        @Query("elder_account") account: String, // 关键：account → elder_account
        @Query("date") date: String
    ): DailyRiskEntity
}