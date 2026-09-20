package com.swasthyasathi.app.ai

import com.swasthyasathi.app.data.model.AskRequest
import com.swasthyasathi.app.data.model.AskResponse
import com.swasthyasathi.app.data.model.EnvironmentalTelemetry
import com.swasthyasathi.app.data.model.RiskEngineResult
import com.swasthyasathi.app.data.model.UserProfile
import com.swasthyasathi.app.data.network.RetrofitClient
import com.swasthyasathi.app.wearable.WearableTelemetry

class AIRepository {

    suspend fun askQuestion(
        question: String,
        language: AppLanguage,
        profile: UserProfile,
        envTelemetry: EnvironmentalTelemetry,
        wearableTelemetry: WearableTelemetry,
        riskResult: RiskEngineResult,
        isOfflineMode: Boolean
    ): AskResponse {
        // If query is a greeting, handle instantly with personalized RAG context
        if (ResearchRAGEngine.isGreeting(question)) {
            val (greetingAnswer, sources) = ResearchRAGEngine.generateGreetingResponse(
                profile, envTelemetry, wearableTelemetry, riskResult, language
            )
            return AskResponse(answer = greetingAnswer, sources = sources)
        }

        if (isOfflineMode) {
            val (ragAnswer, sources) = ResearchRAGEngine.retrieveAndSynthesize(
                question, profile, envTelemetry, wearableTelemetry, riskResult, language
            )
            return AskResponse(answer = ragAnswer, sources = sources)
        }

        return try {
            val req = AskRequest(question = question)
            val response = RetrofitClient.apiService.askQuestion(req)

            if (response.isSuccessful && response.body() != null && response.body()!!.answer.isNotBlank()) {
                response.body()!!
            } else {
                val (ragAnswer, sources) = ResearchRAGEngine.retrieveAndSynthesize(
                    question, profile, envTelemetry, wearableTelemetry, riskResult, language
                )
                AskResponse(answer = ragAnswer, sources = sources)
            }
        } catch (e: Exception) {
            val (ragAnswer, sources) = ResearchRAGEngine.retrieveAndSynthesize(
                question, profile, envTelemetry, wearableTelemetry, riskResult, language
            )
            AskResponse(answer = ragAnswer, sources = sources)
        }
    }
}
