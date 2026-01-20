package risk

class RiskRepository(
    private val behaviorDao: DailyBehaviorDao,
    private val riskDao: DailyRiskDao
) {

    /**
     * 计算某一天的最终 Risk（通常是昨天）
     */
    suspend fun calculateDailyRisk(date: LocalDate) {

        // 至少取最近 14 天（你算法里 5 天是下限）
        val behaviors = behaviorDao.loadFrom(date.minusDays(14))

        if (behaviors.size < 5) return

        val normalized = behaviors.map {
            BehaviorNormalizer.normalize(it)
        }

        val risk = DailyRiskCalculator.calculate(normalized)

        val entity = DailyRiskEntity(
            date = risk.date,
            riskScore = risk.riskScore,
            riskLevel = risk.riskLevel,

            sleepRisk = extractRisk(risk, "sleep"),
            schulteRisk = extractRisk(risk, "schulte"),
            stepsRisk = extractRisk(risk, "steps"),
            speechRisk = extractRisk(risk, "speech"),

            explanations = risk.explanations.joinToString(";")
        )

        riskDao.upsert(entity)
    }

    /**
     * 给 UI / 告警系统用
     */
    suspend fun loadRecentRisks(days: Long): List<DailyRiskEntity> {
        return riskDao.loadFrom(LocalDate.now().minusDays(days))
    }

    /**
     * 简化实现：你也可以直接把子风险传出来
     */
    private fun extractRisk(
        risk: DailyRiskResult,
        type: String
    ): Double {
        // 当前版本 RiskFusion 没返回子风险
        // 这里先占位，后面我会教你怎么改得更优雅
        return 0.0
    }
}
