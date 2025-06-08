package com.angeldevtech.gol.utils

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

fun computeRelativeDay(dateStr: String): String {
    if (dateStr == "Fecha desconocida") return dateStr

    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val itemDate = formatter.parse(dateStr) ?: return dateStr
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val itemCal = Calendar.getInstance().apply {
            time = itemDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffDays = ChronoUnit.DAYS.between(
            today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            itemCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        ).toInt()

        when (diffDays) {
            0 -> "HOY"
            1 -> "MAÑANA"
            -1 -> "AYER"
            else -> dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}