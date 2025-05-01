package com.angeldevtech.gol.di

import com.angeldevtech.gol.data.remote.ApiService
import com.angeldevtech.gol.data.remote.ApiServiceImpl
import com.angeldevtech.gol.data.repositories.PlayerRepositoryImpl
import com.angeldevtech.gol.data.repositories.ScheduleRepositoryImpl
import com.angeldevtech.gol.domain.repositories.PlayerRepository
import com.angeldevtech.gol.domain.repositories.ScheduleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideApiService(client: HttpClient): ApiService {
        return ApiServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(apiService: ApiService): ScheduleRepository {
        return ScheduleRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }
}