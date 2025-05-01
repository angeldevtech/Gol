package com.angeldevtech.gol.domain.usecases

import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.domain.repositories.ScheduleRepository
import jakarta.inject.Inject

class GetScheduleItemByIdUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    operator fun invoke(id: Int): ScheduleItem? {
        return repository.getItemById(id)
    }
}