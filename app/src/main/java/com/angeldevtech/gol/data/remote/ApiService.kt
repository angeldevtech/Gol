package com.angeldevtech.gol.data.remote

import com.angeldevtech.gol.data.models.ScheduleItemDto

interface ApiService {
    suspend fun fetchSchedule(): List<ScheduleItemDto>
}