package com.angeldevtech.gol.domain.repositories

interface PlayerRepository {
    suspend fun extractM3U8Url(webPlayerUrl: String): Result<String>
}