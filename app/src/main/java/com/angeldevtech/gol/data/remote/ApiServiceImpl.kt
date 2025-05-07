package com.angeldevtech.gol.data.remote

import com.angeldevtech.gol.BuildConfig
import com.angeldevtech.gol.data.models.ScheduleDto
import com.angeldevtech.gol.data.models.ScheduleItemDto
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import jakarta.inject.Inject

const val apiBaseUrl = BuildConfig.API_BASE_URL

class ApiServiceImpl @Inject constructor(
    private val lazyClient: Lazy<HttpClient>
): ApiService {

    private val client: HttpClient
        get() {
            return lazyClient.get()
        }

    override suspend fun fetchSchedule(): List<ScheduleItemDto> {
        val response: ScheduleDto = client
            .get(apiBaseUrl)
            .body()
        return response.data
    }
}