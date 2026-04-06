package com.dimas.dimasproject.feature.randomnumber.data.remote

import android.util.Log
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class RandomNumberRemoteDataSource(
    private val client: HttpClient
) {
    suspend fun fetchRandomNumber(): RandomNumberResponse {
        return try {
            client.get("https://aisenseapi.com/services/v1/random_number").body()
        } catch (e: Exception) {
            Log.e("RandomNumber", "Error: ${e::class.simpleName} - ${e.message}", e)
            throw e
        }
    }
}