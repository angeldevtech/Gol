package com.angeldevtech.gol.data.repositories

import com.angeldevtech.gol.data.mappers.toSimplified
import com.angeldevtech.gol.data.remote.ApiService
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.domain.repositories.ScheduleRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScheduleRepositoryImpl @Inject constructor(
    private val apiService: ApiService
): ScheduleRepository {
    private val _scheduleFlow = MutableStateFlow<List<ScheduleItem>>(emptyList())

    override fun observeSchedule(): StateFlow<List<ScheduleItem>> = _scheduleFlow

    override suspend fun refreshSchedule(): Result<Unit> {
        return try {
            val items = apiService.fetchSchedule()
                .mapNotNull { it.toSimplified() }
            _scheduleFlow.value = items
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getItemById(id: Int): ScheduleItem? {
        return _scheduleFlow.value.find { it.id == id }
    }
}