package read_assessment.data

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "YoudaoApiService"

object YoudaoApiService {

    private const val APP_KEY = "5ff9d8f4dedd4ac9"
    private const val APP_SECRET = "ir2QWhtpFmJ2CBjQrfe801WC24WOdT6F"
    private const val API_URL = "https://openapi.youdao.com/iseapi"

    /**
     * 读取文件并 Base64
     */
    private fun fileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * input 字段计算方式
     */
    fun truncate(q: String?): String? {
        if (q == null) {
            return null
        }
        val len = q.length
        return if (len <= 20) q else (q.substring(0, 10) + len + q.substring(len - 10, len))
    }

    /**
     * SHA256 签名
     */
    private fun sha256(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun evaluateSpeech(file: File, refText: String, langType: String): JSONObject =
        withContext(Dispatchers.IO) {
            val q = fileToBase64(file)
            val salt = UUID.randomUUID().toString()
            val curtime = (System.currentTimeMillis() / 1000).toString()

            val input = truncate(q) ?: ""
            val signStr = APP_KEY + input + salt + curtime + APP_SECRET
            val sign = sha256(signStr)


            val params = mapOf(
                "q" to q,
                "text" to refText,
                "langType" to langType,
                "appKey" to APP_KEY,
                "salt" to salt,
                "curtime" to curtime,
                "sign" to sign,
                "signType" to "v2",
                "format" to "wav",
                "rate" to "16000",
                "channel" to "1",
                "type" to "1",
                "docType" to "json"
            )
            Log.d(TAG, "开始创建HTTP连接，URL: $API_URL")

            val url = URL(API_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                useCaches = false
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                // 设置连接超时和读取超时
                connectTimeout = 10000  // 10秒连接超时
                readTimeout = 30000     // 30秒读取超时
            }

            try {
                // 2. 连接尝试日志
                Log.d(TAG, "尝试建立连接...")

                // 实际建立连接
                conn.connect()

                // 3. 连接成功日志
                Log.d(TAG, "连接成功建立，准备发送数据")

                //val postData = params.map { "${it.key}=${it.value}" }.joinToString("&")
                val postData = params.map {
                    "${it.key}=" + URLEncoder.encode(it.value, "UTF-8")
                }.joinToString("&")

                conn.outputStream.use { outputStream ->
                    outputStream.write(postData.toByteArray(Charsets.UTF_8))
                }

                // 4. 数据发送后，获取响应前日志
                Log.d(TAG, "请求数据已发送，等待响应...")

                val responseCode = conn.responseCode
                Log.d(TAG, "HTTP响应码: $responseCode")

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = conn.errorStream?.bufferedReader()?.readText() ?: "无错误信息"
                    Log.e(TAG, "请求失败，响应码: $responseCode, 错误信息: $errorStream")
                }

                val response = conn.inputStream.bufferedReader().readText()
                Log.d(TAG, "API响应数据: $response")

                JSONObject(response)

            } catch (e: Exception) {
                // 5. 连接或请求异常日志
                Log.e(TAG, "请求发生异常: ${e.message}", e)
                throw e
            } finally {
                // 6. 连接关闭日志
                Log.d(TAG, "关闭HTTP连接")
                conn.disconnect()
            }
        }
}

