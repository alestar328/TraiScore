package com.develop.traiscore.data
import com.develop.traiscore.domain.WorkoutModel
import com.develop.traiscore.domain.WorkoutType
import java.util.Calendar
import java.util.Date

// Generar datos de prueba para el ejercicio "Press banca"
val pressBancaWorkouts = generatePressBancaWorkouts()

fun generatePressBancaWorkouts(): List<WorkoutModel> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 8) // Hora fija para consistencia

    val workouts = mutableListOf<WorkoutModel>()

    // Datos de progreso ficticio
    val progressData = listOf(
        Pair(70.0, 12), // Peso, Repeticiones
        Pair(75.0, 10),
        Pair(80.0, 8),
        Pair(82.5, 8),
        Pair(85.0, 6),
        Pair(87.5, 6),
        Pair(90.0, 5),
        Pair(92.5, 5)
    )

    // Generar 2 sesiones por semana durante 4 semanas
    progressData.forEachIndexed { index, (weight, reps) ->
        // Día de la semana 1 (Lunes)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.add(Calendar.WEEK_OF_YEAR, index / 2) // Incrementar semanas
        workouts.add(
            WorkoutModel(
                timestamp = calendar.time,
                type = WorkoutType(
                    title = "Press banca",
                    weight = weight,
                    reps = reps,
                    rir = 2 // Ejemplo de RIR constante
                )
            )
        )

        // Día de la semana 2 (Jueves)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        workouts.add(
            WorkoutModel(
                timestamp = calendar.time,
                type = WorkoutType(
                    title = "Press banca",
                    weight = weight + 2.5, // Incremento de peso
                    reps = reps - 1 // Repeticiones ligeramente reducidas
                )
            )
        )
    }

    return workouts
}