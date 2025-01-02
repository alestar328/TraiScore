package com.develop.traiscore.domain.model

import com.develop.traiscore.domain.WorkoutModel
import java.util.Calendar
import java.util.Date

class FilterWorkoutsUseCase {
    fun invoke(workouts: List<WorkoutModel>, filter: String): List<WorkoutModel> {
        val calendar = Calendar.getInstance()
        val now = calendar.time

        return when (filter) {
            "Hoy" -> workouts.filter { isSameDay(it.timestamp, now) }
            "Esta semana" -> workouts.filter { isSameWeek(it.timestamp, now) }
            "Este mes" -> workouts.filter { isSameMonth(it.timestamp, now) }
            else -> workouts
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }
}