package com.angeldevtech.gol.domain.repositories

import com.angeldevtech.gol.domain.models.ScheduleItem
import kotlinx.coroutines.flow.StateFlow

interface ScheduleRepository {
    fun observeSchedule(): StateFlow<List<ScheduleItem>>
    suspend fun refreshSchedule(): Result<Unit>
    fun getItemById(id: Int): ScheduleItem?
}