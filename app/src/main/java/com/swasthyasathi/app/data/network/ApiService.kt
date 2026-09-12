package com.swasthyasathi.app.data.network

import com.swasthyasathi.app.data.model.AskRequest
import com.swasthyasathi.app.data.model.AskResponse
import com.swasthyasathi.app.data.model.DisasterWarning
import com.swasthyasathi.app.data.model.EnvironmentalTelemetry
import com.swasthyasathi.app.data.model.RiskEngineResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("api/telemetry")
    suspend fun getTelemetry(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("location_name") locationName: String
    ): Response<EnvironmentalTelemetry>

    @POST("ask")
    suspend fun askQuestion(
        @Body request: AskRequest
    ): Response<AskResponse>

    @POST("api/risk")
    suspend fun evaluateRisk(
        @Body request: Map<String, Any>
    ): Response<RiskEngineResult>

    @POST("api/sos")
    suspend fun triggerSos(
        @Body request: Map<String, Any>
    ): Response<Map<String, Any>>
}
