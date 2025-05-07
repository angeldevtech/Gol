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
                            compareBy<ScheduleItem> {
                                it.date
                            }.thenBy {
                                it.hour
                            }
                        )
                        ScheduleCategories(cat, sortedItems)
                    }
                    .let { categories ->
                        val (futbol, others) = categories.partition { it.name == "Futbol" }
                        futbol + others
                    }
                Result.success(categories)
            }
            .flowOn(Dispatchers.Default)
    }
}