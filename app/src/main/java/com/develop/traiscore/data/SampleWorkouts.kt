package com.develop.traiscore.data
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutType
import java.util.Calendar
import java.util.Date

// Datos de prueba
val workoutDataMap = mapOf(
    "press_banca" to generatePressBancaWorkouts(),
    "sentadilla" to generateSentadillaWorkouts(),
    "dominadas" to generateDominadasWorkouts()
)

fun generatePressBancaWorkouts(): List<WorkoutModel> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 8) // Hora fija para consistencia
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)

    val workouts = mutableListOf<WorkoutModel>()

    // Datos de progreso para Press Banca
    val progressData = listOf(
        Pair(70.0, 12), // Peso, Repeticiones
        Pair(75.0, 10),
        Pair(80.0, 8),
        Pair(82.5, 7),
        Pair(85.0, 6),
        Pair(87.5, 5),
        Pair(90.0, 5),
        Pair(92.5, 4),
        Pair(95.0, 3)
    )

    progressData.forEachIndexed { index, (weight, reps) ->
        calendar.time = Date() // Reinicia la fecha base
        calendar.add(Calendar.DAY_OF_YEAR, index * 7) // Incrementar por semanas

        workouts.add(
            WorkoutModel(
                timestamp = calendar.time,
                type = WorkoutType(
                    title = "Press banca",
                    weight = weight,
                    reps = reps,
                    rir = 2
                )
            )
        )
    }

    println("Press Banca Workouts Generated: ${workouts.size}")
    return workouts
}

fun generateSentadillaWorkouts(): List<WorkoutModel> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 8)

    val workouts = mutableListOf<WorkoutModel>()

    // Datos de progreso para Sentadilla
    val progressData = listOf(
        Pair(100.0, 10),
        Pair(105.0, 8),
        Pair(110.0, 8),
        Pair(115.0, 7),
        Pair(120.0, 6),
        Pair(125.0, 6),
        Pair(130.0, 5),
        Pair(135.0, 4),
        Pair(140.0, 3)
    )

    progressData.forEachIndexed { index, (weight, reps) ->
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        calendar.add(Calendar.DAY_OF_YEAR, index) // Incrementar un día por iteración
        workouts.add(
            WorkoutModel(
                timestamp = calendar.time,
                type = WorkoutType(
                    title = "Sentadilla",
                    weight = weight,
                    reps = reps,
                    rir = 2
                )
            )
        )
    }

    return workouts
}

fun generateDominadasWorkouts(): List<WorkoutModel> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 8)

    val workouts = mutableListOf<WorkoutModel>()

    // Datos de progreso para Dominadas
    val progressData = listOf(
        Pair(20.0, 12), // Peso añadido (lastre), Repeticiones
        Pair(25.0, 10),
        Pair(30.0, 8),
        Pair(32.5, 7),
        Pair(35.0, 6),
        Pair(37.5, 5),
        Pair(40.0, 5),
        Pair(42.5, 4),
        Pair(45.0, 3)
    )

    progressData.forEachIndexed { index, (weight, reps) ->
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        calendar.add(Calendar.DAY_OF_YEAR, index) // Incrementar un día por iteración
        workouts.add(
            WorkoutModel(
                timestamp = calendar.time,
                type = WorkoutType(
                    title = "Dominadas",
                    weight = weight,
                    reps = reps,
                    rir = 2
                )
            )
        )
    }

    return workouts
}