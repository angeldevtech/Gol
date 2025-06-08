package com.angeldevtech.gol.domain.usecases

import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.domain.repositories.ScheduleRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetScheduleCategoriesUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    operator fun invoke(): Flow<Result<List<ScheduleCategories>>> {
        return repository.observeSchedule()
            .map { list ->
                val categories = list
                    .groupBy { it.category }
                    .map { (cat, items) ->
                        val sortedItems = items.sortedWith(
                            compareBy<ScheduleItem> { it.date == "Fecha desconocida" }
                                .thenBy { it.date }
                                .thenBy { it.hour == "Hora desconocida" }
                                .thenBy { it.hour }
                        )
                        ScheduleCategories(cat, sortedItems)
                    }
                    .sortedBy { category ->
                        when (category.name) {
                            "Futbol" -> 0
                            "Otros" -> Int.MAX_VALUE - 1
                            "Deporte desconocido" -> Int.MAX_VALUE
                            else -> 1
                        }
                    }
                Result.success(categories)
            }
            .flowOn(Dispatchers.Default)
    }
}