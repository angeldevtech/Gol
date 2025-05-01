package com.angeldevtech.gol.domain.usecases

import com.angeldevtech.gol.domain.repositories.PlayerRepository
import jakarta.inject.Inject

class ExtractM3U8UrlUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(url: String): Result<String> {
        return repository.extractM3U8Url(url)
    }
}