package com.angeldevtech.gol.domain.usecases

import com.angeldevtech.gol.domain.repositories.ScheduleRepository
import javax.inject.Inject

class RefreshScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshSchedule()
    }
}