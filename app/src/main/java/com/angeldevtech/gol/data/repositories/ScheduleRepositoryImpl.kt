package com.angeldevtech.gol.data.repositories

import com.angeldevtech.gol.data.mappers.toSimplified
import com.angeldevtech.gol.data.remote.ApiService
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.domain.repositories.ScheduleRepository
import com.angeldevtech.gol.utils.DateChangeObserver
import com.angeldevtech.gol.utils.computeRelativeDay
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ScheduleRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    dateChangeObserver: DateChangeObserver,
    externalScope: CoroutineScope
): ScheduleRepository {
    private val rawScheduleFlow = MutableStateFlow<List<ScheduleItem>>(emptyList())

    private val _scheduleFlow: StateFlow<List<ScheduleItem>> = combine(
        rawScheduleFlow,
        dateChangeObserver.onDateChanged.onStart { emit(Unit) }
    ) { items, _ ->
        items.map { item ->
            item.copy(relativeDay = computeRelativeDay(item.date))
        }
    }.stateIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    override fun observeSchedule(): StateFlow<List<ScheduleItem>> = _scheduleFlow

    override suspend fun refreshSchedule(): Result<Unit> {
        return try {
            val items = apiService.fetchSchedule()
                .mapNotNull { it.toSimplified() }
            rawScheduleFlow.value = items
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getItemById(id: Int): ScheduleItem? {
        return _scheduleFlow.value.find { it.id == id }
    }
}