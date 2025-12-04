package read_assessment.data

import org.json.JSONObject
import java.io.File

class ReadAssessmentRepository {

    suspend fun evaluate(file: File, refText: String, langType: String): JSONObject {
        return YoudaoApiService.evaluateSpeech(file, refText, langType)
    }
}